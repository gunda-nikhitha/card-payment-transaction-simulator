import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('cpts_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [token, setToken] = useState(() => localStorage.getItem('cpts_token'));
  const [loading, setLoading] = useState(true);

  // Sync / Verify with server on mount
  useEffect(() => {
    const verifyUser = async () => {
      if (token) {
        try {
          const freshUser = await api.get('/api/auth/me');
          setUser(freshUser);
          localStorage.setItem('cpts_user', JSON.stringify(freshUser));
        } catch (err) {
          console.warn('Session expired or invalid, logging out', err);
          logout();
        }
      }
      setLoading(false);
    };

    const handleAuthExpired = () => {
      logout();
    };

    window.addEventListener('cpts-auth-expired', handleAuthExpired);
    verifyUser();

    return () => {
      window.removeEventListener('cpts-auth-expired', handleAuthExpired);
    };
  }, []);

  const login = async (email, password) => {
    const res = await api.post('/api/auth/login', { email, password });
    setToken(res.token);
    setUser(res.user);
    localStorage.setItem('cpts_token', res.token);
    localStorage.setItem('cpts_user', JSON.stringify(res.user));
    return res.user;
  };

  const register = async (name, email, password) => {
    const res = await api.post('/api/auth/register', { name, email, password });
    setToken(res.token);
    setUser(res.user);
    localStorage.setItem('cpts_token', res.token);
    localStorage.setItem('cpts_user', JSON.stringify(res.user));
    return res.user;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('cpts_token');
    localStorage.removeItem('cpts_user');
  };

  const updateUser = (updated) => {
    setUser(updated);
    localStorage.setItem('cpts_user', JSON.stringify(updated));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.role === 'ADMIN',
        login,
        register,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
