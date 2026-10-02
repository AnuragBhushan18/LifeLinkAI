import React from 'react';
import { useWebSocket } from '../context/WebSocketContext';
import { X, Bell, Truck, AlertTriangle } from 'lucide-react';

const ToastNotification = () => {
  const { latestToast, setLatestToast } = useWebSocket();

  if (!latestToast) return null;

  return (
    <div className="fixed bottom-5 right-5 z-50 max-w-sm w-full bg-white rounded-xl shadow-2xl border-l-4 border-blue-600 p-4 transform transition-all duration-300 animate-slide-up flex gap-3 items-start">
      <div className="p-2 bg-blue-100 text-blue-600 rounded-lg shrink-0">
        <Bell size={20} />
      </div>
      <div className="flex-1 min-w-0">
        <h4 className="text-sm font-bold text-gray-900">{latestToast.title}</h4>
        <p className="text-xs text-gray-600 mt-1 leading-snug">{latestToast.message}</p>
        {latestToast.relatedEmergencyId && (
          <span className="inline-block mt-1 font-mono text-[10px] text-gray-500">
            Emergency: #{latestToast.relatedEmergencyId.slice(-6)}
          </span>
        )}
      </div>
      <button
        onClick={() => setLatestToast(null)}
        className="text-gray-400 hover:text-gray-600 p-1 shrink-0"
      >
        <X size={16} />
      </button>
    </div>
  );
};

export default ToastNotification;
