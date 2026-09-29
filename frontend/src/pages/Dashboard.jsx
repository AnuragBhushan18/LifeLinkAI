import React from 'react';
import { useAuth } from '../context/AuthContext';
import { User, Mail, ShieldAlert, Zap } from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();

  return (
    <div className="min-h-[calc(100vh-64px)] bg-slate-50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Dashboard</h1>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* User Profile Card */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 md:col-span-1">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-2xl font-bold">
                {user?.name?.charAt(0).toUpperCase()}
              </div>
              <div>
                <h2 className="text-xl font-bold text-gray-900">{user?.name}</h2>
                <span className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-md font-semibold mt-1">
                  {user?.role}
                </span>
              </div>
            </div>
            
            <div className="space-y-4">
              <div className="flex items-center gap-3 text-gray-600">
                <Mail size={18} className="text-gray-400" />
                <span className="text-sm">{user?.email}</span>
              </div>
              <div className="flex items-center gap-3 text-gray-600">
                <User size={18} className="text-gray-400" />
                <span className="text-sm">User ID: {user?.id?.slice(-8) || 'N/A'}</span>
              </div>
            </div>
          </div>

          {/* Coming Soon Area */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 md:col-span-2 flex flex-col items-center justify-center text-center min-h-[300px]">
            <div className="w-20 h-20 bg-gray-50 text-gray-300 rounded-full flex items-center justify-center mb-6">
              <Zap size={40} />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-3">Emergency Response System</h3>
            <p className="text-gray-500 max-w-md mx-auto mb-6">
              The core emergency coordination modules, real-time routing, and AI features are scheduled for Phase 3.
            </p>
            <div className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 bg-blue-50 px-4 py-2 rounded-full">
              <ShieldAlert size={16} />
              <span>Coming in Phase 3</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
