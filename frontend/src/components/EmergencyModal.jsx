import React, { useState } from 'react';
import { HeartPulse, X, MapPin } from 'lucide-react';
import { emergencyService } from '../services/entityServices';

const EmergencyModal = ({ isOpen, onClose, onCreated }) => {
  const [symptoms, setSymptoms] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [locationError, setLocationError] = useState('');
  const [locationMode, setLocationMode] = useState('demo'); // 'demo' | 'device'
  
  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setLocationError('');
    
    const submitWithCoords = async (latitude, longitude) => {
      try {
        const reqData = {
          latitude,
          longitude,
          symptoms: symptoms.split(',').map(s => s.trim()).filter(s => s),
          emergencyDescription: description
        };
        
        const res = await emergencyService.create(reqData);
        onCreated(res.data);
        onClose();
      } catch (error) {
        console.error("Error creating emergency", error);
        alert(error.response?.data?.message || "Failed to create emergency");
      } finally {
        setLoading(false);
      }
    };

    if (locationMode === 'demo') {
      // Generate realistic coordinates near the demo fleet coverage zone (NYC)
      // Produces realistic 1.0 - 2.5 km distance and ~2 - 5 min ETA
      const jitterLat = 40.7180 + (Math.random() - 0.5) * 0.012;
      const jitterLon = -74.0040 + (Math.random() - 0.5) * 0.012;
      submitWithCoords(Math.round(jitterLat * 10000) / 10000, Math.round(jitterLon * 10000) / 10000);
      return;
    }

    if (!navigator.geolocation) {
      submitWithCoords(40.7128, -74.0060);
      return;
    }
    
    navigator.geolocation.getCurrentPosition(
      (position) => {
        submitWithCoords(position.coords.latitude, position.coords.longitude);
      },
      (error) => {
        console.warn('Geolocation unavailable/denied, falling back to default coordinates', error);
        submitWithCoords(40.7128, -74.0060);
      },
      { timeout: 5000 }
    );
  };

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative">
        <button onClick={onClose} className="absolute top-4 right-4 text-gray-500 hover:text-gray-800">
          <X size={24} />
        </button>
        
        <div className="text-center mb-6">
          <div className="w-16 h-16 bg-red-100 text-red-600 rounded-full flex items-center justify-center mx-auto mb-3">
            <HeartPulse size={32} />
          </div>
          <h2 className="text-2xl font-bold text-gray-900">Emergency SOS</h2>
          <p className="text-red-600 font-medium">Please provide details to help us assist you better.</p>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Symptoms (comma separated)</label>
            <input 
              type="text" 
              placeholder="e.g., chest pain, difficulty breathing" 
              value={symptoms}
              onChange={(e) => setSymptoms(e.target.value)}
              className="w-full border border-gray-300 rounded-md p-2 focus:ring-red-500 focus:border-red-500"
              required
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Additional Description</label>
            <textarea 
              rows="3" 
              placeholder="Describe the situation briefly"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full border border-gray-300 rounded-md p-2 focus:ring-red-500 focus:border-red-500"
            ></textarea>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Pickup Location Mode</label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => setLocationMode('demo')}
                className={`p-2.5 rounded-xl border text-left transition-all ${
                  locationMode === 'demo'
                    ? 'border-red-500 bg-red-50/60 ring-1 ring-red-500/50'
                    : 'border-gray-200 hover:border-gray-300 bg-white'
                }`}
              >
                <div className="flex items-center gap-1.5 mb-1">
                  <MapPin size={15} className={locationMode === 'demo' ? 'text-red-600' : 'text-gray-400'} />
                  <span className={`text-xs font-bold ${locationMode === 'demo' ? 'text-red-900' : 'text-gray-700'}`}>
                    Demo Fleet Area
                  </span>
                </div>
                <p className="text-[11px] text-gray-500 leading-tight">
                  NYC Service Zone (~2–5 min ETA)
                </p>
              </button>

              <button
                type="button"
                onClick={() => setLocationMode('device')}
                className={`p-2.5 rounded-xl border text-left transition-all ${
                  locationMode === 'device'
                    ? 'border-red-500 bg-red-50/60 ring-1 ring-red-500/50'
                    : 'border-gray-200 hover:border-gray-300 bg-white'
                }`}
              >
                <div className="flex items-center gap-1.5 mb-1">
                  <MapPin size={15} className={locationMode === 'device' ? 'text-red-600' : 'text-gray-400'} />
                  <span className={`text-xs font-bold ${locationMode === 'device' ? 'text-red-900' : 'text-gray-700'}`}>
                    Device GPS
                  </span>
                </div>
                <p className="text-[11px] text-gray-500 leading-tight">
                  Real Browser Geolocation
                </p>
              </button>
            </div>
            {locationMode === 'device' && (
              <p className="text-[11px] text-amber-600 mt-1.5 bg-amber-50 p-2 rounded-lg border border-amber-200">
                Notice: The demo hospital and ambulance fleet are in New York. If your device is outside NYC, distance will reflect your true distance.
              </p>
            )}
          </div>

          {locationError && (
            <div className="p-3 bg-red-50 text-red-700 text-sm rounded-md flex items-start gap-2">
              <MapPin size={16} className="mt-0.5 shrink-0" />
              <p>{locationError}</p>
            </div>
          )}
          
          <button 
            type="submit" 
            disabled={loading}
            className="w-full bg-red-600 hover:bg-red-700 text-white py-3 rounded-lg font-bold text-lg shadow-md transition-all disabled:opacity-70 disabled:cursor-not-allowed flex justify-center items-center gap-2"
          >
            {loading ? 'PROCESSING...' : 'CONFIRM SOS'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default EmergencyModal;
