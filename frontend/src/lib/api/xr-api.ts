import apiClient from '@/lib/axios';

export interface XRScene {
  id: string;
  name: string;
  type: 'INTERACTIVE' | 'STATIC' | '360_VIDEO' | 'HYBRID';
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  description?: string;
  thumbnailUrl?: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  hotspots: XRHotspot[];
  assets: string[];
}

export interface XRHotspot {
  id: string;
  sceneId: string;
  name: string;
  type: 'LINK' | 'INFO' | 'VIDEO' | 'AUDIO' | 'IMAGE';
  position: { x: number; y: number; z: number };
  rotation: { x: number; y: number; z: number };
  scale: { x: number; y: number; z: number };
  targetSceneId?: string;
  content?: string;
  assetId?: string;
}

export interface XRAsset {
  id: string;
  name: string;
  type: '360_VIDEO' | '3D_MODEL' | 'AUDIO' | 'IMAGE';
  url: string;
  thumbnailUrl?: string;
  fileSize: number;
  mimeType: string;
  duration?: number;
  metadata?: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export interface XRVideo {
  id: string;
  name: string;
  url: string;
  thumbnailUrl?: string;
  duration: number;
  resolution: string;
  transcodingStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  transcodingProgress: number;
  formats: { quality: string; url: string; size: number }[];
  createdAt: string;
}

export interface XRTour {
  id: string;
  name: string;
  description?: string;
  scenes: string[];
  thumbnailUrl?: string;
  status: 'DRAFT' | 'PUBLISHED';
  viewCount: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface XRAnalytics {
  totalViews: number;
  totalSessions: number;
  avgSessionDuration: number;
  deviceDistribution: { device: string; count: number; percentage: number }[];
  heatmapData: { x: number; y: number; intensity: number }[];
  viewsOverTime: { date: string; views: number; sessions: number }[];
  topScenes: { sceneId: string; name: string; views: number; avgDuration: number }[];
}

export interface XRCollaborationRoom {
  id: string;
  name: string;
  sceneId: string;
  sceneName: string;
  hostUserId: string;
  participants: { userId: string; username: string; joinedAt: string; role: 'HOST' | 'VIEWER' | 'EDITOR' }[];
  maxParticipants: number;
  status: 'WAITING' | 'ACTIVE' | 'ENDED';
  createdAt: string;
  startedAt?: string;
  endedAt?: string;
}

export const xrScenesApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/xr/scenes', { params }),
  getById: (id: string) => apiClient.get(`/xr/scenes/${id}`),
  create: (data: Partial<XRScene>) => apiClient.post('/xr/scenes', data),
  update: (id: string, data: Partial<XRScene>) => apiClient.put(`/xr/scenes/${id}`, data),
  delete: (id: string) => apiClient.delete(`/xr/scenes/${id}`),
  duplicate: (id: string) => apiClient.post(`/xr/scenes/${id}/duplicate`),
  publish: (id: string) => apiClient.post(`/xr/scenes/${id}/publish`),
  unpublish: (id: string) => apiClient.post(`/xr/scenes/${id}/unpublish`),
  getHotspots: (sceneId: string) => apiClient.get(`/xr/scenes/${sceneId}/hotspots`),
  addHotspot: (sceneId: string, data: Partial<XRHotspot>) =>
    apiClient.post(`/xr/scenes/${sceneId}/hotspots`, data),
  updateHotspot: (sceneId: string, hotspotId: string, data: Partial<XRHotspot>) =>
    apiClient.put(`/xr/scenes/${sceneId}/hotspots/${hotspotId}`, data),
  deleteHotspot: (sceneId: string, hotspotId: string) =>
    apiClient.delete(`/xr/scenes/${sceneId}/hotspots/${hotspotId}`),
  getStats: (id: string) => apiClient.get(`/xr/scenes/${id}/stats`),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/scenes/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/scenes/export/excel', { params, responseType: 'blob' }),
};

export const xrAssetsApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/xr/assets', { params }),
  getById: (id: string) => apiClient.get(`/xr/assets/${id}`),
  upload: (formData: FormData, onProgress?: (pct: number) => void) =>
    apiClient.post('/xr/assets/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) onProgress(Math.round((e.loaded * 100) / e.total));
      },
    }),
  delete: (id: string) => apiClient.delete(`/xr/assets/${id}`),
  bulkDelete: (ids: string[]) => apiClient.post('/xr/assets/bulk-delete', ids),
  getByType: (type: string, params?: Record<string, unknown>) =>
    apiClient.get(`/xr/assets/type/${type}`, { params }),
  getStats: () => apiClient.get('/xr/assets/stats'),
};

