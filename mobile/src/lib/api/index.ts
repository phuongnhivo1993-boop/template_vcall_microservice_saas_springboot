import api from '../axios';

export const authApi = {
  login: (data: { email: string; password: string }) =>
    api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
};

export const callsApi = {
  getHistory: (params?: { page?: number; limit?: number }) =>
    api.get('/calls', { params }),
  getCall: (id: string) => api.get(`/calls/${id}`),
  startCall: (data: { calleeNumber: string }) =>
    api.post('/calls/start', data),
  endCall: (id: string) => api.post(`/calls/${id}/end`),
  muteCall: (id: string) => api.post(`/calls/${id}/mute`),
  unmuteCall: (id: string) => api.post(`/calls/${id}/unmute`),
  holdCall: (id: string) => api.post(`/calls/${id}/hold`),
  resumeCall: (id: string) => api.post(`/calls/${id}/resume`),
};

export const agentsApi = {
  getProfile: () => api.get('/agents/profile'),
  updateStatus: (status: string) => api.patch('/agents/status', { status }),
  getStats: () => api.get('/agents/stats'),
};

export const customersApi = {
  search: (query: string) => api.get('/customers/search', { params: { q: query } }),
  getCustomer: (id: string) => api.get(`/customers/${id}`),
};

export const ticketsApi = {
  getAll: (params?: { status?: string; priority?: string; page?: number }) =>
    api.get('/tickets', { params }),
  getTicket: (id: string) => api.get(`/tickets/${id}`),
  createComment: (id: string, data: { content: string }) =>
    api.post(`/tickets/${id}/comments`, data),
  updateStatus: (id: string, status: string) =>
    api.patch(`/tickets/${id}/status`, { status }),
};

export const chatApi = {
  getConversations: () => api.get('/chat/conversations'),
  getMessages: (conversationId: string, params?: { page?: number }) =>
    api.get(`/chat/conversations/${conversationId}/messages`, { params }),
  sendMessage: (conversationId: string, data: { content: string; type?: string }) =>
    api.post(`/chat/conversations/${conversationId}/messages`, data),
  markRead: (conversationId: string) =>
    api.post(`/chat/conversations/${conversationId}/read`),
};
