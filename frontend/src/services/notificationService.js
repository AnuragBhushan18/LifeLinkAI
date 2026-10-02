import api from './api';
import websocketService from './websocketService';

export const notificationService = {
  getNotifications: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAsRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllAsRead: () => api.patch('/notifications/read-all'),

  subscribeToUserNotifications: (userId, onNotification) => {
    if (!userId) return () => {};
    const topic = `/topic/user/${userId}/notifications`;
    return websocketService.subscribe(topic, onNotification);
  },
};

export default notificationService;
