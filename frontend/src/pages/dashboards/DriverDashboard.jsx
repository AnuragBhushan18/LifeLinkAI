import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../context/WebSocketContext';
import {
  Truck,
  Navigation,
  Phone,
  Edit2,
  Save,
  MapPin,
  AlertTriangle,
  Play,
  Square,
  Clock,
  Building2,
  User,
  Activity,
  CheckCircle,
} from 'lucide-react';
import api from '../../services/api';
import { emergencyService } from '../../services/entityServices';
import emergencyRealtimeService from '../../services/emergencyRealtimeService';
import websocketService from '../../services/websocketService';
import LiveTrackingMap from '../../components/LiveTrackingMap';
import EmergencyTimeline from '../../components/EmergencyTimeline';
import ConnectionBadge from '../../components/ConnectionBadge';

const DriverDashboard = () => {
  const { user } = useAuth();
  const { connectionState } = useWebSocket();
  const [profile, setProfile] = useState({
    licenseNumber: '',
    experienceYears: 0,
    availabilityStatus: 'AVAILABLE',
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [activeEmergency, setActiveEmergency] = useState(null);
  const [ambulance, setAmbulance] = useState(null);
  const [hospital, setHospital] = useState(null);

  // Simulation state
  const [isSimulating, setIsSimulating] = useState(false);
  const simulationIntervalRef = useRef(null);
  const simulationStepRef = useRef(0);

  const fetchActiveEmergency = useCallback(async () => {
    try {
      const res = await emergencyService.getAll();
      if (res.data && res.data.length > 0) {
        const active = res.data.find(
          (e) => !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(e.status)
        );
        if (active) {
          setActiveEmergency(active);
          if (active.recommendedHospitalId) {
            api.get(`/hospitals/${active.recommendedHospitalId}`)
              .then((r) => setHospital(r.data))
              .catch(() => {});
          }
          if (active.assignedAmbulanceId) {
            api.get(`/ambulances/${active.assignedAmbulanceId}`)
              .then((r) => setAmbulance(r.data))
              .catch(() => {});
          }
        } else {
          setActiveEmergency(null);
        }
      } else {
        setActiveEmergency(null);
      }
    } catch (error) {
      console.error('Error fetching driver emergencies', error);
    }
  }, []);

  const fetchDriverProfile = useCallback(async () => {
    try {
      const res = await api.get(`/drivers/user/${user.id}`);
      if (res.data) setProfile(res.data);
    } catch (error) {
      console.error('Error fetching driver profile', error);
    }
  }, [user.id]);

  useEffect(() => {
    const loadData = async () => {
      await Promise.all([fetchDriverProfile(), fetchActiveEmergency()]);
      setLoading(false);
    };

    if (user?.id) loadData();
  }, [user.id, fetchDriverProfile, fetchActiveEmergency]);

  // Reconcile data on WebSocket reconnect
  useEffect(() => {
    const unbind = websocketService.onReconnect(() => {
      console.log('Driver reconnected: reconciling state');
      fetchActiveEmergency();
    });
    return () => unbind();
  }, [fetchActiveEmergency]);

  // Real-time WebSocket subscription
  useEffect(() => {
    if (!user?.id) return;

    // Driver specific notifications/assignment topic
    const unsubDriver = emergencyRealtimeService.subscribeToDriver(user.id, (event) => {
      console.log('[DriverDashboard] Driver event received:', event);
      fetchActiveEmergency();
    });

    let unsubEmergency = () => {};
    if (activeEmergency?.id) {
      unsubEmergency = emergencyRealtimeService.subscribeToEmergency(activeEmergency.id, (event) => {
        console.log('[DriverDashboard] Emergency event received:', event);

        setActiveEmergency((prev) => {
          if (!prev || prev.id !== event.emergencyId) return prev;
          if (['COMPLETED', 'CANCELLED', 'REJECTED'].includes(event.status)) {
            stopSimulation();
            return {
              ...prev,
              status: event.status,
              timeline: event.timeline || prev.timeline,
            };
          }
          return {
            ...prev,
            status: event.status || prev.status,
            currentAmbulanceLatitude: event.latitude !== undefined ? event.latitude : prev.currentAmbulanceLatitude,
            currentAmbulanceLongitude: event.longitude !== undefined ? event.longitude : prev.currentAmbulanceLongitude,
            estimatedEtaMinutes: event.etaMinutes !== undefined ? event.etaMinutes : prev.estimatedEtaMinutes,
            estimatedDistanceKm: event.distanceKm !== undefined ? event.distanceKm : prev.estimatedDistanceKm,
            timeline: event.timeline && event.timeline.length > 0 ? event.timeline : prev.timeline,
          };
        });
      });
    }

    return () => {
      unsubDriver();
      unsubEmergency();
    };
  }, [user?.id, activeEmergency?.id, fetchActiveEmergency]);

  const stopSimulation = () => {
    if (simulationIntervalRef.current) {
      clearInterval(simulationIntervalRef.current);
      simulationIntervalRef.current = null;
    }
    setIsSimulating(false);
  };

  // Stop simulation on unmount
  useEffect(() => {
    return () => stopSimulation();
  }, []);

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    try {
      await api.put(`/drivers/user/${user.id}`, profile);
      setIsEditing(false);
    } catch (error) {
      console.error('Error saving profile', error);
      alert('Failed to save profile');
    }
  };

  const updateAvailabilityStatus = async (status) => {
    try {
      await api.put(`/drivers/user/${user.id}/status`, { status });
      setProfile({ ...profile, availabilityStatus: status });
    } catch (error) {
      console.error('Error updating status', error);
    }
  };

  // State Transition Handlers
  const handleTransition = async (newStatus) => {
    if (!activeEmergency) return;
    try {
      await emergencyService.updateStatus(activeEmergency.id, newStatus);
      setActiveEmergency((prev) => ({ ...prev, status: newStatus }));
      fetchActiveEmergency();
    } catch (error) {
      console.error('Transition error', error);
      alert(error.response?.data?.message || 'Invalid state transition');
    }
  };

  // Simulated GPS Movement Generator (Requirement 8)
  const startSimulation = () => {
    if (!activeEmergency) return;
    setIsSimulating(true);

    const startLat = ambulance?.latitude || 40.7128;
    const startLon = ambulance?.longitude || -74.0060;

    // Depending on status, move to patient or move to hospital
    let targetLat = activeEmergency.latitude;
    let targetLon = activeEmergency.longitude;

    if (activeEmergency.status === 'GOING_TO_HOSPITAL' && hospital?.latitude) {
      targetLat = hospital.latitude;
      targetLon = hospital.longitude;
    }

    const totalSteps = 20;
    simulationStepRef.current = 0;

    simulationIntervalRef.current = setInterval(async () => {
      simulationStepRef.current += 1;
      const progress = Math.min(1.0, simulationStepRef.current / totalSteps);

      const currentLat = startLat + (targetLat - startLat) * progress;
      const currentLon = startLon + (targetLon - startLon) * progress;

      // Broadcast simulated location via STOMP / WebSocket (Requirement 6, 7, 8)
      await emergencyRealtimeService.sendLocationUpdate(currentLat, currentLon, true);

      // Local state update
      setActiveEmergency((prev) => ({
        ...prev,
        currentAmbulanceLatitude: currentLat,
        currentAmbulanceLongitude: currentLon,
      }));

      if (progress >= 1.0) {
        stopSimulation();
      }
    }, 3000); // 3 seconds interval as per Requirement 7
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Driver Control Console</h1>
          <p className="text-sm text-gray-500 mt-1">Real-time dispatch navigation and emergency coordination</p>
        </div>
        <div className="flex items-center gap-3">
          <ConnectionBadge />
          <button
            onClick={() => (isEditing ? handleSave() : setIsEditing(true))}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl font-medium shadow-sm transition-all"
          >
            {isEditing ? (
              <>
                <Save size={18} /> Save Details
              </>
            ) : (
              <>
                <Edit2 size={18} /> Edit Details
              </>
            )}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Driver Info & Duty Status */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center">
            <div className="w-20 h-20 bg-orange-100 text-orange-600 rounded-2xl flex items-center justify-center text-4xl font-bold mx-auto mb-4 shadow-inner">
              <Truck size={36} />
            </div>
            <h3 className="text-xl font-bold text-gray-900">{user?.name}</h3>
            <p className="text-sm text-gray-500 mb-4">Certified Ambulance Driver</p>

            {/* Duty Status Selector */}
            <div className="space-y-2">
              <span className="block text-xs font-semibold text-gray-400 uppercase tracking-wider text-left mb-1">
                Duty Status
              </span>
              <button
                onClick={() => updateAvailabilityStatus('AVAILABLE')}
                className={`w-full text-sm font-semibold py-2.5 px-3 rounded-xl transition-all border ${
                  profile.availabilityStatus === 'AVAILABLE'
                    ? 'bg-emerald-50 border-emerald-300 text-emerald-800 shadow-sm'
                    : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                }`}
              >
                Available for Dispatch
              </button>
              <button
                onClick={() => updateAvailabilityStatus('ON_MISSION')}
                className={`w-full text-sm font-semibold py-2.5 px-3 rounded-xl transition-all border ${
                  profile.availabilityStatus === 'ON_MISSION'
                    ? 'bg-blue-50 border-blue-300 text-blue-800 shadow-sm'
                    : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                }`}
              >
                On Active Mission
              </button>
              <button
                onClick={() => updateAvailabilityStatus('OFF_DUTY')}
                className={`w-full text-sm font-semibold py-2.5 px-3 rounded-xl transition-all border ${
                  profile.availabilityStatus === 'OFF_DUTY'
                    ? 'bg-gray-100 border-gray-300 text-gray-800'
                    : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'
                }`}
              >
                Off Duty
              </button>
            </div>
          </div>

          {/* Credentials Card */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-bold mb-4 flex items-center gap-2 text-gray-800">
              <MapPin className="text-orange-500" size={20} /> Driver Profile
            </h2>
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">License Number</label>
                {isEditing ? (
                  <input
                    type="text"
                    name="licenseNumber"
                    value={profile.licenseNumber || ''}
                    onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                ) : (
                  <p className="text-gray-900 font-semibold">{profile.licenseNumber || 'Not provided'}</p>
                )}
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">Experience (Years)</label>
                {isEditing ? (
                  <input
                    type="number"
                    name="experienceYears"
                    value={profile.experienceYears || ''}
                    onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.experienceYears || '0'} Years</p>
                )}
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">Assigned Ambulance</label>
                <p className="text-gray-900 font-bold text-sm">
                  {ambulance?.vehicleNumber || 'NY-1000'} ({ambulance?.type || 'ICU'})
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Active Emergency Dispatch & Controls */}
        <div className="lg:col-span-2 space-y-6">
          {activeEmergency ? (
            <div className="space-y-6">
              {/* Dispatch Action Header Card */}
              <div className="bg-white rounded-2xl shadow-sm border border-red-100 p-6">
                <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-gray-100">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 bg-red-100 text-red-600 rounded-xl flex items-center justify-center animate-pulse">
                      <AlertTriangle size={24} />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h2 className="text-xl font-extrabold text-gray-900">Active Dispatch Mission</h2>
                        <span className="font-mono text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                          #{activeEmergency.id.slice(-6)}
                        </span>
                      </div>
                      <p className="text-xs text-gray-500 mt-0.5">
                        Patient: {activeEmergency.patientId?.slice(-6)} | Hospital: {hospital?.name || 'Assigned'}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-bold ${
                        activeEmergency.severity === 'CRITICAL'
                          ? 'bg-red-100 text-red-800 border border-red-200'
                          : activeEmergency.severity === 'HIGH'
                          ? 'bg-orange-100 text-orange-800 border border-orange-200'
                          : 'bg-yellow-100 text-yellow-800 border border-yellow-200'
                      }`}
                    >
                      {activeEmergency.severity}
                    </span>
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800 border border-blue-200">
                      {activeEmergency.status}
                    </span>
                  </div>
                </div>

                {/* State Machine Transition Controls (Requirement 4, 10, 17) */}
                <div className="my-5">
                  <span className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
                    Emergency State Actions (State Machine Enforced)
                  </span>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
                    <button
                      onClick={() => handleTransition('GOING_TO_PATIENT')}
                      disabled={activeEmergency.status !== 'AMBULANCE_ASSIGNED'}
                      className="text-xs font-bold py-2.5 px-3 rounded-xl transition-all disabled:opacity-40 disabled:cursor-not-allowed bg-blue-600 hover:bg-blue-700 text-white shadow-sm"
                    >
                      1. Going to Patient
                    </button>
                    <button
                      onClick={() => handleTransition('ARRIVED_AT_PICKUP')}
                      disabled={activeEmergency.status !== 'GOING_TO_PATIENT'}
                      className="text-xs font-bold py-2.5 px-3 rounded-xl transition-all disabled:opacity-40 disabled:cursor-not-allowed bg-blue-600 hover:bg-blue-700 text-white shadow-sm"
                    >
                      2. Arrived at Pickup
                    </button>
                    <button
                      onClick={() => handleTransition('PATIENT_PICKED_UP')}
                      disabled={activeEmergency.status !== 'ARRIVED_AT_PICKUP'}
                      className="text-xs font-bold py-2.5 px-3 rounded-xl transition-all disabled:opacity-40 disabled:cursor-not-allowed bg-blue-600 hover:bg-blue-700 text-white shadow-sm"
                    >
                      3. Patient Picked Up
                    </button>
                    <button
                      onClick={() => handleTransition('GOING_TO_HOSPITAL')}
                      disabled={activeEmergency.status !== 'PATIENT_PICKED_UP'}
                      className="text-xs font-bold py-2.5 px-3 rounded-xl transition-all disabled:opacity-40 disabled:cursor-not-allowed bg-blue-600 hover:bg-blue-700 text-white shadow-sm"
                    >
                      4. Going to Hospital
                    </button>
                    <button
                      onClick={() => handleTransition('ARRIVED_AT_HOSPITAL')}
                      disabled={activeEmergency.status !== 'GOING_TO_HOSPITAL'}
                      className="col-span-2 sm:col-span-2 text-xs font-bold py-2.5 px-3 rounded-xl transition-all disabled:opacity-40 disabled:cursor-not-allowed bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm"
                    >
                      5. Arrived at Hospital
                    </button>
                  </div>
                </div>

                {/* Simulated Location Controller (Requirement 8) */}
                <div className="p-4 bg-amber-50 rounded-xl border border-amber-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div>
                    <span className="inline-block bg-amber-200 text-amber-900 text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-full mb-1">
                      Demo / Simulated Location Mode
                    </span>
                    <p className="text-xs text-amber-800">
                      Simulates live GPS coordinate movement along route every 3s.
                    </p>
                  </div>

                  <div>
                    {isSimulating ? (
                      <button
                        onClick={stopSimulation}
                        className="flex items-center gap-2 bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold px-4 py-2 rounded-lg shadow-sm transition-all"
                      >
                        <Square size={14} /> Stop Simulation
                      </button>
                    ) : (
                      <button
                        onClick={startSimulation}
                        disabled={connectionState !== 'CONNECTED'}
                        className="flex items-center gap-2 bg-amber-600 hover:bg-amber-700 disabled:opacity-50 text-white text-xs font-bold px-4 py-2 rounded-lg shadow-sm transition-all"
                      >
                        <Play size={14} /> Start Demo GPS Run
                      </button>
                    )}
                  </div>
                </div>
              </div>

              {/* Live Dispatch Map */}
              <LiveTrackingMap
                ambulanceLat={activeEmergency.currentAmbulanceLatitude || ambulance?.latitude}
                ambulanceLon={activeEmergency.currentAmbulanceLongitude || ambulance?.longitude}
                patientLat={activeEmergency.latitude}
                patientLon={activeEmergency.longitude}
                hospitalLat={hospital?.latitude}
                hospitalLon={hospital?.longitude}
                etaMinutes={activeEmergency.estimatedEtaMinutes}
                distanceKm={activeEmergency.estimatedDistanceKm}
                simulated={isSimulating}
                status={activeEmergency.status}
              />

              {/* Real-time Timeline */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2">
                  <Clock size={20} className="text-orange-500" /> Dispatch Timeline
                </h3>
                <EmergencyTimeline timeline={activeEmergency.timeline} />
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
              <div className="w-20 h-20 bg-emerald-50 text-emerald-600 rounded-3xl flex items-center justify-center mx-auto mb-4">
                <CheckCircle size={40} />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-1">No Active Emergency Dispatches</h3>
              <p className="text-sm text-gray-500 max-w-sm mx-auto">
                You are on standby. New emergency assignments dispatched by the LifeLink AI allocation engine will appear here in real time.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DriverDashboard;
