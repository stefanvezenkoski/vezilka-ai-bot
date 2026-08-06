import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  Typography
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useState } from 'react';
import useSessions from '../../../../hooks/useSessions.ts';
import type { CreateTargetRequest, SocialNetwork, TargetType } from '../../../../api/types/session.ts';

interface StartSessionDialogProps {
  open: boolean;
  onClose: () => void;
}

const StartSessionDialog = ({ open, onClose }: StartSessionDialogProps) => {
  const { onCreate } = useSessions();
  const [socialNetwork, setSocialNetwork] = useState<SocialNetwork>('KAJGANA');
  const [description, setDescription] = useState<string>('');
  const [targets, setTargets] = useState<CreateTargetRequest[]>([
    { type: 'FEED_URL', value: 'https://forum.kajgana.com' }
  ]);

  const handleAddTarget = () => {
    setTargets([...targets, { type: 'KEYWORD', value: '' }]);
  };

  const handleRemoveTarget = (index: number) => {
    setTargets(targets.filter((_, i) => i !== index));
  };

  const handleTargetChange = (index: number, field: keyof CreateTargetRequest, val: string) => {
    const updated = [...targets];
    updated[index] = { ...updated[index], [field]: val };
    setTargets(updated);
  };

  const handleSubmit = async () => {
    try {
      await onCreate({
        socialNetwork,
        description,
        targets: targets.filter(t => t.value.trim() !== '')
      });
      onClose();
    } catch (error) {
      console.error('Failed to create session', error);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth='sm'>
      <DialogTitle>New Extraction Session</DialogTitle>
      <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
        <FormControl fullWidth>
          <InputLabel id='social-network-label'>Target Site / Network</InputLabel>
          <Select
            labelId='social-network-label'
            value={socialNetwork}
            label='Target Site / Network'
            onChange={(e) => setSocialNetwork(e.target.value as SocialNetwork)}
          >
            <MenuItem value='KAJGANA'>Kajgana.mk / Forum Kajgana</MenuItem>
            <MenuItem value='REDDIT'>Reddit</MenuItem>
            <MenuItem value='X'>X (Twitter)</MenuItem>
            <MenuItem value='FACEBOOK'>Facebook</MenuItem>
            <MenuItem value='INSTAGRAM'>Instagram</MenuItem>
            <MenuItem value='YOUTUBE'>YouTube</MenuItem>
            <MenuItem value='THREADS'>Threads</MenuItem>
            <MenuItem value='LINKEDIN'>LinkedIn</MenuItem>
          </Select>
        </FormControl>

        <TextField
          label='Session Description'
          fullWidth
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder='e.g., Extract sports discussions from Kajgana forum'
        />

        <Box>
          <Typography variant='subtitle1' sx={{ mb: 1, fontWeight: 'bold' }}>
            Extraction Targets
          </Typography>
          {targets.map((target, index) => (
            <Box key={index} sx={{ display: 'flex', gap: 1, mb: 1.5, alignItems: 'center' }}>
              <FormControl sx={{ minWidth: 140 }}>
                <InputLabel size='small'>Target Type</InputLabel>
                <Select
                  size='small'
                  value={target.type}
                  label='Target Type'
                  onChange={(e) => handleTargetChange(index, 'type', e.target.value as TargetType)}
                >
                  <MenuItem value='FEED_URL'>Feed / Page URL</MenuItem>
                  <MenuItem value='KEYWORD'>Keyword</MenuItem>
                  <MenuItem value='HASHTAG'>Hashtag</MenuItem>
                  <MenuItem value='PROFILE'>Profile</MenuItem>
                </Select>
              </FormControl>
              <TextField
                size='small'
                fullWidth
                label='Value'
                value={target.value}
                onChange={(e) => handleTargetChange(index, 'value', e.target.value)}
                placeholder='e.g. https://forum.kajgana.com or спорт'
              />
              {targets.length > 1 && (
                <IconButton color='error' onClick={() => handleRemoveTarget(index)}>
                  <DeleteIcon />
                </IconButton>
              )}
            </Box>
          ))}
          <Button startIcon={<AddIcon />} variant='outlined' size='small' onClick={handleAddTarget}>
            Add Target
          </Button>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant='contained' onClick={handleSubmit} disabled={targets.length === 0}>
          Start Session
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default StartSessionDialog;
