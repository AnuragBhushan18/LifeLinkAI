import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Building2, Activity, Edit2, Save, Users, Truck, AlertCircle } from 'lucide-react';
import api from '../../services/api';
import { emergencyService } from '../../services/entityServices';

const HospitalDashboard = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState({
    hospitalName: '',
    address: '',
    totalBeds: 0,
    availableBeds: 0,
    totalIcuBeds: 0,
    availableIcuBeds: 0,
    operationalStatus: 'OPERATIONAL'
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [emergencies, setEmergencies] = useState([]);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get(`/hospitals/user/${user.id}`);
        if (res.data) setProfile(res.data);
      } catch (error) {
        console.error("Error fetching hospital profile", error);
      }
    };
    
    const fetchEmergencies = async () => {
      try {
        const res = await emergencyService.getAll();
        if (res.data) setEmergencies(res.data);
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
    const { name, value } = e.target;
    const numFields = ['totalBeds', 'availableBeds', 'totalIcuBeds', 'availableIcuBeds'];
    setProfile({ 
      ...profile, 
      [name]: numFields.includes(name) ? Math.max(0, parseInt(value) || 0) : value 
    });
  };

  const handleSave = async () => {
    try {
      await api.put(`/hospitals/user/${user.id}`, profile);
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
        <h1 className="text-3xl font-bold text-gray-900">Hospital Dashboard</h1>
        <button 
          onClick={() => isEditing ? handleSave() : setIsEditing(true)}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md transition-colors"
        >
          {isEditing ? <><Save size={18} /> Save Details</> : <><Edit2 size={18} /> Update Facility</>}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-gray-800">
              <Building2 className="text-blue-500" /> Facility Profile
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-500 mb-1">Hospital Name</label>
                {isEditing ? (
                  <input type="text" name="hospitalName" value={profile.hospitalName || ''} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium text-lg">{profile.hospitalName || user?.name}</p>
                )}
              </div>
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-500 mb-1">Address</label>
                {isEditing ? (
                  <textarea name="address" value={profile.address || ''} onChange={handleChange} rows="2" className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                ) : (
                  <p className="text-gray-900 font-medium">{profile.address || 'Not specified'}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Operational Status</label>
                {isEditing ? (
                  <select name="operationalStatus" value={profile.operationalStatus} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500">
                    <option value="OPERATIONAL">Operational</option>
                    <option value="LIMITED">Limited Capacity</option>
                    <option value="MAINTENANCE">Under Maintenance</option>
                  </select>
                ) : (
                  <p className="text-gray-900 font-medium">{profile.operationalStatus}</p>
                )}
              </div>
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-gray-800">
              <Activity className="text-green-500" /> Bed Availability Tracking
            </h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
                <label className="block text-xs font-medium text-blue-600 mb-1">Total Beds</label>
                {isEditing ? (
                  <input type="number" name="totalBeds" value={profile.totalBeds} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm text-sm" />
                ) : (
                  <p className="text-2xl font-bold text-gray-900">{profile.totalBeds}</p>
                )}
              </div>
              <div className="bg-green-50 p-4 rounded-xl border border-green-100">
                <label className="block text-xs font-medium text-green-600 mb-1">Available Beds</label>
                {isEditing ? (
                  <input type="number" name="availableBeds" value={profile.availableBeds} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm text-sm" />
                ) : (
                  <p className="text-2xl font-bold text-green-700">{profile.availableBeds}</p>
                )}
              </div>
              <div className="bg-purple-50 p-4 rounded-xl border border-purple-100">
                <label className="block text-xs font-medium text-purple-600 mb-1">Total ICU</label>
                {isEditing ? (
                  <input type="number" name="totalIcuBeds" value={profile.totalIcuBeds} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm text-sm" />
                ) : (
                  <p className="text-2xl font-bold text-gray-900">{profile.totalIcuBeds}</p>
                )}
              </div>
              <div className="bg-orange-50 p-4 rounded-xl border border-orange-100">
                <label className="block text-xs font-medium text-orange-600 mb-1">Available ICU</label>
                {isEditing ? (
                  <input type="number" name="availableIcuBeds" value={profile.availableIcuBeds} onChange={handleChange} className="w-full border-gray-300 rounded-md shadow-sm text-sm" />
                ) : (
                  <p className="text-2xl font-bold text-orange-700">{profile.availableIcuBeds}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center">
             <div className="w-20 h-20 bg-gray-100 text-gray-600 rounded-full flex items-center justify-center text-3xl font-bold mx-auto mb-4">
              {user?.name?.charAt(0).toUpperCase() || 'H'}
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">{user?.name}</h3>
            <span className="inline-block bg-blue-100 text-blue-800 text-xs px-3 py-1 rounded-full font-semibold">
              Hospital Admin
            </span>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h3 className="font-semibold text-gray-800 mb-4">Quick Links</h3>
            <div className="space-y-3">
              <button className="w-full flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3 text-gray-700">
                  <Users size={18} /> <span>Manage Doctors</span>
                </div>
              </button>
              <button className="w-full flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors">
                <div className="flex items-center gap-3 text-gray-700">
                  <Truck size={18} /> <span>Manage Ambulances</span>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
      
      {/* Emergency Queue Section */}
      <div className="mt-8 bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
        <h2 className="text-xl font-semibold mb-4 flex items-center gap-2 text-red-600">
          <AlertCircle size={24} /> Emergency Queue
        </h2>
        
        {emergencies.length === 0 ? (
          <p className="text-gray-500 text-center py-6">No incoming emergencies at this time.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="py-3 px-4 font-semibold text-gray-600">ID</th>
                  <th className="py-3 px-4 font-semibold text-gray-600">Severity</th>
                  <th className="py-3 px-4 font-semibold text-gray-600">Status</th>
                  <th className="py-3 px-4 font-semibold text-gray-600">Symptoms</th>
                  <th className="py-3 px-4 font-semibold text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody>
                {emergencies.map(em => (
                  <tr key={em.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="py-3 px-4 font-mono text-sm">{em.id.slice(-6)}</td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-1 rounded text-xs font-bold ${
                        em.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                        em.severity === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                        em.severity === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        {em.severity}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-sm font-medium">{em.status}</td>
                    <td className="py-3 px-4 text-sm text-gray-600 max-w-xs truncate">
                      {em.symptoms?.join(', ')}
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex gap-2">
                        {em.status === 'ARRIVED_AT_HOSPITAL' && (
                          <button 
                            onClick={() => emergencyService.updateStatus(em.id, 'ADMITTED').then(() => window.location.reload())}
                            className="bg-blue-600 text-white px-3 py-1 rounded text-xs font-medium hover:bg-blue-700"
                          >
                            Admit Patient
                          </button>
                        )}
                        {em.status === 'ADMITTED' && (
                          <button 
                            onClick={() => emergencyService.updateStatus(em.id, 'TREATMENT_IN_PROGRESS').then(() => window.location.reload())}
                            className="bg-indigo-600 text-white px-3 py-1 rounded text-xs font-medium hover:bg-indigo-700"
                          >
                            Start Treatment
                          </button>
                        )}
                        {em.status === 'TREATMENT_IN_PROGRESS' && (
                          <button 
                            onClick={() => emergencyService.updateStatus(em.id, 'COMPLETED').then(() => window.location.reload())}
                            className="bg-green-600 text-white px-3 py-1 rounded text-xs font-medium hover:bg-green-700"
                          >
                            Mark Complete
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default HospitalDashboard;
