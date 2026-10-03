import React from 'react';
import { useAuth } from '../context/AuthContext';
import PatientDashboard from './dashboards/PatientDashboard';
import DoctorDashboard from './dashboards/DoctorDashboard';
import HospitalDashboard from './dashboards/HospitalDashboard';
import DriverDashboard from './dashboards/DriverDashboard';
import AdminDashboard from './dashboards/AdminDashboard';
import BloodBankDashboard from './dashboards/BloodBankDashboard';
import PharmacyDashboard from './dashboards/PharmacyDashboard';
import { ShieldAlert, Zap } from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();

  const renderDashboard = () => {
    switch (user?.role) {
      case 'PATIENT':
        return <PatientDashboard />;
      case 'DOCTOR':
        return <DoctorDashboard />;
      case 'HOSPITAL_STAFF':
      case 'HOSPITAL':
        return <HospitalDashboard />;
      case 'AMBULANCE_DRIVER':
        return <DriverDashboard />;
      case 'ADMIN':
        return <AdminDashboard />;
      case 'BLOOD_BANK':
        return <BloodBankDashboard />;
      case 'PHARMACY':
        return <PharmacyDashboard />;
      default:
        return (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 flex flex-col items-center justify-center text-center min-h-[300px]">
            <div className="w-20 h-20 bg-gray-50 text-gray-300 rounded-full flex items-center justify-center mb-6">
              <Zap size={40} />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-3">Role Not Found</h3>
            <p className="text-gray-500 max-w-md mx-auto mb-6">
              Your role is either missing or unrecognized by the system.
            </p>
          </div>
        );
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] bg-slate-50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {renderDashboard()}
      </div>
    </div>
  );
};

export default Dashboard;
