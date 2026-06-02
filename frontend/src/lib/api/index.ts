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
  getByUsername: (username: string) => apiClient.get('/users/by-username', { params: { username } }),
  create: (data: Record<string, unknown>) => apiClient.post('/users', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/users/${id}`, data),
  delete: (id: string) => apiClient.delete(`/users/${id}`),
  updateStatus: (id: string, status: string) =>
    apiClient.patch(`/users/${id}/status`, { status }),
};

export const rolesApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/roles', { params }),
  getById: (id: number) => apiClient.get(`/roles/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/roles', data),
  update: (id: number, data: Record<string, unknown>) => apiClient.put(`/roles/${id}`, data),
  delete: (id: number) => apiClient.delete(`/roles/${id}`),
  assignUser: (roleId: number, userId: string) =>
    apiClient.post(`/roles/${roleId}/users/${userId}`),
  removeUser: (roleId: number, userId: string) =>
    apiClient.delete(`/roles/${roleId}/users/${userId}`),
};

export const agentsApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/agents', { params }),
  getById: (id: string) => apiClient.get(`/agents/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/agents', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/agents/${id}`, data),
  delete: (id: string) => apiClient.delete(`/agents/${id}`),
  updateStatus: (id: string, status: string) =>
    apiClient.patch(`/agents/${id}/status`, { status }),
  getByStatus: (status: string) => apiClient.get(`/agents/status/${status}`),
  getStatusHistory: (id: string) => apiClient.get(`/agents/${id}/status-history`),
  getProfile: (userId: string) => apiClient.get('/agents/profile', { params: { userId } }),
  getStats: () => apiClient.get('/agents/stats'),
  getSessions: (id: string) => apiClient.get(`/agent-sessions/active/${id}`),
};

export const customersApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/customers', { params }),
  getById: (id: string) => apiClient.get(`/customers/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/customers', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/customers/${id}`, data),
  delete: (id: string) => apiClient.delete(`/customers/${id}`),
  search: (keyword: string, params?: Record<string, unknown>) =>
    apiClient.get('/customers/search', { params: { keyword, ...params } }),
  addContact: (id: string, data: Record<string, unknown>) =>
    apiClient.post(`/customers/${id}/contacts`, data),
  updateContact: (customerId: string, contactId: number, data: Record<string, unknown>) =>
    apiClient.put(`/customers/${customerId}/contacts/${contactId}`, data),
  deleteContact: (customerId: string, contactId: number) =>
    apiClient.delete(`/customers/${customerId}/contacts/${contactId}`),
  addAddress: (id: string, data: Record<string, unknown>) =>
    apiClient.post(`/customers/${id}/addresses`, data),
  updateAddress: (customerId: string, addressId: number, data: Record<string, unknown>) =>
    apiClient.put(`/customers/${customerId}/addresses/${addressId}`, data),
  deleteAddress: (customerId: string, addressId: number) =>
    apiClient.delete(`/customers/${customerId}/addresses/${addressId}`),
};

export const crmApi = {
  leads: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/leads', { params }),
    getById: (id: string) => apiClient.get(`/crm/leads/${id}`),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/leads', data),
    update: (id: string, data: Record<string, unknown>) => apiClient.put(`/crm/leads/${id}`, data),
    delete: (id: string) => apiClient.delete(`/crm/leads/${id}`),
    updateStatus: (id: string, status: string) =>
      apiClient.patch(`/crm/leads/${id}/status`, status, {
        headers: { 'Content-Type': 'application/json' },
      }),
    convert: (id: string, data: Record<string, unknown>) =>
      apiClient.post(`/crm/leads/${id}/convert`, data),
  },
  opportunities: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/opportunities', { params }),
    getById: (id: string) => apiClient.get(`/crm/opportunities/${id}`),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/opportunities', data),
    update: (id: string, data: Record<string, unknown>) => apiClient.put(`/crm/opportunities/${id}`, data),
    delete: (id: string) => apiClient.delete(`/crm/opportunities/${id}`),
    updateStage: (id: string, stage: string) =>
      apiClient.patch(`/crm/opportunities/${id}/stage`, stage, {
        headers: { 'Content-Type': 'application/json' },
      }),
  },
  activities: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/activities', { params }),
    getById: (id: number) => apiClient.get(`/crm/activities/${id}`),
    getByCustomer: (customerId: string, params?: Record<string, unknown>) =>
      apiClient.get(`/crm/activities/customer/${customerId}`, { params }),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/activities', data),
    update: (id: number, data: Record<string, unknown>) => apiClient.put(`/crm/activities/${id}`, data),
    delete: (id: number) => apiClient.delete(`/crm/activities/${id}`),
  },
  notes: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/notes', { params }),
    getById: (id: number) => apiClient.get(`/crm/notes/${id}`),
    getByCustomer: (customerId: string, params?: Record<string, unknown>) =>
      apiClient.get(`/crm/notes/customer/${customerId}`, { params }),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/notes', data),
    update: (id: number, data: Record<string, unknown>) => apiClient.put(`/crm/notes/${id}`, data),
    delete: (id: number) => apiClient.delete(`/crm/notes/${id}`),
  },
};

