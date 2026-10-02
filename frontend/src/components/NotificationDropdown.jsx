import React, { useState, useRef, useEffect } from 'react';
import { useWebSocket } from '../context/WebSocketContext';
import { Bell, Check, CheckCheck, Clock, AlertTriangle, ShieldCheck, Truck, Hospital, Info } from 'lucide-react';

const NotificationDropdown = () => {
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useWebSocket();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getIcon = (type) => {
    switch (type) {
      case 'SOS_CREATED':
      case 'EMERGENCY_STATUS_UPDATED':
        return <AlertTriangle size={18} className="text-red-500 shrink-0 mt-0.5" />;
      case 'AMBULANCE_ASSIGNED':
      case 'AMBULANCE_APPROACHING':
      case 'AMBULANCE_ARRIVED':
      case 'PATIENT_PICKED_UP':
        return <Truck size={18} className="text-orange-500 shrink-0 mt-0.5" />;
      case 'HOSPITAL_ARRIVAL':
      case 'HOSPITAL_ACCEPTED':
        return <Hospital size={18} className="text-blue-500 shrink-0 mt-0.5" />;
      case 'EMERGENCY_COMPLETED':
        return <ShieldCheck size={18} className="text-green-500 shrink-0 mt-0.5" />;
      default:
        return <Info size={18} className="text-gray-500 shrink-0 mt-0.5" />;
    }
  };

  const formatTime = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    const now = new Date();
    const diffSec = Math.floor((now - date) / 1000);

    if (diffSec < 60) return 'Just now';
    if (diffSec < 3600) return `${Math.floor(diffSec / 60)}m ago`;
    if (diffSec < 86400) return `${Math.floor(diffSec / 3600)}h ago`;
    return date.toLocaleDateString();
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-gray-600 hover:text-blue-600 hover:bg-gray-100 rounded-full transition-colors focus:outline-none"
        title="Notifications"
      >
        <Bell size={22} />
        {unreadCount > 0 && (
          <span className="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none text-white transform bg-red-600 rounded-full animate-pulse">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-2xl shadow-2xl border border-gray-100 z-50 overflow-hidden">
          <div className="p-4 bg-gray-50 border-b border-gray-100 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="font-bold text-gray-900">Notifications</span>
              {unreadCount > 0 && (
                <span className="bg-red-100 text-red-700 text-xs px-2 py-0.5 rounded-full font-semibold">
                  {unreadCount} new
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                onClick={markAllAsRead}
                className="text-xs text-blue-600 hover:text-blue-800 font-medium flex items-center gap-1 transition-colors"
              >
                <CheckCheck size={14} /> Mark all read
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto divide-y divide-gray-100">
            {notifications.length === 0 ? (
              <div className="py-8 text-center text-gray-500 text-sm">
                No notifications yet.
              </div>
            ) : (
              notifications.map((notif) => (
                <div
                  key={notif.id}
                  onClick={() => !notif.read && markAsRead(notif.id)}
                  className={`p-4 flex gap-3 cursor-pointer transition-colors ${
                    notif.read ? 'bg-white hover:bg-gray-50' : 'bg-blue-50/50 hover:bg-blue-50'
                  }`}
                >
                  {getIcon(notif.type)}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-baseline justify-between gap-1 mb-1">
                      <p className={`text-sm ${notif.read ? 'font-medium text-gray-800' : 'font-bold text-gray-900'}`}>
                        {notif.title}
                      </p>
                      <span className="text-[11px] text-gray-400 shrink-0">
                        {formatTime(notif.createdAt)}
                      </span>
                    </div>
                    <p className="text-xs text-gray-600 leading-relaxed break-words">
                      {notif.message}
                    </p>
                    {notif.relatedEmergencyId && (
                      <span className="inline-block mt-1 font-mono text-[10px] text-blue-600 bg-blue-50 px-1.5 py-0.5 rounded border border-blue-100">
                        Ref: {notif.relatedEmergencyId.slice(-6)}
                      </span>
                    )}
                  </div>
                  {!notif.read && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        markAsRead(notif.id);
                      }}
                      className="text-gray-400 hover:text-blue-600 shrink-0 self-center p-1"
                      title="Mark as read"
                    >
                      <Check size={16} />
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationDropdown;
