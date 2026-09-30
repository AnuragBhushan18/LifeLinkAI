import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Truck, Navigation, Phone, Edit2, Save, MapPin } from 'lucide-react';
import api from '../../services/api';

const DriverDashboard = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({
    licenseNumber: '',
    experienceYears: 0,
    availabilityStatus: 'AVAILABLE'
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get(`/drivers/user/${user.id}`);
        if (res.data) setProfile(res.data);
      } catch (error) {
        console.error("Error fetching driver profile", error);
      } finally {
        setLoading(false);
      }
    };
    if (user?.id) fetchProfile();
  }, [user.id]);

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    try {
      await api.put(`/drivers/user/${user.id}`, profile);
      setIsEditing(false);
    } catch (error) {
      console.error("Error saving profile", error);
      alert("Failed to save profile");
    }
  };

  const updateStatus = async (status) => {
    try {
      await api.put(`/drivers/user/${user.id}/status`, { status });
      setProfile({ ...profile, availabilityStatus: status });
    } catch (error) {
      console.error("Error updating status", error);
    }
  };

  if (loading) return <div className="text-center py-10">Loading...</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Driver Dashboard</h1>
        <button 
          onClick={() => isEditing ? handleSave() : setIsEditing(true)}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md transition-colors"
        >
          {isEditing ? <><Save size={18} /> Save Details</> : <><Edit2 size={18} /> Edit Details</>}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center">
            <div className="w-24 h-24 bg-orange-100 text-orange-600 rounded-full flex items-center justify-center text-4xl font-bold mx-auto mb-4">
              <Truck size={40} />
            </div>
            <h3 className="text-2xl font-bold text-gray-900">{user?.name}</h3>
            <p className="text-gray-500 mb-6">Ambulance Driver</p>
            
            <div className="flex flex-col gap-3">
              <button 
                onClick={() => updateStatus('AVAILABLE')}
                className={`w-full font-medium py-2 rounded-lg transition-colors border ${profile.availabilityStatus === 'AVAILABLE' ? 'bg-green-100 border-green-200 text-green-800' : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'}`}
              >
                Available for Dispatch
              </button>
              <button 
                onClick={() => updateStatus('ON_MISSION')}
                className={`w-full font-medium py-2 rounded-lg transition-colors border ${profile.availabilityStatus === 'ON_MISSION' ? 'bg-blue-100 border-blue-200 text-blue-800' : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'}`}
              >
                On Mission
              </button>
              <button 
                onClick={() => updateStatus('OFF_DUTY')}
                className={`w-full font-medium py-2 rounded-lg transition-colors border ${profile.availabilityStatus === 'OFF_DUTY' ? 'bg-gray-200 border-gray-300 text-gray-800' : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'}`}
              >
                Off Duty
              </button>
            </div>
          </div>
        </div>

        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-gray-800">
              <MapPin className="text-orange-500" /> Driver Credentials
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">License Number</label>
                {isEditing ? (
                  <input type="text" name="licenseNumber" value={profile.licenseNumber || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.licenseNumber || 'Not provided'}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Experience (Years)</label>
                {isEditing ? (
                  <input type="number" name="experienceYears" value={profile.experienceYears || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.experienceYears || '0'} Years</p>
                )}
              </div>
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-500 mb-1">Phone Number</label>
                <p className="text-gray-900 font-medium flex items-center gap-2">
                  <Phone size={16} className="text-gray-400" /> {user?.phone || 'Not provided'}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-blue-50 rounded-2xl shadow-sm border border-blue-100 p-6 flex items-center justify-between">
            <div>
              <h3 className="text-lg font-bold text-blue-900 mb-1 flex items-center gap-2">
                <Navigation size={20} /> Active Dispatch
              </h3>
              <p className="text-blue-700 text-sm">No active emergencies assigned at the moment.</p>
            </div>
            <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center shadow-sm">
              <span className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DriverDashboard;
