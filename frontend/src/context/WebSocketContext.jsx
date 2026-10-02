import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useAuth } from './AuthContext';
import websocketService, { ConnectionState } from '../services/websocketService';
import notificationService from '../services/notificationService';

const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
  const { user } = useAuth();
  const [connectionState, setConnectionState] = useState(ConnectionState.DISCONNECTED);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [latestToast, setLatestToast] = useState(null);

  const fetchNotifications = useCallback(async () => {
    if (!user) return;
    try {
      const [resList, resCount] = await Promise.all([
        notificationService.getNotifications(),
        notificationService.getUnreadCount(),
      ]);
      if (resList.data) setNotifications(resList.data);
      if (resCount.data) setUnreadCount(resCount.data.unreadCount || 0);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
    }
  }, [user]);

  // Handle connection lifecycle
  useEffect(() => {
    if (user?.id) {
      websocketService.connect();

      const unsubscribeState = websocketService.onStateChange((state) => {
        setConnectionState(state);
      });

      const unsubscribeReconnect = websocketService.onReconnect(() => {
        fetchNotifications();
      });

      return () => {
        unsubscribeState();
        unsubscribeReconnect();
        websocketService.disconnect();
      };
    } else {
      websocketService.disconnect();
      setNotifications([]);
      setUnreadCount(0);
    }
  }, [user?.id, fetchNotifications]);

  // Load initial notifications & subscribe to live user notifications
  useEffect(() => {
    if (!user?.id) return;

    fetchNotifications();

    const unsubscribe = notificationService.subscribeToUserNotifications(user.id, (notif) => {
      setNotifications((prev) => [notif, ...prev]);
      setUnreadCount((prev) => prev + 1);
      setLatestToast(notif);

      // Auto-clear toast after 5 seconds
      setTimeout(() => {
        setLatestToast((current) => (current?.id === notif.id ? null : current));
      }, 5000);
    });

    return () => {
      unsubscribe();
    };
  }, [user?.id, fetchNotifications]);

  const markAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all as read:', err);
    }
  };

  return (
    <WebSocketContext.Provider
      value={{
        connectionState,
        notifications,
        unreadCount,
        latestToast,
        setLatestToast,
        markAsRead,
        markAllAsRead,
        refreshNotifications: fetchNotifications,
      }}
    >
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => useContext(WebSocketContext);
export default WebSocketContext;
