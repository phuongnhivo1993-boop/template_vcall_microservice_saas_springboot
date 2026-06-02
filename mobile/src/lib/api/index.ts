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
  startCall: (data: { callerNumber: string; calleeNumber: string; direction?: string }) =>
    api.post('/calls', data),
  endCall: (id: string) => api.post(`/calls/${id}/hangup`),
  muteCall: (id: string) => api.post(`/calls/${id}/mute`),
  unmuteCall: (id: string) => api.post(`/calls/${id}/unmute`),
  holdCall: (id: string) => api.post(`/calls/${id}/hold`),
  resumeCall: (id: string) => api.post(`/calls/${id}/resume`),
  transferCall: (id: string, targetAgentId: string) =>
    api.post(`/calls/${id}/transfer`, { targetAgentId }),
};

export const agentsApi = {
  getProfile: (userId: string) => api.get('/agents/profile', { params: { userId } }),
  updateStatus: (agentId: string, status: string, reason?: string) =>
    api.patch(`/agents/${agentId}/status`, { status, reason }),
  getStats: () => api.get('/agents/stats'),
};

export const customersApi = {
  search: (keyword: string) => api.get('/customers/search', { params: { keyword } }),
  getCustomer: (id: string) => api.get(`/customers/${id}`),
};

export const ticketsApi = {
  getAll: (params?: { status?: string; priority?: string; page?: number }) =>
    api.get('/tickets', { params }),
  getTicket: (id: string) => api.get(`/tickets/${id}`),
  createComment: (id: string, data: { content: string; isInternal?: boolean }) =>
    api.post(`/tickets/${id}/comments`, data),
  updateStatus: (id: string, status: string) =>
    api.patch(`/tickets/${id}/status`, { status }),
  search: (params: { q?: string; status?: string; priority?: string; page?: number }) =>
    api.get('/tickets/search', { params }),
};

export const chatApi = {
  getConversations: () => api.get('/chat/conversations'),
  getMessages: (conversationId: string, params?: { page?: number }) =>
    api.get(`/chat/conversations/${conversationId}/messages`, { params }),
  sendMessage: (conversationId: string, data: { content: string; contentType?: string; senderType?: string; senderId?: string }) =>
    api.post(`/chat/conversations/${conversationId}/messages`, data),
  markRead: (conversationId: string) =>
    api.post(`/chat/conversations/${conversationId}/read`),
};
