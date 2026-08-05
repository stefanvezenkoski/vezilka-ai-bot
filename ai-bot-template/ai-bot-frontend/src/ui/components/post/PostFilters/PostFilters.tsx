import { Box, Typography } from '@mui/material';
import type { PostFilter } from '../../../../api/types/post.ts';

interface PostFiltersProps {
  filter: PostFilter;
  onChange: (filter: PostFilter) => void;
}

/**
 * TODO(student): Implement the filter bar for the content browser: session,
 * minimum Macedonian confidence (slider), donated yes/no, and a free-text
 * search over the content. Call onChange with the updated filter.
 */
const PostFilters = ({ filter, onChange }: PostFiltersProps) => {
  void filter;
  void onChange;

  return (
    <Box sx={{ mb: 2 }}>
      <Typography color='text.secondary'>
        TODO(student): Implement the post filters.
      </Typography>
    </Box>
  );
};

export default PostFilters;
