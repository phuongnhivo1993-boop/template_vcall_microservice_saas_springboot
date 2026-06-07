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
  duplicate: (id: string) => apiClient.post(`/agents/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/agents/bulk-delete', ids),
  updateStatus: (id: string, status: string) =>
    apiClient.patch(`/agents/${id}/status`, { status }),
  getByStatus: (status: string) => apiClient.get(`/agents/status/${status}`),
  getStatusHistory: (id: string) => apiClient.get(`/agents/${id}/status-history`),
  getProfile: (userId: string) => apiClient.get('/agents/profile', { params: { userId } }),
  getStats: () => apiClient.get('/agents/stats'),
  getSessions: (id: string) => apiClient.get(`/agent-sessions/active/${id}`),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/agents/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/agents/export/excel', { params, responseType: 'blob' }),
};

export const customersApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/customers', { params }),
  getById: (id: string) => apiClient.get(`/customers/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/customers', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/customers/${id}`, data),
  delete: (id: string) => apiClient.delete(`/customers/${id}`),
  duplicate: (id: string) => apiClient.post(`/customers/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/customers/bulk-delete', ids),
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
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/customers/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/customers/export/excel', { params, responseType: 'blob' }),
};

export const crmApi = {
  leads: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/leads', { params }),
    getById: (id: string) => apiClient.get(`/crm/leads/${id}`),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/leads', data),
    update: (id: string, data: Record<string, unknown>) => apiClient.put(`/crm/leads/${id}`, data),
    delete: (id: string) => apiClient.delete(`/crm/leads/${id}`),
    duplicate: (id: string) => apiClient.post(`/crm/leads/${id}/duplicate`),
    bulkDelete: (ids: string[]) => apiClient.post('/crm/leads/bulk-delete', ids),
    updateStatus: (id: string, status: string) =>
      apiClient.patch(`/crm/leads/${id}/status`, { status }),
    convert: (id: string, data: Record<string, unknown>) =>
      apiClient.post(`/crm/leads/${id}/convert`, data),
  },
  opportunities: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/opportunities', { params }),
    getById: (id: string) => apiClient.get(`/crm/opportunities/${id}`),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/opportunities', data),
    update: (id: string, data: Record<string, unknown>) => apiClient.put(`/crm/opportunities/${id}`, data),
    delete: (id: string) => apiClient.delete(`/crm/opportunities/${id}`),
    duplicate: (id: string) => apiClient.post(`/crm/opportunities/${id}/duplicate`),
    bulkDelete: (ids: string[]) => apiClient.post('/crm/opportunities/bulk-delete', ids),
    updateStage: (id: string, stage: string) =>
      apiClient.patch(`/crm/opportunities/${id}/stage`, { stage }),
  },
  activities: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/activities', { params }),
    getById: (id: number) => apiClient.get(`/crm/activities/${id}`),
    getByCustomer: (customerId: string, params?: Record<string, unknown>) =>
      apiClient.get(`/crm/activities/customer/${customerId}`, { params }),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/activities', data),
    update: (id: number, data: Record<string, unknown>) => apiClient.put(`/crm/activities/${id}`, data),
    delete: (id: number) => apiClient.delete(`/crm/activities/${id}`),
    bulkDelete: (ids: number[]) => apiClient.post('/crm/activities/bulk-delete', ids),
  },
  notes: {
    list: (params?: Record<string, unknown>) => apiClient.get('/crm/notes', { params }),
    getById: (id: number) => apiClient.get(`/crm/notes/${id}`),
    getByCustomer: (customerId: string, params?: Record<string, unknown>) =>
      apiClient.get(`/crm/notes/customer/${customerId}`, { params }),
    create: (data: Record<string, unknown>) => apiClient.post('/crm/notes', data),
    update: (id: number, data: Record<string, unknown>) => apiClient.put(`/crm/notes/${id}`, data),
    delete: (id: number) => apiClient.delete(`/crm/notes/${id}`),
    bulkDelete: (ids: number[]) => apiClient.post('/crm/notes/bulk-delete', ids),
  },
  exportCsv: (type: string, params?: Record<string, unknown>) =>
    apiClient.get(`/crm/${type}/export/csv`, { params, responseType: 'blob' }),
  exportExcel: (type: string, params?: Record<string, unknown>) =>
    apiClient.get(`/crm/${type}/export/excel`, { params, responseType: 'blob' }),
};

export const callsApi = {
  getAll: (params?: Record<string, unknown>) => apiClient.get('/calls', { params }),
  list: (params?: Record<string, unknown>) => apiClient.get('/calls/active', { params }),
  getById: (id: string) => apiClient.get(`/calls/${id}`),
  getAgentCalls: (agentId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/calls/agent/${agentId}`, { params }),
  create: (data: Record<string, unknown>) => apiClient.post('/calls', data),
  update: (id: string, data: Record<string, unknown>) =>
    apiClient.put(`/calls/${id}`, data),
  delete: (id: string) => apiClient.delete(`/calls/${id}`),
  duplicate: (id: string) => apiClient.post(`/calls/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/calls/bulk-delete', ids),
  updateStatus: (id: string, data: Record<string, unknown>) =>
    apiClient.patch(`/calls/${id}/status`, data),
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
  duplicate: (id: string) => apiClient.post(`/tickets/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/tickets/bulk-delete', ids),
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
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/tickets/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/tickets/export/excel', { params, responseType: 'blob' }),
};

export const reportsApi = {
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
  duplicate: (id: string) => apiClient.post(`/campaigns/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/campaigns/bulk-delete', ids),
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
  bulkDeleteMembers: (campaignId: string, ids: number[]) =>
    apiClient.post(`/campaigns/${campaignId}/members/bulk-delete`, ids),
  importMembers: (campaignId: string, formData: FormData) =>
    apiClient.post(`/campaigns/${campaignId}/members/import`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),
  getResults: (campaignId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/campaigns/${campaignId}/results`, { params }),
  getAgentResults: (campaignId: string, agentId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/campaigns/${campaignId}/results/agent/${agentId}`, { params }),
  searchResults: (campaignId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/campaigns/${campaignId}/results/search`, { params }),
  deleteResult: (campaignId: string, resultId: number) =>
    apiClient.delete(`/campaigns/${campaignId}/results/${resultId}`),
  bulkDeleteResults: (campaignId: string, ids: number[]) =>
    apiClient.post(`/campaigns/${campaignId}/results/bulk-delete`, ids),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/campaigns/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/campaigns/export/excel', { params, responseType: 'blob' }),
};

export const billingApi = {
  getAll: (params?: Record<string, unknown>) => apiClient.get('/billing', { params }),
  getPlans: (params?: Record<string, unknown>) => apiClient.get('/billing/plans', { params }),
  getPlan: (id: number) => apiClient.get(`/billing/plans/${id}`),
  createPlan: (data: Record<string, unknown>) => apiClient.post('/billing/plans', data),
  updatePlan: (id: number, data: Record<string, unknown>) =>
    apiClient.put(`/billing/plans/${id}`, data),
  deletePlan: (id: number) => apiClient.delete(`/billing/plans/${id}`),
  bulkDeletePlans: (ids: number[]) => apiClient.post('/billing/plans/bulk-delete', ids),
  bulkDeleteSubscriptions: (ids: string[]) => apiClient.post('/billing/subscriptions/bulk-delete', ids),
  bulkDeleteInvoices: (ids: string[]) => apiClient.post('/billing/invoices/bulk-delete', ids),
  bulkDeleteUsage: (ids: string[]) => apiClient.post('/billing/usage/bulk-delete', ids),
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
  list: (params?: Record<string, unknown>) => apiClient.get('/audit/logs', { params }),
  getById: (id: string) => apiClient.get(`/audit/logs/${id}`),
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
  list: (params?: Record<string, unknown>) => apiClient.get('/notifications', { params }),
  getById: (id: string) => apiClient.get(`/notifications/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/notifications', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/notifications/${id}`, data),
  delete: (id: string) => apiClient.delete(`/notifications/${id}`),
  bulkDelete: (ids: string[]) => apiClient.post('/notifications/bulk-delete', ids),
  bulkDeleteTemplates: (ids: number[]) => apiClient.post('/notifications/templates/bulk-delete', ids),
  bulkDeletePreferences: (ids: string[]) => apiClient.post('/notifications/preferences/bulk-delete', ids),
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

export const knowledgeBaseApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/knowledge-base/articles', { params }),
  getById: (id: string) => apiClient.get(`/knowledge-base/articles/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/knowledge-base/articles', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/knowledge-base/articles/${id}`, data),
  delete: (id: string) => apiClient.delete(`/knowledge-base/articles/${id}`),
  duplicate: (id: string) => apiClient.post(`/knowledge-base/articles/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/knowledge-base/articles/bulk-delete', ids),
  search: (params?: Record<string, unknown>) => apiClient.get('/knowledge-base/articles/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/knowledge-base/articles/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/knowledge-base/articles/export/excel', { params, responseType: 'blob' }),
  getStats: () => apiClient.get('/knowledge-base/articles/stats'),
};

export const automationApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/automation/rules', { params }),
  getById: (id: string) => apiClient.get(`/automation/rules/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/automation/rules', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/automation/rules/${id}`, data),
  delete: (id: string) => apiClient.delete(`/automation/rules/${id}`),
  duplicate: (id: string) => apiClient.post(`/automation/rules/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/automation/rules/bulk-delete', ids),
  toggle: (id: string, isActive: boolean) =>
    apiClient.patch(`/automation/rules/${id}`, { isActive }),
  search: (params?: Record<string, unknown>) => apiClient.get('/automation/rules/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/automation/rules/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/automation/rules/export/excel', { params, responseType: 'blob' }),
  getStats: () => apiClient.get('/automation/rules/stats'),
};

export const webhooksApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/webhooks', { params }),
  getById: (id: string) => apiClient.get(`/webhooks/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/webhooks', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/webhooks/${id}`, data),
  delete: (id: string) => apiClient.delete(`/webhooks/${id}`),
  duplicate: (id: string) => apiClient.post(`/webhooks/${id}/duplicate`),
  bulkDelete: (ids: string[]) => apiClient.post('/webhooks/bulk-delete', ids),
  test: (id: string) => apiClient.post(`/webhooks/${id}/test`),
  search: (params?: Record<string, unknown>) => apiClient.get('/webhooks/search', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/webhooks/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/webhooks/export/excel', { params, responseType: 'blob' }),
  getStats: () => apiClient.get('/webhooks/stats'),
};

export const schedulingApi = {
  getAppointments: (params?: Record<string, unknown>) => apiClient.get('/appointments', { params }),
  getAppointment: (id: string) => apiClient.get(`/appointments/${id}`),
  createAppointment: (data: Record<string, unknown>) => apiClient.post('/appointments', data),
  updateAppointment: (id: string, data: Record<string, unknown>) => apiClient.put(`/appointments/${id}`, data),
  deleteAppointment: (id: string) => apiClient.delete(`/appointments/${id}`),
  updateAppointmentStatus: (id: string, status: string) =>
    apiClient.patch(`/appointments/${id}/status`, null, { params: { status } }),
  searchAppointments: (params?: Record<string, unknown>) => apiClient.get('/appointments/search', { params }),
  bulkDeleteAppointments: (ids: string[]) => apiClient.post('/appointments/bulk-delete', ids),
  bulkStatusAppointments: (ids: string[], status: string) =>
    apiClient.post('/appointments/bulk-status', { ids, status }),
  getAppointmentsByCustomer: (customerId: string) => apiClient.get(`/appointments/customer/${customerId}`),
  getAppointmentsByAgent: (agentId: string) => apiClient.get(`/appointments/agent/${agentId}`),
  getAppointmentStats: () => apiClient.get('/appointments/stats'),
  exportAppointmentsCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/appointments/export/csv', { params, responseType: 'blob' }),
  exportAppointmentsExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/appointments/export/excel', { params, responseType: 'blob' }),

  getAvailability: (params?: Record<string, unknown>) => apiClient.get('/availability', { params }),
  getAvailabilityById: (id: string) => apiClient.get(`/availability/${id}`),
  createAvailability: (data: Record<string, unknown>) => apiClient.post('/availability', data),
  deleteAvailability: (id: string) => apiClient.delete(`/availability/${id}`),
  updateAvailabilityStatus: (id: string, status: string) =>
    apiClient.patch(`/availability/${id}/status`, null, { params: { status } }),
  toggleBooked: (id: string) => apiClient.patch(`/availability/${id}/toggle-booked`),
  checkAvailability: (data: Record<string, unknown>) => apiClient.post('/availability/check', data),
  getAvailabilityByAgent: (agentId: string) => apiClient.get(`/availability/agent/${agentId}`),
  getAvailabilityByAgentAndDate: (agentId: string, date: string) =>
    apiClient.get(`/availability/agent/${agentId}/date/${date}`),
  getAvailabilityByAgentAndRange: (agentId: string, startDate: string, endDate: string) =>
    apiClient.get(`/availability/agent/${agentId}/range`, { params: { startDate, endDate } }),
  searchAvailability: (params?: Record<string, unknown>) => apiClient.get('/availability/search', { params }),
  exportAvailabilityCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/availability/export/csv', { params, responseType: 'blob' }),
  exportAvailabilityExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/availability/export/excel', { params, responseType: 'blob' }),

  getTemplates: (params?: Record<string, unknown>) => apiClient.get('/schedule-templates', { params }),
  getTemplate: (id: string) => apiClient.get(`/schedule-templates/${id}`),
  createTemplate: (data: Record<string, unknown>) => apiClient.post('/schedule-templates', data),
  updateTemplate: (id: string, data: Record<string, unknown>) => apiClient.put(`/schedule-templates/${id}`, data),
  deleteTemplate: (id: string) => apiClient.delete(`/schedule-templates/${id}`),
  searchTemplates: (params?: Record<string, unknown>) => apiClient.get('/schedule-templates/search', { params }),
  getTemplatesByAgent: (agentId: string) => apiClient.get(`/schedule-templates/agent/${agentId}`),
  getTemplatesByAgentAndDay: (agentId: string, dayOfWeek: string) =>
    apiClient.get(`/schedule-templates/agent/${agentId}/day/${dayOfWeek}`),
  getActiveTemplatesByAgent: (agentId: string) => apiClient.get(`/schedule-templates/agent/${agentId}/active`),
  bulkDeleteTemplates: (ids: string[]) => apiClient.post('/schedule-templates/bulk-delete', ids),
  exportTemplatesCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/schedule-templates/export/csv', { params, responseType: 'blob' }),
  exportTemplatesExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/schedule-templates/export/excel', { params, responseType: 'blob' }),
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

export const surveyApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/surveys', { params }),
  getById: (id: string) => apiClient.get(`/surveys/${id}`),
  create: (data: Record<string, unknown>) => apiClient.post('/surveys', data),
  update: (id: string, data: Record<string, unknown>) => apiClient.put(`/surveys/${id}`, data),
  delete: (id: string) => apiClient.delete(`/surveys/${id}`),
  bulkDelete: (ids: string[]) => apiClient.post('/surveys/bulk-delete', ids),
  search: (params?: Record<string, unknown>) => apiClient.get('/surveys/search', { params }),
  getStats: (id: string) => apiClient.get(`/surveys/${id}/stats`),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/surveys/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/surveys/export/excel', { params, responseType: 'blob' }),

  getTemplates: (params?: Record<string, unknown>) => apiClient.get('/survey-templates', { params }),
  getTemplateById: (id: string) => apiClient.get(`/survey-templates/${id}`),
  createTemplate: (data: Record<string, unknown>) => apiClient.post('/survey-templates', data),
  updateTemplate: (id: string, data: Record<string, unknown>) => apiClient.put(`/survey-templates/${id}`, data),
  deleteTemplate: (id: string) => apiClient.delete(`/survey-templates/${id}`),
  bulkDeleteTemplates: (ids: string[]) => apiClient.post('/survey-templates/bulk-delete', ids),
  searchTemplates: (params?: Record<string, unknown>) => apiClient.get('/survey-templates/search', { params }),

  getQuestions: (surveyId: string) => apiClient.get(`/survey-questions/by-survey/${surveyId}`),
  getQuestionById: (id: string) => apiClient.get(`/survey-questions/${id}`),
  createQuestion: (data: Record<string, unknown>) => apiClient.post('/survey-questions', data),
  updateQuestion: (id: string, data: Record<string, unknown>) => apiClient.put(`/survey-questions/${id}`, data),
  deleteQuestion: (id: string) => apiClient.delete(`/survey-questions/${id}`),

  getResponses: (params?: Record<string, unknown>) => apiClient.get('/survey-responses', { params }),
  getResponseById: (id: string) => apiClient.get(`/survey-responses/${id}`),
  searchResponses: (params?: Record<string, unknown>) => apiClient.get('/survey-responses/search', { params }),
  exportResponsesCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/survey-responses/export/csv', { params, responseType: 'blob' }),
  exportResponsesExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/survey-responses/export/excel', { params, responseType: 'blob' }),
};
