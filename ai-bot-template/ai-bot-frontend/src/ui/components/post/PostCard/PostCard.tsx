import { Card, CardContent, Typography } from '@mui/material';
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
  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        <Typography variant='subtitle2'>{post.authorHandle ?? 'unknown author'}</Typography>
        <Typography variant='body2' color='text.secondary'>
          TODO(student): Render this post.
        </Typography>
      </CardContent>
    </Card>
  );
};

export default PostCard;
