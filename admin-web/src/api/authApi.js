import axiosClient from './axiosClient';

export const authApi = {
  login: async (credentials) => {
    const response = await axiosClient.post('/auth/login', credentials);
    return response.data;
  },

  register: async (registrationData) => {
    const response = await axiosClient.post('/auth/signup', registrationData);
    return response.data;
  },

  pinLogin: async (pinData) => {
    const response = await axiosClient.post('/auth/pin-login', pinData);
    return response.data;
  },

  verify2Fa: async (verificationData) => {
    const response = await axiosClient.post('/auth/2fa/verify', verificationData);
    return response.data;
  },

  refreshToken: async (refreshToken) => {
    const response = await axiosClient.post('/auth/refresh', { refreshToken });
    return response.data;
  },

  logout: async (refreshToken) => {
    try {
      await axiosClient.post('/auth/logout', { refreshToken });
    } catch (e) {
      console.warn('Logout API error:', e);
    }
  },

  getMe: async () => {
    const response = await axiosClient.get('/auth/me');
    return response.data;
  },

  changePassword: async (passwordData) => {
    const response = await axiosClient.post('/auth/change-password', passwordData);
    return response.data;
  },

  setup2Fa: async () => {
    const response = await axiosClient.post('/auth/2fa/setup');
    return response.data;
  },

  enable2Fa: async (secret, code) => {
    const response = await axiosClient.post('/auth/2fa/enable', { secret, code });
    return response.data;
  },

  disable2Fa: async (password) => {
    const response = await axiosClient.post('/auth/2fa/disable', { password });
    return response.data;
  },

  getTenantBranches: async (tenantCode) => {
    const response = await axiosClient.get(`/auth/tenant/${tenantCode}/branches`);
    return response.data;
  }
};
