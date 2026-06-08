import axios from 'axios';
import { signOut } from 'next-auth/react';

const apiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  async (config) => {
    if (typeof window !== 'undefined') {
      const { getSession } = await import('next-auth/react');
      const session = await getSession();
      if (session?.accessToken) {
        config.headers.Authorization = `Bearer ${session.accessToken}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error),
);

apiClient.interceptors.response.use(
  (response) => {
    if (response.data && typeof response.data === 'object' && 'status' in response.data && 'data' in response.data) {
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      signOut({ callbackUrl: '/auth/login' });
    }
    return Promise.reject(error);
  },
);

export default apiClient;
