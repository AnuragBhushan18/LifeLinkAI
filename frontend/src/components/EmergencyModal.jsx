import React, { useState } from 'react';
import { HeartPulse, X, MapPin } from 'lucide-react';
import { emergencyService } from '../services/entityServices';

const EmergencyModal = ({ isOpen, onClose, onCreated }) => {
  const [symptoms, setSymptoms] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [locationError, setLocationError] = useState('');
  
  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setLocationError('');
    
    if (!navigator.geolocation) {
      setLocationError('Geolocation is not supported by your browser');
      setLoading(false);
      return;
    }
    
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const reqData = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            symptoms: symptoms.split(',').map(s => s.trim()).filter(s => s),
            emergencyDescription: description
          };
          
          const res = await emergencyService.create(reqData);
          onCreated(res.data);
          onClose();
        } catch (error) {
          console.error("Error creating emergency", error);
          alert("Failed to create emergency");
        } finally {
          setLoading(false);
        }
      },
      (error) => {
        setLocationError('Unable to retrieve your location. Location access is required for emergencies.');
        setLoading(false);
      }
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
