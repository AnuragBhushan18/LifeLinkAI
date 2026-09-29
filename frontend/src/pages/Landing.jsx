import React from 'react';
import { Link } from 'react-router-dom';
import { Shield, Clock, Activity } from 'lucide-react';

const Landing = () => {
  return (
    <div className="min-h-[calc(100vh-64px)] bg-slate-50 flex flex-col">
      <main className="flex-grow">
        {/* Hero Section */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 text-center">
          <h1 className="text-5xl font-extrabold text-gray-900 tracking-tight mb-6">
            Intelligent Emergency Response &<br />
            <span className="text-blue-600">Healthcare Coordination</span>
          </h1>
          <p className="mt-4 text-xl text-gray-600 max-w-3xl mx-auto mb-10">
            LifeLink AI coordinates patients, ambulances, and hospitals during critical emergencies to save lives through real-time intelligent routing and resource management.
          </p>
          <div className="flex justify-center gap-4">
            <Link to="/register" className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-lg font-semibold text-lg transition-colors shadow-lg shadow-blue-200">
              Get Started
            </Link>
            <Link to="/login" className="bg-white hover:bg-gray-50 text-gray-800 border border-gray-200 px-8 py-3 rounded-lg font-semibold text-lg transition-colors shadow-sm">
              Login to Portal
            </Link>
          </div>
        </div>

        {/* Features Section */}
        <div className="bg-white py-20 border-t border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-10 text-center">
              <div className="p-6 rounded-2xl bg-blue-50 border border-blue-100">
                <div className="w-14 h-14 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Activity size={28} />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">Real-Time Coordination</h3>
                <p className="text-gray-600">Seamless communication between patients, first responders, and medical facilities.</p>
              </div>
              <div className="p-6 rounded-2xl bg-blue-50 border border-blue-100">
                <div className="w-14 h-14 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Clock size={28} />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">Rapid Response</h3>
                <p className="text-gray-600">AI-driven routing ensures the fastest possible emergency medical response.</p>
              </div>
              <div className="p-6 rounded-2xl bg-blue-50 border border-blue-100">
                <div className="w-14 h-14 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Shield size={28} />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">Secure & Reliable</h3>
                <p className="text-gray-600">Enterprise-grade security protecting sensitive healthcare data and operations.</p>
              </div>
            </div>
          </div>
        </div>
      </main>
      
      <footer className="bg-gray-900 text-white py-8 text-center">
        <p className="text-gray-400">© 2026 LifeLink AI Platform. Phase 1 - Foundation.</p>
      </footer>
    </div>
  );
};

export default Landing;
