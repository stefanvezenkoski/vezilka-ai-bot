import { Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, List, ListItem, ListItemText, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import postApi from '../../../../api/postApi.ts';
import type { PostResponse } from '../../../../api/types/post.ts';
import useDonations from '../../../../hooks/useDonations.ts';

interface SubmitDonationDialogProps {
  open: boolean;
  onClose: () => void;
}

/**
 * TODO(student): Implement the "create donation batch" flow: let the user
 * pick not-yet-donated posts (postApi.findAll with donated=false), review
 * their content, and create the batch via useDonations().onCreate.
 */
const SubmitDonationDialog = ({ open, onClose }: SubmitDonationDialogProps) => {
  const { onCreate } = useDonations();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  useEffect(() => {
    if (!open) return;
    postApi.findAll({ donated: false }, 0, 100).then((response) => setPosts(response.data.content));
  }, [open]);

  const toggle = (id: number) => setSelectedIds((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id]);
  const create = async () => {
    await onCreate({ postIds: selectedIds });
    handleClose();
  };
  const handleClose = () => {
    setSelectedIds([]);
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth='md'>
      <DialogTitle>New Donation Batch</DialogTitle>
      <DialogContent>
        {posts.length === 0 && <Typography color='text.secondary'>There are no undonated posts available.</Typography>}
        <List dense>
          {posts.map((post) => <ListItem key={post.id} disablePadding>
            <FormControlLabel control={<Checkbox checked={selectedIds.includes(post.id)} onChange={() => toggle(post.id)}/>} label={<ListItemText primary={post.authorHandle || 'Unknown author'} secondary={(post.content || '').slice(0, 140)}/>}/>
          </ListItem>)}
        </List>
      </DialogContent>
      <DialogActions><Button onClick={handleClose}>Cancel</Button><Button variant='contained' disabled={selectedIds.length === 0} onClick={create}>Create batch ({selectedIds.length})</Button></DialogActions>
    </Dialog>
  );
};

export default SubmitDonationDialog;
