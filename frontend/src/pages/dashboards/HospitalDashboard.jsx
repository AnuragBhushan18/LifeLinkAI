import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../context/WebSocketContext';
import {
  Building2,
  Activity,
  Edit2,
  Save,
  Users,
  Truck,
  AlertCircle,
  Clock,
  Navigation,
  CheckCircle,
  Eye,
} from 'lucide-react';
import api from '../../services/api';
import { emergencyService } from '../../services/entityServices';
import emergencyRealtimeService from '../../services/emergencyRealtimeService';
import websocketService from '../../services/websocketService';
import LiveTrackingMap from '../../components/LiveTrackingMap';
import EmergencyTimeline from '../../components/EmergencyTimeline';
import ConnectionBadge from '../../components/ConnectionBadge';

const HospitalDashboard = () => {
  const { user } = useAuth();
  const { connectionState } = useWebSocket();
  const [profile, setProfile] = useState({
    hospitalName: '',
    address: '',
    totalBeds: 0,
    availableBeds: 0,
    totalIcuBeds: 0,
    availableIcuBeds: 0,
    operationalStatus: 'OPERATIONAL',
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [emergencies, setEmergencies] = useState([]);
  const [selectedEmergency, setSelectedEmergency] = useState(null);

  const fetchHospitalProfile = useCallback(async () => {
    try {
      const res = await api.get(`/hospitals/user/${user.id}`);
      if (res.data) setProfile(res.data);
    } catch (error) {
      console.error('Error fetching hospital profile', error);
    }
  }, [user.id]);

  const fetchEmergencies = useCallback(async () => {
    try {
      const res = await emergencyService.getAll();
      if (res.data) {
        setEmergencies(res.data);
        // If an emergency is selected, update it
        setSelectedEmergency((current) => {
          if (!current) {
            // Default select first active emergency if any
            const active = res.data.find(
              (e) => !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(e.status)
            );
            return active || res.data[0] || null;
          }
          const updated = res.data.find((e) => e.id === current.id);
          return updated || current;
        });
      }
    } catch (error) {
      console.error('Error fetching hospital emergencies', error);
    }
  }, []);

  useEffect(() => {
    const loadData = async () => {
      await Promise.all([fetchHospitalProfile(), fetchEmergencies()]);
      setLoading(false);
    };

    if (user?.id) loadData();
  }, [user.id, fetchHospitalProfile, fetchEmergencies]);

  // Reconnect reconciliation via REST
  useEffect(() => {
    const unbind = websocketService.onReconnect(() => {
      console.log('Hospital reconnected: refreshing emergencies from REST');
      fetchEmergencies();
    });
    return () => unbind();
  }, [fetchEmergencies]);

  // WebSocket subscriptions for live real-time updates
  useEffect(() => {
    if (!user?.id) return;

    // 1. Subscribe to Hospital topic for new assigned emergencies or status updates
    const unsubHospital = emergencyRealtimeService.subscribeToHospital(user.id, (event) => {
      console.log('[HospitalDashboard] Real-time event received:', event);
      fetchEmergencies();
    });

    // 2. Also subscribe to selected emergency for live GPS coordinate updates & timeline
    let unsubEmergency = () => {};
    if (selectedEmergency?.id) {
      unsubEmergency = emergencyRealtimeService.subscribeToEmergency(selectedEmergency.id, (event) => {
        setEmergencies((prev) =>
          prev.map((em) => {
            if (em.id === event.emergencyId) {
              return {
                ...em,
                status: event.status || em.status,
                currentAmbulanceLatitude: event.latitude !== undefined ? event.latitude : em.currentAmbulanceLatitude,
                currentAmbulanceLongitude: event.longitude !== undefined ? event.longitude : em.currentAmbulanceLongitude,
                estimatedEtaMinutes: event.etaMinutes !== undefined ? event.etaMinutes : em.estimatedEtaMinutes,
                estimatedDistanceKm: event.distanceKm !== undefined ? event.distanceKm : em.estimatedDistanceKm,
                timeline: event.timeline && event.timeline.length > 0 ? event.timeline : em.timeline,
              };
            }
            return em;
          })
        );

        setSelectedEmergency((prev) => {
          if (!prev || prev.id !== event.emergencyId) return prev;
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
      unsubHospital();
      unsubEmergency();
    };
  }, [user?.id, selectedEmergency?.id, fetchEmergencies]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    const numFields = ['totalBeds', 'availableBeds', 'totalIcuBeds', 'availableIcuBeds'];
    setProfile({
      ...profile,
      [name]: numFields.includes(name) ? Math.max(0, parseInt(value) || 0) : value,
    });
  };

  const handleSave = async () => {
    try {
      await api.put(`/hospitals/user/${user.id}`, profile);
      setIsEditing(false);
    } catch (error) {
      console.error('Error saving profile', error);
      alert('Failed to save profile');
    }
  };

  const handleUpdateStatus = async (emergencyId, status) => {
    try {
      await emergencyService.updateStatus(emergencyId, status);
      fetchEmergencies();
    } catch (err) {
      console.error('Failed to update status:', err);
      alert(err.response?.data?.message || 'Failed to update emergency status');
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
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Hospital Emergency Command</h1>
          <p className="text-sm text-gray-500 mt-1">Real-time incoming ambulance tracking & bed coordination</p>
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
                <Edit2 size={18} /> Update Facility
              </>
            )}
          </button>
        </div>
      </div>

      {/* Bed Tracking Overview */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
          <span className="block text-xs font-bold uppercase text-gray-400 mb-1">Total Beds</span>
          {isEditing ? (
            <input
              type="number"
              name="totalBeds"
              value={profile.totalBeds}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg p-1.5 text-lg font-bold"
            />
          ) : (
            <p className="text-3xl font-black text-gray-900">{profile.totalBeds}</p>
          )}
        </div>
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
          <span className="block text-xs font-bold uppercase text-emerald-600 mb-1">Available Beds</span>
          {isEditing ? (
            <input
              type="number"
              name="availableBeds"
              value={profile.availableBeds}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg p-1.5 text-lg font-bold"
            />
          ) : (
            <p className="text-3xl font-black text-emerald-600">{profile.availableBeds}</p>
          )}
        </div>
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
          <span className="block text-xs font-bold uppercase text-purple-600 mb-1">Total ICU</span>
          {isEditing ? (
            <input
              type="number"
              name="totalIcuBeds"
              value={profile.totalIcuBeds}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg p-1.5 text-lg font-bold"
            />
          ) : (
            <p className="text-3xl font-black text-purple-900">{profile.totalIcuBeds}</p>
          )}
        </div>
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
          <span className="block text-xs font-bold uppercase text-orange-600 mb-1">Available ICU</span>
          {isEditing ? (
            <input
              type="number"
              name="availableIcuBeds"
              value={profile.availableIcuBeds}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg p-1.5 text-lg font-bold"
            />
          ) : (
            <p className="text-3xl font-black text-orange-600">{profile.availableIcuBeds}</p>
          )}
        </div>
      </div>

      {/* Main Content Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left 2 Cols: Emergency Queue & Live Tracking */}
        <div className="lg:col-span-2 space-y-6">
          {/* Live Tracking Map for Selected Emergency (Requirement 11) */}
          {selectedEmergency && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                  <Navigation className="text-blue-500" size={22} />
                  Incoming Ambulance Radar: #{selectedEmergency.id.slice(-6)}
                </h2>
                <div className="flex items-center gap-2">
                  <span
                    className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                      selectedEmergency.severity === 'CRITICAL'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-orange-100 text-orange-800'
                    }`}
                  >
                    {selectedEmergency.severity}
                  </span>
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-800">
                    {selectedEmergency.status}
                  </span>
                </div>
              </div>

              <LiveTrackingMap
                ambulanceLat={selectedEmergency.currentAmbulanceLatitude}
                ambulanceLon={selectedEmergency.currentAmbulanceLongitude}
                patientLat={selectedEmergency.latitude}
                patientLon={selectedEmergency.longitude}
                hospitalLat={profile.latitude}
                hospitalLon={profile.longitude}
                etaMinutes={selectedEmergency.estimatedEtaMinutes}
                distanceKm={selectedEmergency.estimatedDistanceKm}
                status={selectedEmergency.status}
              />
            </div>
          )}

          {/* Emergency Queue Table */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 overflow-hidden">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                <AlertCircle className="text-red-500" size={22} /> Real-Time Emergency Queue
              </h2>
              <span className="text-xs text-gray-500">Live STOMP Sync Active</span>
            </div>

            {emergencies.length === 0 ? (
              <div className="text-center py-12 text-gray-400">
                No active emergencies assigned to this hospital at this moment.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-gray-100 text-xs font-bold uppercase text-gray-400">
                      <th className="py-3 px-3">ID</th>
                      <th className="py-3 px-3">Severity</th>
                      <th className="py-3 px-3">Status</th>
                      <th className="py-3 px-3">ETA</th>
                      <th className="py-3 px-3">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 text-sm">
                    {emergencies.map((em) => {
                      const isSelected = selectedEmergency?.id === em.id;
                      return (
                        <tr
                          key={em.id}
                          onClick={() => setSelectedEmergency(em)}
                          className={`cursor-pointer transition-colors ${
                            isSelected ? 'bg-blue-50/70' : 'hover:bg-gray-50'
                          }`}
                        >
                          <td className="py-3 px-3 font-mono font-bold text-gray-800">
                            #{em.id.slice(-6)}
                          </td>
                          <td className="py-3 px-3">
                            <span
                              className={`px-2 py-0.5 rounded text-xs font-extrabold ${
                                em.severity === 'CRITICAL'
                                  ? 'bg-red-100 text-red-800'
                                  : em.severity === 'HIGH'
                                  ? 'bg-orange-100 text-orange-800'
                                  : 'bg-yellow-100 text-yellow-800'
                              }`}
                            >
                              {em.severity}
                            </span>
                          </td>
                          <td className="py-3 px-3 font-medium text-xs text-gray-700">{em.status}</td>
                          <td className="py-3 px-3 text-xs font-bold text-emerald-600">
                            {em.estimatedEtaMinutes !== undefined && em.estimatedEtaMinutes !== null
                              ? `~${Math.round(em.estimatedEtaMinutes)}m`
                              : '--'}
                          </td>
                          <td className="py-3 px-3" onClick={(e) => e.stopPropagation()}>
                            <div className="flex items-center gap-1.5">
                              {em.status === 'ARRIVED_AT_HOSPITAL' && (
                                <button
                                  onClick={() => handleUpdateStatus(em.id, 'ADMITTED')}
                                  className="bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm"
                                >
                                  Admit
                                </button>
                              )}
                              {em.status === 'ADMITTED' && (
                                <button
                                  onClick={() => handleUpdateStatus(em.id, 'TREATMENT_IN_PROGRESS')}
                                  className="bg-purple-600 hover:bg-purple-700 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm"
                                >
                                  Start Tx
                                </button>
                              )}
                              {em.status === 'TREATMENT_IN_PROGRESS' && (
                                <button
                                  onClick={() => handleUpdateStatus(em.id, 'COMPLETED')}
                                  className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow-sm"
                                >
                                  Complete
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Right Col: Selected Emergency Timeline & Details */}
        <div className="lg:col-span-1 space-y-6">
          {selectedEmergency ? (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Clock size={20} className="text-blue-500" /> Case Timeline
              </h3>
              <div className="mb-4 p-3 bg-gray-50 rounded-xl border border-gray-100 text-xs space-y-1">
                <p>
                  <strong className="text-gray-700">Patient:</strong> {selectedEmergency.patientId}
                </p>
                <p>
                  <strong className="text-gray-700">Symptoms:</strong>{' '}
                  {selectedEmergency.symptoms?.join(', ') || 'N/A'}
                </p>
                {selectedEmergency.emergencyDescription && (
                  <p>
                    <strong className="text-gray-700">Note:</strong>{' '}
                    {selectedEmergency.emergencyDescription}
                  </p>
                )}
              </div>
              <EmergencyTimeline timeline={selectedEmergency.timeline} />
            </div>
          ) : (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center text-gray-400 text-sm">
              Select an emergency from the queue to view real-time timeline details.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default HospitalDashboard;
