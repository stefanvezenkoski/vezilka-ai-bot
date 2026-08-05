import axiosInstance from '../axios/axios.ts';
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from './types/user.ts';

const userApi = {
  register: async (data: RegisterRequest) => {
    return await axiosInstance.post<RegisterResponse>('/user/register', data);
  },
  login: async (data: LoginRequest) => {
    return await axiosInstance.post<LoginResponse>('/user/login', data);
  }
};

export default userApi;
