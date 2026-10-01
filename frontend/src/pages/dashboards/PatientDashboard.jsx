import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { User, Activity, AlertCircle, Phone, Edit2, Save, ShieldAlert, HeartPulse, Clock, MapPin } from 'lucide-react';
import api from '../../services/api';
import EmergencyModal from '../../components/EmergencyModal';
import { emergencyService } from '../../services/entityServices';

const PatientDashboard = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({
    bloodGroup: '',
    allergies: '',
    medications: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  
  const [isEmergencyModalOpen, setIsEmergencyModalOpen] = useState(false);
  const [activeEmergency, setActiveEmergency] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get(`/patients/user/${user.id}`);
        if (res.data) setProfile(res.data);
      } catch (error) {
        console.error("Error fetching patient profile", error);
      }
    };
    
    const fetchEmergencies = async () => {
      try {
        const res = await emergencyService.getAll();
        if (res.data && res.data.length > 0) {
          // Find the most recent active emergency
          const active = res.data.find(e => !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(e.status));
          if (active) setActiveEmergency(active);
        }
      } catch (error) {
        console.error("Error fetching emergencies", error);
      }
    };

    const loadData = async () => {
      await Promise.all([fetchProfile(), fetchEmergencies()]);
      setLoading(false);
    };

    if (user?.id) loadData();
  }, [user.id]);

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    try {
      await api.put(`/patients/user/${user.id}`, profile);
      setIsEditing(false);
    } catch (error) {
      console.error("Error saving profile", error);
      alert("Failed to save profile");
    }
  };

  if (loading) return <div className="text-center py-10">Loading...</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Patient Dashboard</h1>
        <button 
          onClick={() => isEditing ? handleSave() : setIsEditing(true)}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md transition-colors"
        >
          {isEditing ? <><Save size={18} /> Save Profile</> : <><Edit2 size={18} /> Edit Profile</>}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-gray-800">
              <Activity className="text-blue-500" /> Medical Information
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Blood Group</label>
                {isEditing ? (
                  <input type="text" name="bloodGroup" value={profile.bloodGroup || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.bloodGroup || 'Not specified'}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Allergies</label>
                {isEditing ? (
                  <input type="text" name="allergies" value={profile.allergies || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.allergies || 'None'}</p>
                )}
              </div>
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-500 mb-1">Current Medications</label>
                {isEditing ? (
                  <textarea name="medications" value={profile.medications || ''} onChange={handleChange} rows="2" className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.medications || 'None'}</p>
                )}
              </div>
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-gray-800">
              <Phone className="text-green-500" /> Emergency Contact
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Contact Name</label>
                {isEditing ? (
                  <input type="text" name="emergencyContactName" value={profile.emergencyContactName || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.emergencyContactName || 'Not specified'}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Contact Phone</label>
                {isEditing ? (
                  <input type="tel" name="emergencyContactPhone" value={profile.emergencyContactPhone || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.emergencyContactPhone || 'Not specified'}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center">
            <div className="w-20 h-20 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-3xl font-bold mx-auto mb-4">
              {user?.name?.charAt(0).toUpperCase()}
            </div>
            <h3 className="text-xl font-bold text-gray-900">{user?.name}</h3>
            <p className="text-gray-500 mb-2">{user?.email}</p>
            <span className="inline-block bg-blue-100 text-blue-800 text-xs px-3 py-1 rounded-full font-semibold">
              Patient
            </span>
          </div>

          {activeEmergency ? (
            <div className="bg-red-50 rounded-2xl shadow-sm border border-red-200 p-6">
              <div className="flex items-center gap-2 text-red-700 font-bold mb-4">
                <HeartPulse size={24} className="animate-pulse" />
                <h3 className="text-xl">Active Emergency</h3>
              </div>
              
              <div className="space-y-3 mb-6">
                <div className="flex justify-between items-center bg-white p-3 rounded border border-red-100">
                  <span className="text-gray-600 text-sm">Status</span>
                  <span className="font-bold text-red-700">{activeEmergency.status}</span>
                </div>
                <div className="flex justify-between items-center bg-white p-3 rounded border border-red-100">
                  <span className="text-gray-600 text-sm">Severity</span>
                  <span className="font-bold text-red-700">{activeEmergency.severity || 'Analyzing...'}</span>
                </div>
                {activeEmergency.recommendedHospitalId && (
                  <div className="flex justify-between items-center bg-white p-3 rounded border border-red-100">
                    <span className="text-gray-600 text-sm">Hospital Assigned</span>
                    <span className="font-bold text-green-700">Yes</span>
                  </div>
                )}
                {activeEmergency.assignedAmbulanceId && (
                  <div className="flex justify-between items-center bg-white p-3 rounded border border-red-100">
                    <span className="text-gray-600 text-sm">Ambulance Assigned</span>
                    <span className="font-bold text-green-700">Yes</span>
                  </div>
                )}
              </div>
              
              <button 
                onClick={async () => {
                  if(window.confirm('Are you sure you want to cancel this emergency request?')) {
                    try {
                      await emergencyService.cancel(activeEmergency.id);
                      setActiveEmergency(null);
                    } catch (e) { alert('Failed to cancel'); }
                  }
                }}
                className="w-full bg-white text-red-600 border border-red-600 hover:bg-red-50 py-2 rounded-lg font-semibold transition-colors"
              >
                Cancel Request
              </button>
            </div>
          ) : (
            <div className="bg-red-50 rounded-2xl shadow-sm border border-red-100 p-6 text-center">
              <div className="w-16 h-16 bg-red-100 text-red-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <AlertCircle size={32} />
              </div>
              <h3 className="text-lg font-bold text-red-800 mb-2">Emergency SOS</h3>
              <p className="text-sm text-red-600 mb-4">
                Instantly alert hospitals and ambulances in your vicinity.
              </p>
              <button 
                onClick={() => setIsEmergencyModalOpen(true)}
                className="w-full bg-red-600 hover:bg-red-700 text-white py-3 rounded-lg font-bold text-lg shadow-md transition-all active:scale-95 flex justify-center items-center gap-2"
              >
                <HeartPulse size={24} /> ACTIVATE SOS
              </button>
            </div>
          )}
        </div>
      </div>
      
      <EmergencyModal 
        isOpen={isEmergencyModalOpen} 
        onClose={() => setIsEmergencyModalOpen(false)} 
        onCreated={(emergency) => setActiveEmergency(emergency)}
      />
    </div>
  );
};

export default PatientDashboard;
