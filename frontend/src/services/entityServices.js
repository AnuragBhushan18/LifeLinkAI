import api from './api';

export const patientService = {
  getProfile: (userId) => api.get(`/patients/user/${userId}`),
  updateProfile: (userId, data) => api.put(`/patients/user/${userId}`, data),
};

export const doctorService = {
  getProfile: (userId) => api.get(`/doctors/user/${userId}`),
  updateProfile: (userId, data) => api.put(`/doctors/user/${userId}`, data),
  updateStatus: (userId, status) => api.put(`/doctors/user/${userId}/status`, { status }),
};

export const hospitalService = {
  getProfile: (userId) => api.get(`/hospitals/user/${userId}`),
  updateProfile: (userId, data) => api.put(`/hospitals/user/${userId}`, data),
};

export const driverService = {
  getProfile: (userId) => api.get(`/drivers/user/${userId}`),
  updateProfile: (userId, data) => api.put(`/drivers/user/${userId}`, data),
  updateStatus: (userId, status) => api.put(`/drivers/user/${userId}/status`, { status }),
};

export const adminService = {
  getAllUsers: () => api.get('/admin/users'),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
  // Additional admin methods will go here
};
