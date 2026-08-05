import axiosInstance from '../axios/axios.ts';
import type { PageResponse, PostFilter, PostResponse } from './types/post.ts';

const postApi = {
  findAll: async (filter: PostFilter, page: number, size: number) => {
    return await axiosInstance.get<PageResponse<PostResponse>>('/posts', {
      params: { ...filter, page, size }
    });
  },
  findById: async (id: string) => {
    return await axiosInstance.get<PostResponse>(`/posts/${id}`);
  },
  delete: async (id: string) => {
    return await axiosInstance.delete<PostResponse>(`/posts/${id}/delete`);
  }
};

export default postApi;
