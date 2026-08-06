import { useCallback, useEffect, useState } from 'react';
import postApi from '../api/postApi.ts';
import type { PageResponse, PostFilter, PostResponse } from '../api/types/post.ts';

const usePosts = (filter: PostFilter, page: number, size: number) => {
  const [posts, setPosts] = useState<PageResponse<PostResponse> | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  const fetchPosts = useCallback(async () => {
    setLoading(true);
    try {
      const response = await postApi.findAll(filter, page, size);
      setPosts(response.data);
    } catch (error) {
      console.error('Failed to fetch posts', error);
    } finally {
      setLoading(false);
    }
  }, [filter, page, size]);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  const onDelete = async (id: number) => {
    try {
      await postApi.delete(id.toString());
      await fetchPosts();
    } catch (error) {
      console.error(`Failed to delete post ${id}`, error);
    }
  };

  return { posts, loading, onDelete, refetch: fetchPosts };
};

export default usePosts;
