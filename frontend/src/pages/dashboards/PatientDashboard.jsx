import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../context/WebSocketContext';
import {
  Activity,
  AlertCircle,
  Phone,
  Edit2,
  Save,
  HeartPulse,
  Truck,
  Building2,
  Clock,
  Navigation,
  ShieldCheck,
  XCircle,
} from 'lucide-react';
import api from '../../services/api';
import EmergencyModal from '../../components/EmergencyModal';
import { emergencyService } from '../../services/entityServices';
import emergencyRealtimeService from '../../services/emergencyRealtimeService';
import websocketService from '../../services/websocketService';
import LiveTrackingMap from '../../components/LiveTrackingMap';
import EmergencyTimeline from '../../components/EmergencyTimeline';
import ConnectionBadge from '../../components/ConnectionBadge';

const PatientDashboard = () => {
  const { user } = useAuth();
  const { connectionState } = useWebSocket();
  const [profile, setProfile] = useState({
    bloodGroup: '',
    allergies: '',
    medications: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);

  const [isEmergencyModalOpen, setIsEmergencyModalOpen] = useState(false);
  const [activeEmergency, setActiveEmergency] = useState(null);
  const [assignedAmbulance, setAssignedAmbulance] = useState(null);
  const [recommendedHospital, setRecommendedHospital] = useState(null);
  const [assignedDriver, setAssignedDriver] = useState(null);

  const fetchActiveEmergency = useCallback(async () => {
    try {
      const res = await emergencyService.getAll();
      if (res.data && res.data.length > 0) {
        const active = res.data.find(
          (e) => !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(e.status)
        );
        if (active) {
          setActiveEmergency(active);
          // Also fetch details for ambulance, hospital, driver
          if (active.assignedAmbulanceId) {
            api.get(`/ambulances/${active.assignedAmbulanceId}`)
              .then((r) => setAssignedAmbulance(r.data))
              .catch(() => {});
          }
          if (active.recommendedHospitalId) {
            api.get(`/hospitals/${active.recommendedHospitalId}`)
              .then((r) => setRecommendedHospital(r.data))
              .catch(() => {});
          }
          if (active.assignedDriverId) {
            api.get(`/drivers/${active.assignedDriverId}`)
              .then((r) => setAssignedDriver(r.data))
              .catch(() => {});
          }
        } else {
          setActiveEmergency(null);
        }
      } else {
        setActiveEmergency(null);
      }
    } catch (error) {
      console.error('Error fetching patient emergencies', error);
    }
  }, []);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get(`/patients/user/${user.id}`);
        if (res.data) setProfile(res.data);
      } catch (error) {
        console.error('Error fetching patient profile', error);
      }
    };

    const loadData = async () => {
      await Promise.all([fetchProfile(), fetchActiveEmergency()]);
      setLoading(false);
    };

    if (user?.id) loadData();
  }, [user.id, fetchActiveEmergency]);

  // Reconcile data on WebSocket reconnect
  useEffect(() => {
    const unbind = websocketService.onReconnect(() => {
      console.log('Reconnected: reconciling emergency state from REST');
      fetchActiveEmergency();
    });
    return () => unbind();
  }, [fetchActiveEmergency]);

  // Subscribe to real-time events for this patient and active emergency
  useEffect(() => {
    if (!user?.id) return;

    // 1. Subscribe to patient topic for new emergency alerts or general updates
    const unsubPatient = emergencyRealtimeService.subscribeToPatient(user.id, (event) => {
      console.log('[PatientDashboard] Patient event received:', event);
      if (event.emergencyId) {
        fetchActiveEmergency();
      }
    });

    // 2. Subscribe to specific emergency topic if active
    let unsubEmergency = () => {};
    if (activeEmergency?.id) {
      unsubEmergency = emergencyRealtimeService.subscribeToEmergency(activeEmergency.id, (event) => {
        console.log('[PatientDashboard] Emergency event received:', event);

        setActiveEmergency((prev) => {
          if (!prev || prev.id !== event.emergencyId) return prev;

          // If emergency completed or cancelled, update status
          if (['COMPLETED', 'CANCELLED', 'REJECTED'].includes(event.status)) {
            return {
              ...prev,
              status: event.status,
              timeline: event.timeline || prev.timeline,
            };
          }

          return {
            ...prev,
            status: event.status || prev.status,
            severity: event.severity || prev.severity,
            currentAmbulanceLatitude: event.latitude !== undefined ? event.latitude : prev.currentAmbulanceLatitude,
            currentAmbulanceLongitude: event.longitude !== undefined ? event.longitude : prev.currentAmbulanceLongitude,
            estimatedEtaMinutes: event.etaMinutes !== undefined ? event.etaMinutes : prev.estimatedEtaMinutes,
            estimatedDistanceKm: event.distanceKm !== undefined ? event.distanceKm : prev.estimatedDistanceKm,
            timeline: event.timeline && event.timeline.length > 0 ? event.timeline : prev.timeline,
          };
        });

        // If ambulance was newly assigned, fetch ambulance details
        if (event.ambulanceId && !assignedAmbulance) {
          api.get(`/ambulances/${event.ambulanceId}`).then((r) => setAssignedAmbulance(r.data)).catch(() => {});
        }
      });
    }

    return () => {
      unsubPatient();
      unsubEmergency();
    };
  }, [user?.id, activeEmergency?.id, assignedAmbulance, fetchActiveEmergency]);

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    try {
      await api.put(`/patients/user/${user.id}`, profile);
      setIsEditing(false);
    } catch (error) {
      console.error('Error saving profile', error);
      alert('Failed to save profile');
    }
  };

  const handleCancelEmergency = async () => {
    if (!activeEmergency) return;
    if (window.confirm('Are you sure you want to cancel this emergency request?')) {
      try {
        await emergencyService.cancel(activeEmergency.id);
        setActiveEmergency(null);
      } catch (e) {
        alert('Failed to cancel emergency request');
      }
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Patient Portal</h1>
          <p className="text-sm text-gray-500 mt-1">Real-time emergency coordination and medical profile</p>
        </div>
        <div className="flex items-center gap-3">
          <ConnectionBadge />
          <button
            onClick={() => (isEditing ? handleSave() : setIsEditing(true))}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl font-medium shadow-sm transition-all"
          >
            {isEditing ? (
              <>
                <Save size={18} /> Save Profile
              </>
            ) : (
              <>
                <Edit2 size={18} /> Edit Profile
              </>
            )}
          </button>
        </div>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Medical & Contact Info */}
        <div className="lg:col-span-1 space-y-6">
          {/* User Profile Card */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center">
            <div className="w-20 h-20 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center text-3xl font-bold mx-auto mb-4 shadow-inner">
              {user?.name?.charAt(0).toUpperCase()}
            </div>
            <h3 className="text-xl font-bold text-gray-900">{user?.name}</h3>
            <p className="text-sm text-gray-500 mb-3">{user?.email}</p>
            <span className="inline-block bg-blue-50 text-blue-700 text-xs px-3 py-1 rounded-full font-semibold border border-blue-100">
              Registered Patient
            </span>
          </div>

          {/* Medical Info Card */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-bold mb-4 flex items-center gap-2 text-gray-800">
              <Activity className="text-blue-500" size={20} /> Medical Details
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">Blood Group</label>
                {isEditing ? (
                  <input
                    type="text"
                    name="bloodGroup"
                    value={profile.bloodGroup || ''}
                    onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                ) : (
                  <p className="text-gray-900 font-semibold">{profile.bloodGroup || 'Not specified'}</p>
                )}
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">Known Allergies</label>
                {isEditing ? (
                  <input
                    type="text"
                    name="allergies"
                    value={profile.allergies || ''}
                    onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.allergies || 'None recorded'}</p>
                )}
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">Current Medications</label>
                {isEditing ? (
                  <textarea
                    name="medications"
                    value={profile.medications || ''}
                    onChange={handleChange}
                    rows="2"
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.medications || 'None recorded'}</p>
                )}
              </div>
            </div>
          </div>

          {/* Emergency Contact */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-bold mb-4 flex items-center gap-2 text-gray-800">
              <Phone className="text-emerald-500" size={20} /> Emergency Contact
            </h2>
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">Contact Name</label>
                {isEditing ? (
                  <input
                    type="text"
                    name="emergencyContactName"
                    value={profile.emergencyContactName || ''}
                    onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                ) : (
                  <p className="text-gray-900 font-semibold">{profile.emergencyContactName || 'Not specified'}</p>
                )}
              </div>
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-400 mb-1">Contact Phone</label>
                {isEditing ? (
                  <input
                    type="tel"
                    name="emergencyContactPhone"
                    value={profile.emergencyContactPhone || ''}
                    onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg p-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.emergencyContactPhone || 'Not specified'}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Live Emergency Tracking / SOS Button */}
        <div className="lg:col-span-2 space-y-6">
          {activeEmergency ? (
            <div className="space-y-6">
              {/* Active Emergency Status Card */}
              <div className="bg-white rounded-2xl shadow-sm border border-red-100 p-6">
                <div className="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-gray-100">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 bg-red-100 text-red-600 rounded-xl flex items-center justify-center animate-pulse">
                      <HeartPulse size={26} />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h2 className="text-xl font-extrabold text-gray-900">Active Emergency</h2>
                        <span className="font-mono text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                          #{activeEmergency.id.slice(-6)}
                        </span>
                      </div>
                      <p className="text-xs text-gray-500 mt-0.5">
                        Initiated {new Date(activeEmergency.createdAt).toLocaleTimeString()}
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
                      {activeEmergency.severity || 'ANALYZING'}
                    </span>
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800 border border-blue-200">
                      {activeEmergency.status}
                    </span>
                  </div>
                </div>

                {/* Key Dispatch Details */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 my-5">
                  <div className="bg-gray-50 p-3.5 rounded-xl border border-gray-100">
                    <span className="text-xs text-gray-500 font-medium flex items-center gap-1.5 mb-1">
                      <Truck size={14} className="text-blue-500" /> Ambulance
                    </span>
                    <p className="text-sm font-bold text-gray-900">
                      {assignedAmbulance?.vehicleNumber || activeEmergency.assignedAmbulanceId?.slice(-6) || 'Searching...'}
                    </p>
                    {assignedDriver?.phone && (
                      <p className="text-xs text-gray-500 mt-0.5">Driver: {assignedDriver.phone}</p>
                    )}
                  </div>

                  <div className="bg-gray-50 p-3.5 rounded-xl border border-gray-100">
                    <span className="text-xs text-gray-500 font-medium flex items-center gap-1.5 mb-1">
                      <Building2 size={14} className="text-emerald-500" /> Hospital
                    </span>
                    <p className="text-sm font-bold text-gray-900 truncate">
                      {recommendedHospital?.name || 'Recommending...'}
                    </p>
                    {recommendedHospital?.phone && (
                      <p className="text-xs text-gray-500 mt-0.5">{recommendedHospital.phone}</p>
                    )}
                  </div>

                  <div className="bg-gray-50 p-3.5 rounded-xl border border-gray-100">
                    <span className="text-xs text-gray-500 font-medium flex items-center gap-1.5 mb-1">
                      <Clock size={14} className="text-orange-500" /> Estimated ETA
                    </span>
                    <p className="text-sm font-bold text-emerald-600">
                      {activeEmergency.estimatedEtaMinutes !== undefined && activeEmergency.estimatedEtaMinutes !== null
                        ? activeEmergency.estimatedEtaMinutes > 60
                          ? `~${Math.floor(activeEmergency.estimatedEtaMinutes / 60)}h ${Math.round(activeEmergency.estimatedEtaMinutes % 60)}m`
                          : `~${Math.round(activeEmergency.estimatedEtaMinutes)} mins`
                        : 'Estimating...'}
                    </p>
                    <p className="text-xs text-gray-500 mt-0.5">
                      {activeEmergency.estimatedDistanceKm ? `${activeEmergency.estimatedDistanceKm} km away` : 'Calculating distance'}
                    </p>
                  </div>
                </div>

                {/* Cancel Action */}
                {!['ARRIVED_AT_HOSPITAL', 'ADMITTED', 'TREATMENT_IN_PROGRESS', 'COMPLETED'].includes(
                  activeEmergency.status
                ) && (
                  <button
                    onClick={handleCancelEmergency}
                    className="w-full text-center text-xs font-semibold text-red-600 hover:text-red-700 hover:bg-red-50 py-2 rounded-lg border border-red-200 transition-colors"
                  >
                    Cancel Emergency Request
                  </button>
                )}
              </div>

              {/* Live Tracking Map Component */}
              <LiveTrackingMap
                ambulanceLat={activeEmergency.currentAmbulanceLatitude || (assignedAmbulance?.latitude)}
                ambulanceLon={activeEmergency.currentAmbulanceLongitude || (assignedAmbulance?.longitude)}
                patientLat={activeEmergency.latitude}
                patientLon={activeEmergency.longitude}
                hospitalLat={recommendedHospital?.latitude}
                hospitalLon={recommendedHospital?.longitude}
                etaMinutes={activeEmergency.estimatedEtaMinutes}
                distanceKm={activeEmergency.estimatedDistanceKm}
                simulated={true}
                status={activeEmergency.status}
              />

              {/* Emergency Timeline Card */}
              <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2">
                  <Clock size={20} className="text-blue-500" /> Emergency Timeline
                </h3>
                <EmergencyTimeline timeline={activeEmergency.timeline} />
              </div>
            </div>
          ) : (
            /* SOS Activation State */
            <div className="bg-gradient-to-br from-red-500 to-rose-600 rounded-3xl shadow-xl p-8 sm:p-12 text-white text-center relative overflow-hidden">
              <div className="absolute top-0 right-0 -mt-10 -mr-10 w-48 h-48 bg-white/10 rounded-full blur-2xl pointer-events-none"></div>
              <div className="w-24 h-24 bg-white/20 backdrop-blur rounded-3xl flex items-center justify-center mx-auto mb-6 shadow-2xl">
                <HeartPulse size={48} className="animate-pulse" />
              </div>
              <h2 className="text-3xl sm:text-4xl font-black tracking-tight mb-3">
                Emergency SOS System
              </h2>
              <p className="text-red-100 text-sm sm:text-base max-w-md mx-auto mb-8 leading-relaxed">
                Trigger immediate real-time response. Our intelligent engine will evaluate severity, dispatch the closest available ambulance, and alert the nearest emergency trauma center.
              </p>
              <button
                onClick={() => setIsEmergencyModalOpen(true)}
                className="bg-white text-red-600 hover:bg-red-50 font-black text-lg px-8 py-4 rounded-2xl shadow-2xl transition-all transform active:scale-95 inline-flex items-center gap-3"
              >
                <HeartPulse size={24} /> ACTIVATE SOS NOW
              </button>
            </div>
          )}
        </div>
      </div>

      <EmergencyModal
        isOpen={isEmergencyModalOpen}
        onClose={() => setIsEmergencyModalOpen(false)}
        onCreated={(emergency) => {
          setActiveEmergency(emergency);
          fetchActiveEmergency();
        }}
      />
    </div>
  );
};

export default PatientDashboard;
