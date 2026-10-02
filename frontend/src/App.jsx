import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';

import { WebSocketProvider } from './context/WebSocketContext';
import ToastNotification from './components/ToastNotification';

function App() {
  return (
    <AuthProvider>
      <WebSocketProvider>
        <Router>
          <div className="min-h-screen flex flex-col font-sans text-gray-900 bg-gray-50/50">
            <Navbar />
            <Routes>
              <Route path="/" element={<Landing />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              
              <Route element={<ProtectedRoute />}>
                <Route path="/dashboard" element={<Dashboard />} />
              </Route>
            </Routes>
            <ToastNotification />
          </div>
        </Router>
      </WebSocketProvider>
    </AuthProvider>
  );
}

export default App;
