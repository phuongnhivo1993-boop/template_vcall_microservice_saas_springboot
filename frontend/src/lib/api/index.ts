import apiClient from '@/lib/axios';

export const authApi = {
  login: (data: { username: string; password: string }) =>
    apiClient.post('/auth/login', data),
  logout: () => apiClient.post('/auth/logout'),
  refresh: (token: string) => apiClient.post('/auth/refresh', { refreshToken: token }),
  me: () => apiClient.get('/auth/me'),
};

export const usersApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/users', { params }),
  getById: (id: string) => apiClient.get(`/users/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/users', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/users/${id}`, data),
  delete: (id: string) => apiClient.delete(`/users/${id}`),
};

export const agentsApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/agents', { params }),
  getById: (id: string) => apiClient.get(`/agents/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/agents', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/agents/${id}`, data),
  delete: (id: string) => apiClient.delete(`/agents/${id}`),
  updateStatus: (id: string, status: string) =>
    apiClient.patch(`/agents/${id}/status`, { status }),
  getSessions: (id: string) => apiClient.get(`/agents/${id}/sessions`),
  getStats: (id: string) => apiClient.get(`/agents/${id}/stats`),
};

export const customersApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/customers', { params }),
  getById: (id: string) => apiClient.get(`/customers/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/customers', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/customers/${id}`, data),
  delete: (id: string) => apiClient.delete(`/customers/${id}`),
  search: (query: string) => apiClient.get('/customers/search', { params: { q: query } }),
};

export const callsApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/calls', { params }),
  getById: (id: string) => apiClient.get(`/calls/${id}`),
  start: (data: Record<string, unknown>) => apiClient.post('/calls/start', data),
  end: (id: string) => apiClient.post(`/calls/${id}/end`),
  transfer: (id: string, data: Record<string, unknown>) =>
    apiClient.post(`/calls/${id}/transfer`, data),
  getRecording: (id: string) => apiClient.get(`/calls/${id}/recording`),
  getStats: (params?: Record<string, unknown>) => apiClient.get('/calls/stats', { params }),
};

export const ticketsApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/tickets', { params }),
  getById: (id: string) => apiClient.get(`/tickets/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/tickets', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/tickets/${id}`, data),
  delete: (id: string) => apiClient.delete(`/tickets/${id}`),
  updateStatus: (id: string, status: string) =>
    apiClient.patch(`/tickets/${id}/status`, { status }),
  assign: (id: string, agentId: string) =>
    apiClient.post(`/tickets/${id}/assign`, { agentId }),
};

export const reportsApi = {
  generate: (params: Record<string, unknown>) => apiClient.post('/reports/generate', params),
  list: (params?: Record<string, unknown>) => apiClient.get('/reports', { params }),
  getById: (id: string) => apiClient.get(`/reports/${id}`),
  download: (id: string) => apiClient.get(`/reports/${id}/download`, { responseType: 'blob' }),
  getCallVolume: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/call-volume', { params }),
  getAgentPerformance: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/agent-performance', { params }),
  getSlaCompliance: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/sla-compliance', { params }),
};

export const campaignsApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/campaigns', { params }),
  getById: (id: string) => apiClient.get(`/campaigns/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/campaigns', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/campaigns/${id}`, data),
  delete: (id: string) => apiClient.delete(`/campaigns/${id}`),
  start: (id: string) => apiClient.post(`/campaigns/${id}/start`),
  pause: (id: string) => apiClient.post(`/campaigns/${id}/pause`),
  stop: (id: string) => apiClient.post(`/campaigns/${id}/stop`),
};

export const billingApi = {
  getInvoices: (params?: Record<string, unknown>) => apiClient.get('/billing/invoices', { params }),
  getPlans: () => apiClient.get('/billing/plans'),
  subscribe: (planId: string) => apiClient.post('/billing/subscribe', { planId }),
  getUsage: () => apiClient.get('/billing/usage'),
};

export const settingsApi = {
  getProfile: () => apiClient.get('/settings/profile'),
  updateProfile: (data: Record<string, unknown>) => apiClient.put('/settings/profile', data),
  getOrganization: () => apiClient.get('/settings/organization'),
  updateOrganization: (data: Record<string, unknown>) =>
    apiClient.put('/settings/organization', data),
  getChannels: () => apiClient.get('/settings/channels'),
  updateChannels: (data: Record<string, unknown>) => apiClient.put('/settings/channels', data),
  getSecurity: () => apiClient.get('/settings/security'),
  updateSecurity: (data: Record<string, unknown>) => apiClient.put('/settings/security', data),
};

export const auditApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/audit-logs', { params }),
  getById: (id: string) => apiClient.get(`/audit-logs/${id}`),
  export: (params?: Record<string, unknown>) =>
    apiClient.get('/audit-logs/export', { params, responseType: 'blob' }),
};
