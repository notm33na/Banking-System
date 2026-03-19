import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor – inject JWT
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('vb_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor – handle 401 (expired / invalid token)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('vb_token');
      localStorage.removeItem('vb_user');

      // Show toast notification
      const msg = error.response.data?.message || 'Your session has expired — please log in again';
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        alert(msg); // Replace with toast library if available
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
