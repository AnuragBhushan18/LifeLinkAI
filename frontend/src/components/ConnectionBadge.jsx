import React from 'react';
import { useWebSocket } from '../context/WebSocketContext';
import { Wifi, WifiOff } from 'lucide-react';

const ConnectionBadge = () => {
  const { connectionState } = useWebSocket();

  switch (connectionState) {
    case 'CONNECTED':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          Live
        </span>
      );
    case 'CONNECTING':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
          <span className="w-2 h-2 rounded-full bg-amber-500 animate-ping"></span>
          Connecting
        </span>
      );
    case 'RECONNECTING':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-orange-50 text-orange-700 border border-orange-200">
          <span className="w-2 h-2 rounded-full bg-orange-500 animate-ping"></span>
          Reconnecting...
        </span>
      );
    case 'DISCONNECTED':
    default:
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200">
          <WifiOff size={12} />
          Offline
        </span>
      );
  }
};

export default ConnectionBadge;
