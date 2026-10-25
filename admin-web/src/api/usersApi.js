import axiosClient from './axiosClient';

export const usersApi = {
  listUsers: async (params = {}) => {
    const response = await axiosClient.get('/admin/users', { params });
    return response.data;
  },

  getUserById: async (id) => {
    const response = await axiosClient.get(`/admin/users/${id}`);
    return response.data;
  },

  createUser: async (userData) => {
    const response = await axiosClient.post('/admin/users', userData);
    return response.data;
  },

  updateUser: async (id, userData) => {
    const response = await axiosClient.put(`/admin/users/${id}`, userData);
    return response.data;
  },

  toggleUserStatus: async (id, active) => {
    const response = await axiosClient.patch(`/admin/users/${id}/status`, null, {
      params: { active }
    });
    return response.data;
  },

  resetPassword: async (id, newPassword) => {
    const response = await axiosClient.post(`/admin/users/${id}/reset-password`, {
      newPassword
    });
    return response.data;
  },

  setUserPin: async (id, pin, confirmPin) => {
    const response = await axiosClient.post(`/admin/users/${id}/set-pin`, {
      pin,
      confirmPin
    });
    return response.data;
  },

  getBranches: async () => {
    const response = await axiosClient.get('/admin/branches');
    return response.data;
  }
};
