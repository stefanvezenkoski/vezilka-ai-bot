import { Box, Button, CircularProgress, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';
import type { PostFilter } from '../../../../api/types/post.ts';
import usePosts from '../../../../hooks/usePosts.ts';
import PostFilters from '../../../components/post/PostFilters/PostFilters.tsx';
import PostGrid from '../../../components/post/PostGrid/PostGrid.tsx';

const PostsPage = () => {
  const [searchParams] = useSearchParams();
  const [filter, setFilter] = useState<PostFilter>({});
  const [page, setPage] = useState<number>(0);

  useEffect(() => {
    const sessionIdParam = searchParams.get('sessionId');
    if (sessionIdParam) {
      setFilter((prev) => ({ ...prev, sessionId: Number(sessionIdParam) }));
    }
  }, [searchParams]);

  const { posts, loading } = usePosts(filter, page, 12);

  return (
    <Box>
      <Typography variant='h5' sx={{ mb: 2 }}>Extracted Posts</Typography>
      <PostFilters filter={filter} onChange={(nextFilter) => { setPage(0); setFilter(nextFilter); }}/>
      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress/>
        </Box>
      )}
      {!loading && (!posts || posts.content.length === 0) && (
        <Typography color='text.secondary'>
          No extracted posts match your current filter. Run an extraction session or clear filters.
        </Typography>
      )}
      {!loading && posts && <PostGrid posts={posts.content}/>}
      {!loading && posts && posts.totalPages > 1 && (
        <Stack direction='row' spacing={1} sx={{ mt: 3, justifyContent: 'center' }}>
          <Button disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</Button>
          <Typography sx={{ alignSelf: 'center' }}>Page {page + 1} of {posts.totalPages}</Typography>
          <Button disabled={page + 1 >= posts.totalPages} onClick={() => setPage(page + 1)}>Next</Button>
        </Stack>
      )}
    </Box>
  );
};

export default PostsPage;
