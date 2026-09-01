import { ArrowBack, Launch } from '@mui/icons-material';
import { Box, Button, Chip, CircularProgress, Link, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import postApi from '../../../../api/postApi.ts';
import type { PostResponse } from '../../../../api/types/post.ts';

/**
 * TODO(student): Show one extracted post in full: the complete content, its
 * media items (images/videos), the source link, the language confidence and
 * its donation status (postApi.findById).
 */
const PostDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [post, setPost] = useState<PostResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    postApi.findById(id).then((response) => setPost(response.data)).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress/></Box>;
  if (!post) return <Typography>Post not found.</Typography>;

  return (
    <Box>
      <Button startIcon={<ArrowBack/>} onClick={() => navigate('/posts')} sx={{ mb: 2 }}>Back to posts</Button>
      <Typography variant='h5' gutterBottom>Post #{id}</Typography>
      <Stack direction='row' spacing={1} sx={{ mb: 2 }}>
        <Chip label={post.socialNetwork}/>
        <Chip color='success' variant='outlined' label={`${Math.round((post.macedonianConfidence ?? 0) * 100)}% Macedonian`}/>
      </Stack>
      <Typography variant='subtitle1'>{post.authorHandle || 'Unknown author'}</Typography>
      <Typography sx={{ whiteSpace: 'pre-wrap', my: 2 }}>{post.content || 'No text content.'}</Typography>
      {post.sourceUrl && <Link href={post.sourceUrl} target='_blank' rel='noreferrer' sx={{ display: 'inline-flex', gap: 0.5, alignItems: 'center' }}><Launch fontSize='small'/> Open source</Link>}
    </Box>
  );
};

export default PostDetailsPage;
