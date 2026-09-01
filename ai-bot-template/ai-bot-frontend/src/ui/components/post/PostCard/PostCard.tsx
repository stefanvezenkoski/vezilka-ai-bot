import { Launch } from '@mui/icons-material';
import { Box, Button, Card, CardActions, CardContent, Chip, Link, Typography } from '@mui/material';
import { useNavigate } from 'react-router';
import type { PostResponse } from '../../../../api/types/post.ts';

interface PostCardProps {
  post: PostResponse;
}

const PostCard = ({ post }: PostCardProps) => {
  const navigate = useNavigate();
  const preview = post.content && post.content.length > 240 ? `${post.content.slice(0, 240)}…` : post.content;

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 2, boxShadow: 2 }}>
      <CardContent sx={{ flexGrow: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 1, mb: 1.5, flexWrap: 'wrap' }}>
          <Chip
            label={`Session #${post.sessionId} (${post.socialNetwork ?? 'KAJGANA'})`}
            size='small'
            color='primary'
            variant='contained'
            sx={{ fontWeight: 'bold' }}
          />
          <Chip
            label={`${Math.round((post.macedonianConfidence ?? 0) * 100)}% MK`}
            size='small'
            color='success'
            variant='outlined'
          />
        </Box>

        <Typography variant='subtitle2' sx={{ fontWeight: 'bold', mb: 0.5 }}>
          Author: {post.authorHandle ?? 'Кајгана'}
        </Typography>

        <Typography variant='body2' sx={{ whiteSpace: 'pre-wrap', mb: 1 }}>
          {preview || 'No text content.'}
        </Typography>

        <Typography variant='caption' color='text.secondary' sx={{ display: 'block' }}>
          {post.postedAt ? new Date(post.postedAt).toLocaleString() : 'Date unavailable'}
        </Typography>

        {post.sourceUrl && (
          <Link
            href={post.sourceUrl}
            target='_blank'
            rel='noreferrer'
            variant='caption'
            sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.25, mt: 0.5 }}
          >
            <Launch fontSize='inherit' /> Source Link
          </Link>
        )}
      </CardContent>

      <CardActions sx={{ justifyContent: 'space-between', p: 1.5, pt: 0 }}>
        <Button size='small' onClick={() => navigate(`/posts/${post.id}`)}>
          Details
        </Button>
        {post.donationBatchId && (
          <Chip label={`Batch #${post.donationBatchId}`} size='small' color='secondary' />
        )}
      </CardActions>
    </Card>
  );
};

export default PostCard;
