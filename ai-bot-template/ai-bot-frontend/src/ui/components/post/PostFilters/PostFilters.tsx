import { Box, Button, FormControl, InputLabel, MenuItem, Select, Slider, TextField } from '@mui/material';
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
  const update = (changes: Partial<PostFilter>) => onChange({ ...filter, ...changes });

  return (
    <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
      <TextField size='small' label='Search text' value={filter.search ?? ''} onChange={(event) => update({ search: event.target.value || undefined })}/>
      <FormControl size='small' sx={{ minWidth: 150 }}>
        <InputLabel>Donation</InputLabel>
        <Select label='Donation' value={filter.donated === undefined ? '' : String(filter.donated)} onChange={(event) => update({ donated: event.target.value === '' ? undefined : event.target.value === 'true' })}>
          <MenuItem value=''>All posts</MenuItem><MenuItem value='false'>Not donated</MenuItem><MenuItem value='true'>Donated</MenuItem>
        </Select>
      </FormControl>
      <Box sx={{ width: 190 }}>
        <Slider value={filter.minMacedonianConfidence ?? 0} min={0} max={1} step={0.05} valueLabelDisplay='auto' valueLabelFormat={(value) => `${Math.round(value * 100)}%`} onChange={(_, value) => update({ minMacedonianConfidence: (value as number) || undefined })}/>
      </Box>
      <Button onClick={() => onChange({})}>Clear filters</Button>
    </Box>
  );
};

export default PostFilters;
