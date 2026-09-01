import { Launch } from '@mui/icons-material';
import { Box, Button, Card, CardActions, CardContent, Chip, Link, Typography } from '@mui/material';
import { useNavigate } from 'react-router';
import type { PostResponse } from '../../../../api/types/post.ts';

interface PostCardProps {
  post: PostResponse;
}

/**
 * TODO(student): Show the extracted post: author, content preview, source
 * link, the Macedonian-language confidence, media thumbnails, and whether it
 * is already part of a donation batch. Add navigation to /posts/{id} and a
 * delete action (usePosts().onDelete).
 */
const PostCard = ({ post }: PostCardProps) => {
  const navigate = useNavigate();
  const preview = post.content && post.content.length > 240 ? `${post.content.slice(0, 240)}…` : post.content;
  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, mb: 1 }}>
          <Typography variant='subtitle2'>{post.authorHandle ?? 'Unknown author'}</Typography>
          <Chip label={`${Math.round((post.macedonianConfidence ?? 0) * 100)}% MK`} size='small' color='success' variant='outlined'/>
        </Box>
        <Typography variant='body2' sx={{ whiteSpace: 'pre-wrap' }}>{preview || 'No text content.'}</Typography>
        <Typography variant='caption' color='text.secondary' sx={{ mt: 1, display: 'block' }}>
          {post.postedAt ? new Date(post.postedAt).toLocaleString() : 'Date unavailable'}
        </Typography>
        {post.sourceUrl && <Link href={post.sourceUrl} target='_blank' rel='noreferrer' variant='caption' sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.25, mt: 0.5 }}><Launch fontSize='inherit'/> Source</Link>}
      </CardContent>
      <CardActions>
        <Button size='small' onClick={() => navigate(`/posts/${post.id}`)}>Details</Button>
        {post.donationBatchId && <Chip label={`Batch #${post.donationBatchId}`} size='small' color='primary'/>}
      </CardActions>
    </Card>
  );
};

export default PostCard;