export const callsApi = {
  getAll: (params?: Record<string, unknown>) => apiClient.get('/calls', { params }),
  list: (params?: Record<string, unknown>) => apiClient.get('/calls/active', { params }),
  getById: (id: string) => apiClient.get(`/calls/${id}`),
  getAgentCalls: (agentId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/calls/agent/${agentId}`, { params }),
  start: (data: Record<string, unknown>) => apiClient.post('/calls', data),
  create: (data: Record<string, unknown>) => apiClient.post('/calls', data),
  update: (id: string, data: Record<string, unknown>) =>
    apiClient.put(`/calls/${id}`, data),
  delete: (id: string) => apiClient.delete(`/calls/${id}`),
  updateStatus: (id: string, data: Record<string, unknown>) =>
    apiClient.put(`/calls/${id}/status`, data),
  end: (id: string) => apiClient.post(`/calls/${id}/hangup`),
  mute: (id: string) => apiClient.post(`/calls/${id}/mute`),
  unmute: (id: string) => apiClient.post(`/calls/${id}/unmute`),
  hold: (id: string) => apiClient.post(`/calls/${id}/hold`),
  resume: (id: string) => apiClient.post(`/calls/${id}/resume`),
  transfer: (id: string, data: Record<string, unknown>) =>
    apiClient.post(`/calls/${id}/transfer`, data),
  getRecording: (id: string) => apiClient.get(`/recordings/${id}`),
  search: (params?: Record<string, unknown>) => apiClient.get('/calls/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/calls/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/calls/export/excel', { params, responseType: 'blob' }),
  getStats: (params?: Record<string, unknown>) => apiClient.get('/cdr/analytics', { params }),
  getQueues: (params?: Record<string, unknown>) => apiClient.get('/call-queues', { params }),
  getQueue: (id: number) => apiClient.get(`/call-queues/${id}`),
  getIvrFlows: () => apiClient.get('/ivr-flows'),
  getIvrFlow: (id: number) => apiClient.get(`/ivr-flows/${id}`),
  getRoutingRules: () => apiClient.get('/routing-rules'),
};

export const ticketsApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/tickets', { params }),
  getById: (id: string) => apiClient.get(`/tickets/${id}`),
  getByNumber: (number: string) => apiClient.get(`/tickets/number/${number}`),
  create: (data: Record<string, unknown>) => apiClient.post('/tickets', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/tickets/${id}`, data),
  delete: (id: string) => apiClient.delete(`/tickets/${id}`),
  updateStatus: (id: string, status: string) =>
    apiClient.patch(`/tickets/${id}/status`, { status }),
  assign: (id: string, agentId: string) =>
    apiClient.put(`/tickets/${id}/assign`, { assignedTo: agentId }),
  escalate: (id: string, reason?: string) =>
    apiClient.post(`/tickets/${id}/escalate`, null, { params: { reason: reason || 'Escalated' } }),
  search: (params?: Record<string, unknown>) => apiClient.get('/tickets/search', { params }),
  getStats: () => apiClient.get('/tickets/stats/by-status'),
  getComments: (ticketId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/tickets/${ticketId}/comments`, { params }),
  addComment: (ticketId: string, data: Record<string, unknown>) =>
    apiClient.post(`/tickets/${ticketId}/comments`, data),
  getSlaRules: () => apiClient.get('/sla-rules'),
  getSlaRule: (id: number) => apiClient.get(`/sla-rules/${id}`),
  createSlaRule: (data: Record<string, unknown>) => apiClient.post('/sla-rules', data),
  updateSlaRule: (id: number, data: Record<string, unknown>) =>
    apiClient.put(`/sla-rules/${id}`, data),
  deleteSlaRule: (id: number) => apiClient.delete(`/sla-rules/${id}`),
};

export const reportsApi = {
  getAll: (params?: Record<string, unknown>) => apiClient.get('/reports/definitions', { params }),
  list: (params?: Record<string, unknown>) => apiClient.get('/reports/definitions', { params }),
  getById: (id: number) => apiClient.get(`/reports/definitions/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/reports/definitions', data),
  update: (id: number, data: Record<string, unknown>) =>
    apiClient.put(`/reports/definitions/${id}`, data),
  delete: (id: number) => apiClient.delete(`/reports/definitions/${id}`),
  execute: (id: number) => apiClient.post(`/reports/definitions/${id}/execute`),
  getExecutionHistory: (id: number) => apiClient.get(`/reports/executions/definition/${id}`),
  getAgentPerformance: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/agent-performance/summary', { params }),
  getDashboardWidgets: () => apiClient.get('/reports/dashboard/widgets'),
  getAnalytics: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/analytics', { params }),
  search: (params?: Record<string, unknown>) => apiClient.get('/reports/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/export/excel', { params, responseType: 'blob' }),
  getStats: () => apiClient.get('/reports/stats'),
  getSchedules: (params?: Record<string, unknown>) =>
    apiClient.get('/reports/schedules', { params }),
  createSchedule: (data: Record<string, unknown>) =>
    apiClient.post('/reports/schedules', data),
  updateSchedule: (id: number, data: Record<string, unknown>) =>
    apiClient.put(`/reports/schedules/${id}`, data),
  deleteSchedule: (id: number) =>
    apiClient.delete(`/reports/schedules/${id}`),
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
  getStats: (id: string) => apiClient.get(`/campaigns/${id}/stats`),
  getMembers: (campaignId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/campaigns/${campaignId}/members`, { params }),
  getMember: (campaignId: string, memberId: number) =>
    apiClient.get(`/campaigns/${campaignId}/members/${memberId}`),
  addMember: (campaignId: string, data: Record<string, unknown>) =>
    apiClient.post(`/campaigns/${campaignId}/members`, data),
  removeMember: (campaignId: string, memberId: number) =>
    apiClient.delete(`/campaigns/${campaignId}/members/${memberId}`),
  importMembers: (campaignId: string, formData: FormData) =>
    apiClient.post(`/campaigns/${campaignId}/members/import`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),
  getResults: (campaignId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/campaigns/${campaignId}/results`, { params }),
  getAgentResults: (campaignId: string, agentId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/campaigns/${campaignId}/results/agent/${agentId}`, { params }),
};

export const billingApi = {
  getAll: (params?: Record<string, unknown>) => apiClient.get('/billing', { params }),
  getPlans: (params?: Record<string, unknown>) => apiClient.get('/billing/plans', { params }),
  getPlan: (id: number) => apiClient.get(`/billing/plans/${id}`),
  createPlan: (data: Record<string, unknown>) => apiClient.post('/billing/plans', data),
  updatePlan: (id: number, data: Record<string, unknown>) =>
    apiClient.put(`/billing/plans/${id}`, data),
  deletePlan: (id: number) => apiClient.delete(`/billing/plans/${id}`),
  getActivePlans: (params?: Record<string, unknown>) =>
    apiClient.get('/billing/plans/active', { params }),
  getSubscriptions: (subscriberId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/billing/subscriptions/subscriber/${subscriberId}`, { params }),
  getSubscription: (id: string) => apiClient.get(`/billing/subscriptions/${id}`),
  createSubscription: (data: Record<string, unknown>) =>
    apiClient.post('/billing/subscriptions', data),
  cancelSubscription: (id: string) => apiClient.post(`/billing/subscriptions/${id}/cancel`),
  renewSubscription: (id: string) => apiClient.post(`/billing/subscriptions/${id}/renew`),
  getUsage: (subscriberId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/billing/usage/${subscriberId}`, { params }),
  getUsageSummary: (subscriberId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/billing/usage/${subscriberId}/summary`, { params }),
  getInvoices: (subscriberId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/billing/invoices/subscriber/${subscriberId}`, { params }),
  getInvoice: (id: string) => apiClient.get(`/billing/invoices/${id}`),
  generateInvoice: (params: Record<string, unknown>) =>
    apiClient.post('/billing/invoices/generate', null, { params }),
  payInvoice: (id: string) => apiClient.post(`/billing/invoices/${id}/pay`),
  search: (params?: Record<string, unknown>) => apiClient.get('/billing/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/billing/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/billing/export/excel', { params, responseType: 'blob' }),
  getStats: () => apiClient.get('/billing/stats'),
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
  getAll: (params?: Record<string, unknown>) => apiClient.get('/audit/logs', { params }),
  list: (params?: Record<string, unknown>) => apiClient.get('/audit/logs', { params }),
  getById: (id: string) => apiClient.get(`/audit/logs/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/audit/logs', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/audit/logs/${id}`, data),
  delete: (id: string) => apiClient.delete(`/audit/logs/${id}`),
  search: (params?: Record<string, unknown>) => apiClient.get('/audit/logs/search', { params }),
  getByActor: (actorId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/audit/logs/actor/${actorId}`, { params }),
  getByResource: (resourceType: string, resourceId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/audit/logs/resource/${resourceType}/${resourceId}`, { params }),
  getSecurityLogs: (params?: Record<string, unknown>) =>
    apiClient.get('/audit/security-logs', { params }),
  getFraudAlerts: (params?: Record<string, unknown>) =>
    apiClient.get('/audit/fraud-alerts', { params }),
  updateFraudAlertStatus: (id: string, status: string) =>
    apiClient.patch(`/audit/fraud-alerts/${id}/status`, { status }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/audit/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/audit/export/excel', { params, responseType: 'blob' }),
  exportLogs: (params?: Record<string, unknown>) =>
    apiClient.get('/audit/logs', { params, responseType: 'blob' }),
  getReconciliations: (params?: Record<string, unknown>) =>
    apiClient.get('/audit/reconciliations', { params }),
  runReconciliation: (data: Record<string, unknown>) =>
    apiClient.post('/audit/reconciliations', data),
  getStats: () => apiClient.get('/audit/stats'),
};

export const dashboardApi = {
  getStats: () => apiClient.get('/dashboard/stats'),
  getRecentCalls: (params?: Record<string, unknown>) => apiClient.get('/calls/recent', { params }),
  getAgentStatusDistribution: () => apiClient.get('/agents/stats/status-distribution'),
  getTicketStatusBreakdown: () => apiClient.get('/tickets/stats/by-status'),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/dashboard/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/dashboard/export/excel', { params, responseType: 'blob' }),
};

export const notificationsApi = {
  getAll: (params?: Record<string, unknown>) => apiClient.get('/notifications', { params }),
  list: (params?: Record<string, unknown>) => apiClient.get('/notifications', { params }),
  getById: (id: string) => apiClient.get(`/notifications/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/notifications', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/notifications/${id}`, data),
  delete: (id: string) => apiClient.delete(`/notifications/${id}`),
  getByRecipient: (recipientId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/notifications/recipient/${recipientId}`, { params }),
  getUnread: (recipientId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/notifications/recipient/${recipientId}/unread`, { params }),
  markAsRead: (id: string) => apiClient.patch(`/notifications/${id}/read`),
  markAllAsRead: (recipientId: string) =>
    apiClient.patch(`/notifications/recipient/${recipientId}/read-all`),
  send: (data: Record<string, unknown>) => apiClient.post('/notifications/send', data),
  sendBatch: (data: Record<string, unknown>) => apiClient.post('/notifications/send-batch', data),
  getTemplates: (params?: Record<string, unknown>) =>
    apiClient.get('/notifications/templates', { params }),
  createTemplate: (data: Record<string, unknown>) =>
    apiClient.post('/notifications/templates', data),
  updateTemplate: (id: number, data: Record<string, unknown>) =>
    apiClient.put(`/notifications/templates/${id}`, data),
  deleteTemplate: (id: number) => apiClient.delete(`/notifications/templates/${id}`),
  getPreferences: (userId: string) =>
    apiClient.get(`/notifications/preferences/${userId}`),
  updatePreferences: (userId: string, data: Record<string, unknown>) =>
    apiClient.put(`/notifications/preferences/${userId}`, data),
  search: (params?: Record<string, unknown>) => apiClient.get('/notifications/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/notifications/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/notifications/export/excel', { params, responseType: 'blob' }),
  getStats: () => apiClient.get('/notifications/stats'),
};

export const chatApi = {
  getConversations: (params?: Record<string, unknown>) =>
    apiClient.get('/chat/conversations', { params }),
  getConversation: (id: string) => apiClient.get(`/chat/conversations/${id}`),
  createConversation: (data: Record<string, unknown>) =>
    apiClient.post('/chat/conversations', data),
  updateConversation: (id: string, data: Record<string, unknown>) =>
    apiClient.put(`/chat/conversations/${id}`, data),
  deleteConversation: (id: string) =>
    apiClient.delete(`/chat/conversations/${id}`),
  getMessages: (conversationId: string) =>
    apiClient.get(`/chat/conversations/${conversationId}/messages`),
  sendMessage: (conversationId: string, data: Record<string, unknown>) =>
    apiClient.post(`/chat/conversations/${conversationId}/messages`, data),
  search: (params?: Record<string, unknown>) =>
    apiClient.get('/chat/conversations/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/chat/conversations/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/chat/conversations/export/excel', { params, responseType: 'blob' }),
};
