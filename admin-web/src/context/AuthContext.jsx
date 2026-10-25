import React, { createContext, useState, useEffect, useContext } from 'react';
import { authApi } from '../api/authApi';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [activeBranch, setActiveBranch] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('pos_access_token');
      const savedBranch = localStorage.getItem('pos_active_branch');
      if (savedBranch) {
        try {
          setActiveBranch(JSON.parse(savedBranch));
        } catch (e) {}
      }

      if (token) {
        try {
          const response = await authApi.getMe();
          if (response?.success && response.data) {
            setUser(response.data);
            if (!savedBranch && response.data.branches?.length > 0) {
              const defaultBranch = Array.from(response.data.branches)[0];
              setActiveBranch(defaultBranch);
              localStorage.setItem('pos_active_branch', JSON.stringify(defaultBranch));
            }
          }
        } catch (err) {
          console.warn('Initial session validation failed:', err);
          localStorage.removeItem('pos_access_token');
          localStorage.removeItem('pos_refresh_token');
          setUser(null);
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const handleAuthSuccess = (data) => {
    const { accessToken, refreshToken, user: userData, activeBranch: branchData } = data;
    localStorage.setItem('pos_access_token', accessToken);
    if (refreshToken) {
      localStorage.setItem('pos_refresh_token', refreshToken);
    }
    setUser(userData);

    const branchToSet = branchData || (userData.branches?.length > 0 ? Array.from(userData.branches)[0] : null);
    if (branchToSet) {
      setActiveBranch(branchToSet);
      localStorage.setItem('pos_active_branch', JSON.stringify(branchToSet));
    }
    return { success: true, user: userData };
  };

  const login = async (username, password, branchId = null, rememberMe = false) => {
    const res = await authApi.login({ username, password, branchId, rememberMe });
    if (res?.success && res.data) {
      if (res.data.requires2FA) {
        return { requires2FA: true, tempToken: res.data.tempToken };
      }
      return handleAuthSuccess(res.data);
    }
    throw new Error(res?.message || 'Authentication failed');
  };

  const register = async (registrationData) => {
    const res = await authApi.register(registrationData);
    if (res?.success && res.data) {
      return handleAuthSuccess(res.data);
    }
    throw new Error(res?.message || 'Registration failed');
  };

  const pinLogin = async (tenantCode, branchId, terminalCode, pin) => {
    const res = await authApi.pinLogin({ tenantCode, branchId, terminalCode, pin });
    if (res?.success && res.data) {
      return handleAuthSuccess(res.data);
    }
    throw new Error(res?.message || 'PIN login failed');
  };

  const verify2Fa = async (tempToken, code, branchId = null) => {
    const res = await authApi.verify2Fa({ tempToken, code, branchId });
    if (res?.success && res.data) {
      return handleAuthSuccess(res.data);
    }
    throw new Error(res?.message || '2FA verification failed');
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('pos_refresh_token');
    await authApi.logout(refreshToken);
    localStorage.removeItem('pos_access_token');
    localStorage.removeItem('pos_refresh_token');
    localStorage.removeItem('pos_active_branch');
    setUser(null);
    setActiveBranch(null);
  };

  const switchBranch = (branch) => {
    setActiveBranch(branch);
    localStorage.setItem('pos_active_branch', JSON.stringify(branch));
  };

  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    const formatted = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return user.roles.includes(formatted) || user.roles.includes('ROLE_SUPER_ADMIN');
  };

  const hasPermission = (permission) => {
    if (!user) return false;
    if (hasRole('SUPER_ADMIN')) return true;
    return user.permissions && user.permissions.includes(permission);
  };

  return (
    <AuthContext.Provider value={{
      user,
      activeBranch,
      isAuthenticated: !!user,
      loading,
      login,
      register,
      pinLogin,
      verify2Fa,
      logout,
      switchBranch,
      hasRole,
      hasPermission
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
