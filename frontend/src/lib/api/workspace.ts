import apiClient from '@/lib/axios';

export const workspaceApi = {
  getStatus: () => apiClient.get('/agents/profile'),
  updateStatus: (id: string, status: string) => apiClient.patch(`/agents/${id}/status`, { status }),
  getNotifications: (recipientId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/notifications/recipient/${recipientId}`, { params }),
  markNotificationRead: (id: string) => apiClient.patch(`/notifications/${id}/read`),
};
