import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext(null);

function decodeToken(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      userId: payload.userId,
      email: payload.sub || payload.email,
      role: payload.role,
    };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('vb_token'));
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('vb_user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback((authResponse) => {
    const { token: jwt, userId, email, role } = authResponse;
    const userData = { userId, email, role };
    localStorage.setItem('vb_token', jwt);
    localStorage.setItem('vb_user', JSON.stringify(userData));
    setToken(jwt);
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('vb_token');
    localStorage.removeItem('vb_user');
    setToken(null);
    setUser(null);
    window.location.href = '/login';
  }, []);

  const value = { user, token, login, logout, isAuthenticated: !!token };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
