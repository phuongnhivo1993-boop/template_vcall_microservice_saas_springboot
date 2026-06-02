import apiClient from '@/lib/axios';

export const workspaceApi = {
  getStatus: () => apiClient.get('/agents/profile'),
  updateStatus: (status: string) => apiClient.patch('/agents/status', { status }),
  getNotifications: (params?: Record<string, unknown>) =>
    apiClient.get('/notifications/recipient/current', { params }),
  markNotificationRead: (id: string) => apiClient.patch(`/notifications/${id}/read`),
};
