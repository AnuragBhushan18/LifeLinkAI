import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Shield, Users, Activity, Building, Truck, Search, Filter, Eye, Edit, Trash2 } from 'lucide-react';
import api from '../../services/api';

import emergencyRealtimeService from '../../services/emergencyRealtimeService';
import ConnectionBadge from '../../components/ConnectionBadge';
import { emergencyService } from '../../services/entityServices';

const AdminDashboard = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('emergencies');
  const [stats, setStats] = useState({
    users: 124,
    hospitals: 12,
    doctors: 45,
    ambulances: 28,
    emergencies: 0
  });
  const [emergencies, setEmergencies] = useState([]);

  useEffect(() => {
    const fetchEmergencies = async () => {
      try {
        const res = await emergencyService.getAll();
        if (res.data) {
          setEmergencies(res.data);
          setStats(s => ({...s, emergencies: res.data.length}));
        }
      } catch (error) {
        console.error("Error fetching emergencies", error);
      }
    };
    
    fetchEmergencies();

    // Real-time admin subscription
    const unsubscribe = emergencyRealtimeService.subscribeToAdmin((event) => {
      console.log('[AdminDashboard] Emergency event received:', event);
      setEmergencies((prev) => {
        const exists = prev.some((e) => e.id === event.emergencyId);
        if (exists) {
          return prev.map((e) =>
            e.id === event.emergencyId
              ? { ...e, status: event.status || e.status, severity: event.severity || e.severity }
              : e
          );
        } else {
          fetchEmergencies();
          return prev;
        }
      });
    });

    return () => unsubscribe();
  }, []);

  const tabs = [
    { id: 'emergencies', label: 'Emergencies', icon: Shield },
    { id: 'users', label: 'Users', icon: Users },
    { id: 'hospitals', label: 'Hospitals', icon: Building },
    { id: 'doctors', label: 'Doctors', icon: Activity },
    { id: 'ambulances', label: 'Ambulances', icon: Truck },
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">System Administration</h1>
          <p className="text-sm text-gray-500 mt-1">Real-time emergency monitoring & system operations</p>
        </div>
        <div className="flex items-center gap-3">
          <ConnectionBadge />
          <span className="bg-red-100 text-red-800 text-sm px-3 py-1 rounded-full font-semibold flex items-center gap-2">
            <Shield size={16} /> Super Admin Access
          </span>
        </div>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-14 h-14 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center">
            <Users size={28} />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Total Users</p>
            <p className="text-2xl font-bold text-gray-900">{stats.users}</p>
          </div>
        </div>
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-14 h-14 bg-green-100 text-green-600 rounded-xl flex items-center justify-center">
            <Building size={28} />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Hospitals</p>
            <p className="text-2xl font-bold text-gray-900">{stats.hospitals}</p>
          </div>
        </div>
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-14 h-14 bg-indigo-100 text-indigo-600 rounded-xl flex items-center justify-center">
            <Activity size={28} />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Doctors</p>
            <p className="text-2xl font-bold text-gray-900">{stats.doctors}</p>
          </div>
        </div>
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-14 h-14 bg-orange-100 text-orange-600 rounded-xl flex items-center justify-center">
            <Truck size={28} />
          </div>
          <div>
            <p className="text-sm font-medium text-gray-500">Ambulances</p>
            <p className="text-2xl font-bold text-gray-900">{stats.ambulances}</p>
          </div>
        </div>
      </div>

      {/* Admin Management Section */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="border-b border-gray-100 bg-gray-50 px-6 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex space-x-4">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 pb-2 border-b-2 font-medium transition-colors ${activeTab === tab.id ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
                >
                  <Icon size={18} /> {tab.label}
                </button>
              );
            })}
          </div>
          <div className="flex gap-2">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={18} />
              <input type="text" placeholder={`Search ${activeTab}...`} className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm w-full sm:w-64" />
            </div>
            <button className="flex items-center gap-2 bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-50 text-sm font-medium">
              <Filter size={18} /> Filter
            </button>
          </div>
        </div>
        
        <div className="overflow-x-auto">
          {activeTab === 'emergencies' ? (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200 text-xs uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-4 font-medium">ID</th>
                  <th className="px-6 py-4 font-medium">Patient</th>
                  <th className="px-6 py-4 font-medium">Severity</th>
                  <th className="px-6 py-4 font-medium">Status</th>
                  <th className="px-6 py-4 font-medium">Created At</th>
                  <th className="px-6 py-4 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {emergencies.map(em => (
                  <tr key={em.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 text-sm font-mono text-gray-500">{em.id.slice(-6)}</td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{em.patientId.slice(-6)}</td>
                    <td className="px-6 py-4 text-sm">
                      <span className={`px-2 py-1 rounded text-xs font-semibold ${
                        em.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                        em.severity === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                        em.severity === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        {em.severity}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm font-medium">{em.status}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{new Date(em.createdAt).toLocaleString()}</td>
                    <td className="px-6 py-4 text-sm text-right space-x-2">
                      <button className="text-gray-400 hover:text-blue-600 transition-colors" title="Process Emergency">
                        <Activity size={18} onClick={async () => {
                            try {
                                await api.post(`/emergencies/${em.id}/process`);
                            } catch (e) { alert('Failed to process'); }
                        }} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200 text-xs uppercase text-gray-500 tracking-wider">
                  <th className="px-6 py-4 font-medium">ID</th>
                  <th className="px-6 py-4 font-medium">Name</th>
                  <th className="px-6 py-4 font-medium">Status / Role</th>
                  <th className="px-6 py-4 font-medium">Date Added</th>
                  <th className="px-6 py-4 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                <tr className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-sm text-gray-500">#UID-001</td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">John Doe</td>
                  <td className="px-6 py-4 text-sm"><span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs font-semibold">PATIENT</span></td>
                  <td className="px-6 py-4 text-sm text-gray-500">2026-09-28</td>
                  <td className="px-6 py-4 text-sm text-right space-x-2">
                    <button className="text-gray-400 hover:text-blue-600 transition-colors"><Eye size={18} /></button>
                    <button className="text-gray-400 hover:text-green-600 transition-colors"><Edit size={18} /></button>
                    <button className="text-gray-400 hover:text-red-600 transition-colors"><Trash2 size={18} /></button>
                  </td>
                </tr>
                <tr className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-sm text-gray-500">#UID-002</td>
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">City Hospital</td>
                  <td className="px-6 py-4 text-sm"><span className="bg-green-100 text-green-800 px-2 py-1 rounded text-xs font-semibold">HOSPITAL</span></td>
                  <td className="px-6 py-4 text-sm text-gray-500">2026-09-29</td>
                  <td className="px-6 py-4 text-sm text-right space-x-2">
                    <button className="text-gray-400 hover:text-blue-600 transition-colors"><Eye size={18} /></button>
                    <button className="text-gray-400 hover:text-green-600 transition-colors"><Edit size={18} /></button>
                    <button className="text-gray-400 hover:text-red-600 transition-colors"><Trash2 size={18} /></button>
                  </td>
                </tr>
                {/* More placeholder rows can go here */}
              </tbody>
            </table>
          )}
          
          <div className="px-6 py-4 border-t border-gray-100 text-center">
            {activeTab !== 'emergencies' && <p className="text-sm text-gray-500">Showing dummy data. Actual API integration pending.</p>}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
