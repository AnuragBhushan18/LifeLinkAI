import React from 'react';
import { Truck, MapPin, Building2, Navigation, Compass } from 'lucide-react';

const LiveTrackingMap = ({
  ambulanceLat,
  ambulanceLon,
  patientLat,
  patientLon,
  hospitalLat,
  hospitalLon,
  etaMinutes,
  distanceKm,
  simulated = false,
  status,
}) => {
  // Fallbacks if coordinates are partially null
  const defaultPatientLat = patientLat || 40.7306;
  const defaultPatientLon = patientLon || -73.9352;
  const defaultHospLat = hospitalLat || defaultPatientLat + 0.04;
  const defaultHospLon = hospitalLon || defaultPatientLon + 0.05;
  const currentAmbLat = ambulanceLat || defaultPatientLat - 0.02;
  const currentAmbLon = ambulanceLon || defaultPatientLon - 0.02;

  // Compute bounding box and relative percentage positions
  const lats = [defaultPatientLat, defaultHospLat, currentAmbLat];
  const lons = [defaultPatientLon, defaultHospLon, currentAmbLon];

  const minLat = Math.min(...lats) - 0.008;
  const maxLat = Math.max(...lats) + 0.008;
  const minLon = Math.min(...lons) - 0.008;
  const maxLon = Math.max(...lons) + 0.008;

  const latSpan = maxLat - minLat || 0.01;
  const lonSpan = maxLon - minLon || 0.01;

  const toPercent = (lat, lon) => {
    // Map latitude (y: bottom to top, so 100 - val%)
    // Map longitude (x: left to right)
    const x = Math.min(90, Math.max(10, ((lon - minLon) / lonSpan) * 80 + 10));
    const y = Math.min(85, Math.max(15, 100 - (((lat - minLat) / latSpan) * 70 + 15)));
    return { x, y };
  };

  const patientPos = toPercent(defaultPatientLat, defaultPatientLon);
  const hospPos = toPercent(defaultHospLat, defaultHospLon);
  const ambPos = toPercent(currentAmbLat, currentAmbLon);

  return (
    <div className="bg-slate-900 rounded-2xl p-5 shadow-lg border border-slate-800 text-white relative overflow-hidden">
      {/* Map Header */}
      <div className="flex flex-wrap items-center justify-between gap-3 mb-4 z-10 relative">
        <div className="flex items-center gap-2">
          <div className="p-1.5 bg-blue-500/20 text-blue-400 rounded-lg">
            <Navigation size={18} className="animate-pulse" />
          </div>
          <div>
            <h3 className="font-semibold text-sm text-slate-100 flex items-center gap-2">
              Ambulance Live Dispatch Radar
              {simulated && (
                <span className="bg-amber-500/20 text-amber-300 border border-amber-500/30 text-[11px] px-2 py-0.5 rounded-full font-medium">
                  Demo / Simulated Location
                </span>
              )}
            </h3>
            <p className="text-xs text-slate-400">
              Coordinates: {currentAmbLat.toFixed(4)}, {currentAmbLon.toFixed(4)}
            </p>
          </div>
        </div>

        {/* ETA & Distance Pill */}
        <div className="flex items-center gap-3 bg-slate-800/80 px-3 py-1.5 rounded-xl border border-slate-700/80">
          <div className="text-right">
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Estimated ETA</span>
            <span className="text-sm font-bold text-emerald-400">
              {etaMinutes !== undefined && etaMinutes !== null
                ? etaMinutes > 60
                  ? `~${Math.floor(etaMinutes / 60)}h ${Math.round(etaMinutes % 60)}m`
                  : `~${Math.round(etaMinutes)} mins`
                : 'Calculating...'}
            </span>
          </div>
          <div className="h-6 w-px bg-slate-700"></div>
          <div>
            <span className="text-[10px] text-slate-400 uppercase tracking-wider block">Remaining Dist</span>
            <span className="text-sm font-bold text-blue-400">
              {distanceKm !== undefined && distanceKm !== null ? `${distanceKm.toFixed(1)} km` : '--'}
            </span>
          </div>
        </div>
      </div>

      {/* Visual Canvas Area */}
      <div className="relative h-64 sm:h-72 w-full bg-slate-950/70 rounded-xl border border-slate-800/90 overflow-hidden">
        {/* Radar grid lines */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:2.5rem_2.5rem] opacity-30"></div>
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-blue-950/30 via-slate-950/60 to-slate-950 pointer-events-none"></div>

        {/* SVG Route Paths */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none">
          <defs>
            <linearGradient id="routeGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.8" />
              <stop offset="100%" stopColor="#10b981" stopOpacity="0.8" />
            </linearGradient>
          </defs>
          {/* Path from Ambulance to Patient */}
          <line
            x1={`${ambPos.x}%`}
            y1={`${ambPos.y}%`}
            x2={`${patientPos.x}%`}
            y2={`${patientPos.y}%`}
            stroke="#f59e0b"
            strokeWidth="2.5"
            strokeDasharray="6 4"
            className="animate-pulse"
          />
          {/* Path from Patient to Hospital */}
          <line
            x1={`${patientPos.x}%`}
            y1={`${patientPos.y}%`}
            x2={`${hospPos.x}%`}
            y2={`${hospPos.y}%`}
            stroke="url(#routeGradient)"
            strokeWidth="2"
            strokeDasharray="4 4"
            opacity="0.6"
          />
        </svg>

        {/* Patient Location Pin */}
        <div
          className="absolute -translate-x-1/2 -translate-y-1/2 flex flex-col items-center group transition-all duration-300"
          style={{ left: `${patientPos.x}%`, top: `${patientPos.y}%` }}
        >
          <div className="w-8 h-8 rounded-full bg-red-500/20 flex items-center justify-center border border-red-500 shadow-lg shadow-red-500/30">
            <MapPin size={16} className="text-red-400" />
          </div>
          <span className="text-[10px] font-bold text-red-300 mt-1 bg-slate-900/90 px-1.5 py-0.5 rounded border border-red-900/50 shadow whitespace-nowrap">
            Patient Pickup
          </span>
        </div>

        {/* Hospital Location Pin */}
        <div
          className="absolute -translate-x-1/2 -translate-y-1/2 flex flex-col items-center group transition-all duration-300"
          style={{ left: `${hospPos.x}%`, top: `${hospPos.y}%` }}
        >
          <div className="w-8 h-8 rounded-full bg-emerald-500/20 flex items-center justify-center border border-emerald-500 shadow-lg shadow-emerald-500/30">
            <Building2 size={16} className="text-emerald-400" />
          </div>
          <span className="text-[10px] font-bold text-emerald-300 mt-1 bg-slate-900/90 px-1.5 py-0.5 rounded border border-emerald-900/50 shadow whitespace-nowrap">
            Hospital Facility
          </span>
        </div>

        {/* Moving Ambulance Marker */}
        <div
          className="absolute -translate-x-1/2 -translate-y-1/2 flex flex-col items-center z-20 transition-all duration-1000 ease-out"
          style={{ left: `${ambPos.x}%`, top: `${ambPos.y}%` }}
        >
          <div className="relative">
            <div className="absolute -inset-2 bg-blue-500 rounded-full opacity-40 animate-ping"></div>
            <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center border-2 border-white shadow-xl shadow-blue-500/50">
              <Truck size={20} className="text-white" />
            </div>
          </div>
          <span className="text-[11px] font-extrabold text-blue-200 mt-1 bg-slate-900/95 px-2 py-0.5 rounded-full border border-blue-600/80 shadow-md whitespace-nowrap flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-blue-400 animate-pulse"></span>
            Ambulance
          </span>
        </div>

        {/* Compass indicator */}
        <div className="absolute bottom-3 right-3 p-1.5 bg-slate-900/80 rounded-lg border border-slate-800 text-slate-400 flex items-center gap-1 text-[11px]">
          <Compass size={14} className="text-blue-400" /> N
        </div>
      </div>

      {/* Map Legend */}
      <div className="flex flex-wrap items-center justify-between gap-2 mt-3 pt-3 border-t border-slate-800 text-xs text-slate-400">
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-blue-500"></span> Ambulance
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-red-500"></span> Pickup
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> Destination Hospital
          </span>
        </div>
        <span className="text-[11px] text-slate-400 italic">
          *ETA calculated based on average emergency vehicle transit speed
        </span>
      </div>
    </div>
  );
};

export default LiveTrackingMap;
