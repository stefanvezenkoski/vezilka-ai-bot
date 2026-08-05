import { useState } from 'react';
import type { PageResponse, PostFilter, PostResponse } from '../api/types/post.ts';

/**
 * TODO(student): Load a page of extracted posts (postApi.findAll) for the
 * PostsPage, re-fetching whenever the filter or page changes, with
 * loading/error state and a delete action (postApi.delete).
 */
const usePosts = (filter: PostFilter, page: number, size: number) => {
  void filter;
  void page;
  void size;

  const [posts] = useState<PageResponse<PostResponse> | null>(null);
  const [loading] = useState<boolean>(false);

  const onDelete = async (id: number) => {
    void id;
  };

  return { posts, loading, onDelete };
};

export default usePosts;
