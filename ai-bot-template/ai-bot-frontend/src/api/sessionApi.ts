import axiosInstance from '../axios/axios.ts';
import type { BotActionLogResponse, CreateSessionRequest, SessionResponse } from './types/session.ts';

const sessionApi = {
  findAll: async () => {
    return await axiosInstance.get<SessionResponse[]>('/sessions');
  },
  findById: async (id: string) => {
    return await axiosInstance.get<SessionResponse>(`/sessions/${id}`);
  },
  add: async (data: CreateSessionRequest) => {
    return await axiosInstance.post<SessionResponse>('/sessions/add', data);
  },
  start: async (id: string) => {
    return await axiosInstance.post<SessionResponse>(`/sessions/${id}/start`);
  },
  stop: async (id: string) => {
    return await axiosInstance.post<SessionResponse>(`/sessions/${id}/stop`);
  },
  findLogs: async (id: string) => {
    return await axiosInstance.get<BotActionLogResponse[]>(`/sessions/${id}/logs`);
  }
};

export default sessionApi;
