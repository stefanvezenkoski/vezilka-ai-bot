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
import FlashOnIcon from '@mui/icons-material/FlashOn';
import { useState } from 'react';
import useSessions from '../../../../hooks/useSessions.ts';
import type { CreateTargetRequest, SocialNetwork, TargetType } from '../../../../api/types/session.ts';

interface StartSessionDialogProps {
  open: boolean;
  onClose: () => void;
}

const KAJGANA_MAIN_URLS = [
  { label: 'Вести Македонија (kajgana.com/vesti/makedonija)', value: 'https://kajgana.com/vesti/makedonija' },
  { label: 'Вести Свет (kajgana.com/vesti/svet)', value: 'https://kajgana.com/vesti/svet' },
  { label: 'Спорт (kajgana.com/sport)', value: 'https://kajgana.com/sport' },
  { label: 'Магазин (kajgana.com/magazin)', value: 'https://kajgana.com/magazin' },
  { label: 'Сцена & Култура (kajgana.com/scena)', value: 'https://kajgana.com/scena' },
  { label: 'Наука & Технологија (kajgana.com/nauka-i-tehnologija)', value: 'https://kajgana.com/nauka-i-tehnologija' },
  { label: 'Кајгана Форум (forum.kajgana.com)', value: 'https://forum.kajgana.com' },
  { label: 'Почетна Страна (kajgana.com)', value: 'https://kajgana.com' },
  { label: 'Custom URL...', value: 'CUSTOM' }
];

const StartSessionDialog = ({ open, onClose }: StartSessionDialogProps) => {
  const { onCreate } = useSessions();
  const [socialNetwork, setSocialNetwork] = useState<SocialNetwork>('KAJGANA');
  const [description, setDescription] = useState<string>('');
  const [targets, setTargets] = useState<CreateTargetRequest[]>([
    { type: 'FEED_URL', value: 'https://kajgana.com/vesti/makedonija' }
  ]);
  const [customUrls, setCustomUrls] = useState<{ [key: number]: string }>({});

  const handleAddTarget = () => {
    setTargets([...targets, { type: 'FEED_URL', value: 'https://kajgana.com/vesti/makedonija' }]);
  };

  const handleRemoveTarget = (index: number) => {
    setTargets(targets.filter((_, i) => i !== index));
    const updatedCustom = { ...customUrls };
    delete updatedCustom[index];
    setCustomUrls(updatedCustom);
  };

  const handleTargetChange = (index: number, field: keyof CreateTargetRequest, val: string) => {
    const updated = [...targets];
    updated[index] = { ...updated[index], [field]: val };
    setTargets(updated);
  };

  const handleLoadAllCategories = () => {
    setDescription('Full Monthly Scrape of All Kajgana Categories (August 2026)');
    setTargets([
      { type: 'FEED_URL', value: 'https://kajgana.com/vesti/makedonija' },
      { type: 'FEED_URL', value: 'https://kajgana.com/vesti/svet' },
      { type: 'FEED_URL', value: 'https://kajgana.com/sport' },
      { type: 'FEED_URL', value: 'https://kajgana.com/magazin' },
      { type: 'FEED_URL', value: 'https://kajgana.com/scena' },
      { type: 'FEED_URL', value: 'https://kajgana.com/nauka-i-tehnologija' },
      { type: 'FEED_URL', value: 'https://forum.kajgana.com' }
    ]);
  };

  const handleSubmit = async () => {
    try {
      const finalTargets = targets.map((t, idx) => {
        if (t.type === 'FEED_URL' && t.value === 'CUSTOM') {
          return { ...t, value: customUrls[idx] || 'https://kajgana.com' };
        }
        return t;
      }).filter(t => t.value.trim() !== '');

      await onCreate({
        socialNetwork,
        description,
        targets: finalTargets
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
          </Select>
        </FormControl>

        <TextField
          label='Session Description'
          fullWidth
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder='e.g., Extract news from Kajgana Macedonia section'
        />

        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant='subtitle1' sx={{ fontWeight: 'bold' }}>
            Extraction Targets
          </Typography>
          <Button
            size='small'
            color='secondary'
            variant='contained'
            startIcon={<FlashOnIcon />}
            onClick={handleLoadAllCategories}
          >
            Load All Categories
          </Button>
        </Box>

        {targets.map((target, index) => (
          <Box key={index} sx={{ display: 'flex', gap: 1, mb: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
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

            {target.type === 'FEED_URL' ? (
              <FormControl sx={{ flexGrow: 1, minWidth: 240 }} size='small'>
                <InputLabel>Select Target URL</InputLabel>
                <Select
                  value={KAJGANA_MAIN_URLS.some(u => u.value === target.value) ? target.value : 'CUSTOM'}
                  label='Select Target URL'
                  onChange={(e) => {
                    const selectedVal = e.target.value;
                    handleTargetChange(index, 'value', selectedVal);
                  }}
                >
                  {KAJGANA_MAIN_URLS.map((urlItem) => (
                    <MenuItem key={urlItem.value} value={urlItem.value}>
                      {urlItem.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            ) : (
              <TextField
                size='small'
                sx={{ flexGrow: 1 }}
                label='Value'
                value={target.value}
                onChange={(e) => handleTargetChange(index, 'value', e.target.value)}
                placeholder='e.g. спорт'
              />
            )}

            {target.type === 'FEED_URL' && (target.value === 'CUSTOM' || !KAJGANA_MAIN_URLS.some(u => u.value === target.value)) && (
              <TextField
                size='small'
                fullWidth
                label='Custom URL'
                value={customUrls[index] || ''}
                onChange={(e) => setCustomUrls({ ...customUrls, [index]: e.target.value })}
                placeholder='https://kajgana.com/custom-section'
                sx={{ mt: 1 }}
              />
            )}

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
