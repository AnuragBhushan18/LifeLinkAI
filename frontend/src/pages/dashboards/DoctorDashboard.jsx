import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { User, Activity, Clock, MapPin, Edit2, Save, Award } from 'lucide-react';
import api from '../../services/api';
import DoctorAISummary from './DoctorAISummary';

const DoctorDashboard = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({
    specialization: '',
    department: '',
    shift: '',
    availabilityStatus: 'AVAILABLE',
    experienceYears: 0
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [searchPatientId, setSearchPatientId] = useState('');

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get(`/doctors/user/${user.id}`);
        if (res.data) setProfile(res.data);
      } catch (error) {
        console.error("Error fetching doctor profile", error);
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
      await api.put(`/doctors/user/${user.id}`, profile);
      setIsEditing(false);
    } catch (error) {
      console.error("Error saving profile", error);
      alert("Failed to save profile");
    }
  };

  const toggleAvailability = async () => {
    const newStatus = profile.availabilityStatus === 'AVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE';
    try {
      await api.put(`/doctors/user/${user.id}/status`, { status: newStatus });
      setProfile({ ...profile, availabilityStatus: newStatus });
    } catch (error) {
      console.error("Error updating status", error);
    }
  };

  if (loading) return <div className="text-center py-10">Loading...</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Doctor Dashboard</h1>
        <button 
          onClick={() => isEditing ? handleSave() : setIsEditing(true)}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md transition-colors"
        >
          {isEditing ? <><Save size={18} /> Save Profile</> : <><Edit2 size={18} /> Edit Profile</>}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center">
            <div className="w-24 h-24 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-4xl font-bold mx-auto mb-4">
              {user?.name?.charAt(0).toUpperCase()}
            </div>
            <h3 className="text-2xl font-bold text-gray-900">Dr. {user?.name}</h3>
            <p className="text-gray-500 mb-4">{profile.specialization || 'General Practitioner'}</p>
            
            <div className="flex items-center justify-center gap-2 mb-6">
              <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${profile.availabilityStatus === 'AVAILABLE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                <span className={`w-2 h-2 rounded-full mr-2 ${profile.availabilityStatus === 'AVAILABLE' ? 'bg-green-500' : 'bg-red-500'}`}></span>
                {profile.availabilityStatus}
              </span>
            </div>

            <button 
              onClick={toggleAvailability}
              className="w-full border-2 border-gray-200 hover:border-gray-300 text-gray-700 font-medium py-2 rounded-lg transition-colors"
            >
              Toggle Availability
            </button>
          </div>
        </div>

        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-gray-800">
              <Award className="text-indigo-500" /> Professional Details
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Specialization</label>
                {isEditing ? (
                  <input type="text" name="specialization" value={profile.specialization || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.specialization || 'Not specified'}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Department</label>
                {isEditing ? (
                  <input type="text" name="department" value={profile.department || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.department || 'Not specified'}</p>
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
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1 flex items-center gap-1">
                  <Clock size={16} /> Current Shift
                </label>
                {isEditing ? (
                  <input type="text" name="shift" value={profile.shift || ''} onChange={handleChange} placeholder="e.g. 08:00 AM - 04:00 PM" className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.shift || 'Not specified'}</p>
                )}
              </div>
            </div>
          </div>
          
          <div className="lg:col-span-3 mt-8 bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-bold mb-4 text-blue-900">Patient Lookup & AI Summary</h2>
            <div className="flex gap-4 mb-4">
                <input 
                    type="text" 
                    value={searchPatientId} 
                    onChange={e => setSearchPatientId(e.target.value)} 
                    placeholder="Enter Patient ID (e.g. 64b8f...)" 
                    className="border rounded px-3 py-2 flex-1 outline-none focus:ring-2 focus:ring-blue-500"
                />
            </div>
            {searchPatientId && <DoctorAISummary patientId={searchPatientId} />}
          </div>
          
        </div>
      </div>
    </div>
  );
};

export default DoctorDashboard;
