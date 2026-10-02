import websocketService from './websocketService';
import api from './api';

export const emergencyRealtimeService = {
  subscribeToEmergency: (emergencyId, onEvent) => {
    if (!emergencyId) return () => {};
    return websocketService.subscribe(`/topic/emergency/${emergencyId}`, onEvent);
  },

  subscribeToPatient: (patientId, onEvent) => {
    if (!patientId) return () => {};
    return websocketService.subscribe(`/topic/patient/${patientId}`, onEvent);
  },

  subscribeToDriver: (driverId, onEvent) => {
    if (!driverId) return () => {};
    return websocketService.subscribe(`/topic/driver/${driverId}`, onEvent);
  },

  subscribeToHospital: (hospitalId, onEvent) => {
    if (!hospitalId) return () => {};
    return websocketService.subscribe(`/topic/hospital/${hospitalId}`, onEvent);
  },

  subscribeToAdmin: (onEvent) => {
    return websocketService.subscribe('/topic/admin/emergencies', onEvent);
  },

  sendLocationUpdate: async (latitude, longitude, simulated = false) => {
    const payload = {
      latitude,
      longitude,
      simulated,
      timestamp: new Date().toISOString(),
    };

    // Attempt STOMP message first
    const sent = websocketService.send('/app/ambulance/location', payload);
    if (!sent) {
      // Fallback to REST API if WebSocket is temporarily disconnected
      try {
        await api.post('/ambulances/location', payload);
      } catch (err) {
        console.error('Failed to post location via REST fallback:', err);
      }
    }
  },

  sendStatusUpdate: async (emergencyId, status) => {
    const payload = { emergencyId, status };
    const sent = websocketService.send('/app/emergency/status', payload);
    if (!sent) {
      await api.patch(`/emergencies/${emergencyId}/status?status=${status}`);
    }
  },
};

export default emergencyRealtimeService;
