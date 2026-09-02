import { Box, CircularProgress, Pagination, Stack, TextField, Typography } from '@mui/material';
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
  const [jumpPageInput, setJumpPageInput] = useState<string>('');

  useEffect(() => {
    const sessionIdParam = searchParams.get('sessionId');
    if (sessionIdParam) {
      setFilter((prev) => ({ ...prev, sessionId: Number(sessionIdParam) }));
    }
  }, [searchParams]);

  const { posts, loading } = usePosts(filter, page, 12);

  const handleJumpPageSubmit = () => {
    const targetPage = Number(jumpPageInput);
    if (posts && targetPage >= 1 && targetPage <= posts.totalPages) {
      setPage(targetPage - 1);
      setJumpPageInput('');
    }
  };

  return (
    <Box sx={{ pb: 4 }}>
      <Typography variant='h5' sx={{ mb: 2 }}>Extracted Posts</Typography>
      
      <PostFilters
        filter={filter}
        onChange={(nextFilter) => {
          setPage(0);
          setFilter(nextFilter);
        }}
      />

      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {!loading && (!posts || posts.content.length === 0) && (
        <Typography color='text.secondary'>
          No extracted posts match your current filter. Run an extraction session or clear filters.
        </Typography>
      )}

      {!loading && posts && <PostGrid posts={posts.content} />}

      {!loading && posts && posts.totalPages > 1 && (
        <Stack
          direction='row'
          spacing={2}
          sx={{ mt: 4, mb: 2, justifyContent: 'center', alignItems: 'center', flexWrap: 'wrap' }}
        >
          <Pagination
            count={posts.totalPages}
            page={page + 1}
            onChange={(_, value) => setPage(value - 1)}
            color='primary'
            showFirstButton
            showLastButton
            size='large'
          />

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant='body2' color='text.secondary'>
              Jump to page:
            </Typography>
            <TextField
              size='small'
              type='number'
              placeholder='No.'
              value={jumpPageInput}
              onChange={(e) => setJumpPageInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  handleJumpPageSubmit();
                }
              }}
              sx={{ width: 80 }}
              inputProps={{ min: 1, max: posts.totalPages }}
            />
          </Box>
        </Stack>
      )}
    </Box>
  );
};

export default PostsPage;
