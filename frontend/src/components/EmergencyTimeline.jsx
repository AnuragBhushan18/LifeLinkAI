import React from 'react';
import { Clock, CheckCircle2, CircleDot } from 'lucide-react';

const EmergencyTimeline = ({ timeline = [] }) => {
  if (!timeline || timeline.length === 0) {
    return (
      <div className="text-center py-6 text-gray-400 text-sm">
        No timeline events recorded yet.
      </div>
    );
  }

  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  return (
    <div className="flow-root">
      <ul className="-mb-8">
        {timeline.map((event, eventIdx) => {
          const isLast = eventIdx === timeline.length - 1;
          return (
            <li key={event.id || eventIdx}>
              <div className="relative pb-8">
                {!isLast && (
                  <span
                    className="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200"
                    aria-hidden="true"
                  />
                )}
                <div className="relative flex items-start space-x-3">
                  <div>
                    {isLast ? (
                      <span className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center ring-8 ring-white text-blue-600 animate-pulse">
                        <CircleDot size={18} />
                      </span>
                    ) : (
                      <span className="h-8 w-8 rounded-full bg-green-100 flex items-center justify-center ring-8 ring-white text-green-600">
                        <CheckCircle2 size={18} />
                      </span>
                    )}
                  </div>
                  <div className="min-w-0 flex-1 pt-1.5 flex justify-between space-x-4">
                    <div>
                      <p className="text-sm font-bold text-gray-900">{event.title}</p>
                      <p className="text-xs text-gray-500 mt-0.5 leading-relaxed">{event.description}</p>
                    </div>
                    <div className="text-right text-xs whitespace-nowrap text-gray-400 flex items-center gap-1">
                      <Clock size={12} />
                      <time dateTime={event.timestamp}>{formatTime(event.timestamp)}</time>
                    </div>
                  </div>
                </div>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export default EmergencyTimeline;
