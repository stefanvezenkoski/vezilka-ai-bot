import { Box, Typography } from '@mui/material';
import { useParams } from 'react-router';

/**
 * TODO(student): Show one extracted post in full: the complete content, its
 * media items (images/videos), the source link, the language confidence and
 * its donation status (postApi.findById).
 */
const PostDetailsPage = () => {
  const { id } = useParams<{ id: string }>();

  return (
    <Box>
      <Typography variant='h5' gutterBottom>Post #{id}</Typography>
      <Typography color='text.secondary'>
        TODO(student): Implement this page.
      </Typography>
    </Box>
  );
};

export default PostDetailsPage;