export const xrVideoApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/xr/videos', { params }),
  getById: (id: string) => apiClient.get(`/xr/videos/${id}`),
  upload: (formData: FormData, onProgress?: (pct: number) => void) =>
    apiClient.post('/xr/videos/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) onProgress(Math.round((e.loaded * 100) / e.total));
      },
    }),
  delete: (id: string) => apiClient.delete(`/xr/videos/${id}`),
  getTranscodingStatus: (id: string) => apiClient.get(`/xr/videos/${id}/transcoding`),
  retryTranscoding: (id: string) => apiClient.post(`/xr/videos/${id}/transcoding/retry`),
  getFormats: (id: string) => apiClient.get(`/xr/videos/${id}/formats`),
};

export const xrToursApi = {
  list: (params?: Record<string, unknown>) => apiClient.get('/xr/tours', { params }),
  getById: (id: string) => apiClient.get(`/xr/tours/${id}`),
  create: (data: Partial<XRTour>) => apiClient.post('/xr/tours', data),
  update: (id: string, data: Partial<XRTour>) => apiClient.put(`/xr/tours/${id}`, data),
  delete: (id: string) => apiClient.delete(`/xr/tours/${id}`),
  publish: (id: string) => apiClient.post(`/xr/tours/${id}/publish`),
  unpublish: (id: string) => apiClient.post(`/xr/tours/${id}/unpublish`),
  addScene: (tourId: string, sceneId: string) =>
    apiClient.post(`/xr/tours/${tourId}/scenes`, { sceneId }),
  removeScene: (tourId: string, sceneId: string) =>
    apiClient.delete(`/xr/tours/${tourId}/scenes/${sceneId}`),
  reorderScenes: (tourId: string, sceneIds: string[]) =>
    apiClient.put(`/xr/tours/${tourId}/scenes/reorder`, { sceneIds }),
  getStats: (id: string) => apiClient.get(`/xr/tours/${id}/stats`),
};

export const xrAnalyticsApi = {
  getOverview: (params?: Record<string, unknown>) => apiClient.get('/xr/analytics/overview', { params }),
  getViewsOverTime: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/analytics/views', { params }),
  getDeviceDistribution: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/analytics/devices', { params }),
  getHeatmap: (sceneId: string, params?: Record<string, unknown>) =>
    apiClient.get(`/xr/analytics/heatmap/${sceneId}`, { params }),
  getSessionDuration: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/analytics/sessions', { params }),
  getTopScenes: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/analytics/top-scenes', { params }),
  exportCsv: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/analytics/export/csv', { params, responseType: 'blob' }),
  exportExcel: (params?: Record<string, unknown>) =>
    apiClient.get('/xr/analytics/export/excel', { params, responseType: 'blob' }),
};

export const xrCollaborationApi = {
  listRooms: (params?: Record<string, unknown>) => apiClient.get('/xr/collaboration/rooms', { params }),
  getRoom: (id: string) => apiClient.get(`/xr/collaboration/rooms/${id}`),
  createRoom: (data: Partial<XRCollaborationRoom>) =>
    apiClient.post('/xr/collaboration/rooms', data),
  joinRoom: (id: string) => apiClient.post(`/xr/collaboration/rooms/${id}/join`),
  leaveRoom: (id: string) => apiClient.post(`/xr/collaboration/rooms/${id}/leave`),
  updateRoom: (id: string, data: Partial<XRCollaborationRoom>) =>
    apiClient.put(`/xr/collaboration/rooms/${id}`, data),
  deleteRoom: (id: string) => apiClient.delete(`/xr/collaboration/rooms/${id}`),
  endRoom: (id: string) => apiClient.post(`/xr/collaboration/rooms/${id}/end`),
  kickParticipant: (roomId: string, userId: string) =>
    apiClient.delete(`/xr/collaboration/rooms/${roomId}/participants/${userId}`),
  updateParticipantRole: (roomId: string, userId: string, role: string) =>
    apiClient.patch(`/xr/collaboration/rooms/${roomId}/participants/${userId}`, { role }),
  getActiveRooms: () => apiClient.get('/xr/collaboration/rooms/active'),
  getRoomStats: () => apiClient.get('/xr/collaboration/stats'),
};

export const xrAiApi = {
  generateScene: (prompt: string) => apiClient.post('/xr/ai/generate-scene', { prompt }),
  suggestHotspots: (sceneId: string) => apiClient.post(`/xr/ai/suggest-hotspots/${sceneId}`),
  autoTag: (assetId: string) => apiClient.post(`/xr/ai/auto-tag/${assetId}`),
  analyzeScene: (sceneId: string) => apiClient.post(`/xr/ai/analyze/${sceneId}`),
  optimizeAsset: (assetId: string) => apiClient.post(`/xr/ai/optimize/${assetId}`),
};
