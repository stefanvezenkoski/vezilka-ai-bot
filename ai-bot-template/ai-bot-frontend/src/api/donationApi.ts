import axiosInstance from '../axios/axios.ts';
import type { CreateDonationBatchRequest, DonationBatchResponse } from './types/donation.ts';

const donationApi = {
  findAll: async () => {
    return await axiosInstance.get<DonationBatchResponse[]>('/donations');
  },
  findById: async (id: string) => {
    return await axiosInstance.get<DonationBatchResponse>(`/donations/${id}`);
  },
  add: async (data: CreateDonationBatchRequest) => {
    return await axiosInstance.post<DonationBatchResponse>('/donations/add', data);
  },
  approve: async (id: string) => {
    return await axiosInstance.post<DonationBatchResponse>(`/donations/${id}/approve`);
  },
  submit: async (id: string) => {
    return await axiosInstance.post<DonationBatchResponse>(`/donations/${id}/submit`);
  }
};

export default donationApi;
