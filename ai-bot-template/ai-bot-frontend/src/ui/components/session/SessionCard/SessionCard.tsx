import { Box, Button, Card, CardActions, CardContent, Chip, Typography } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import StopIcon from '@mui/icons-material/Stop';
import InfoIcon from '@mui/icons-material/Info';
import { useNavigate } from 'react-router';
import type { SessionResponse } from '../../../../api/types/session.ts';
import useSessions from '../../../../hooks/useSessions.ts';

interface SessionCardProps {
  session: SessionResponse;
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'RUNNING':
      return 'primary';
    case 'COMPLETED':
      return 'success';
    case 'FAILED':
      return 'error';
    case 'PAUSED':
      return 'warning';
    default:
      return 'default';
  }
};

const SessionCard = ({ session }: SessionCardProps) => {
  const navigate = useNavigate();
  const { onStart, onStop } = useSessions();

  const isRunning = session.status === 'RUNNING';
  const canStart = session.status === 'CREATED' || session.status === 'PAUSED';

  return (
    <Card sx={{ maxWidth: 360, height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 2, boxShadow: 2 }}>
      <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant='h6' sx={{ fontWeight: 'bold' }}>
            {session.socialNetwork}
          </Typography>
          <Chip label={session.status} color={getStatusColor(session.status)} size='small' />
        </Box>

        <Typography variant='body2' color='text.secondary' sx={{ flexGrow: 1 }}>
          {session.description || 'No description provided.'}
        </Typography>

        {session.targets && session.targets.length > 0 && (
          <Box sx={{ mt: 1, display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {session.targets.map((target) => (
              <Chip
                key={target.id}
                label={`${target.type}: ${target.value}`}
                size='small'
                variant='outlined'
              />
            ))}
          </Box>
        )}
      </CardContent>

      <CardActions sx={{ justifyContent: 'space-between', p: 2, pt: 0 }}>
        <Button
          size='small'
          startIcon={<InfoIcon />}
          onClick={() => navigate(`/sessions/${session.id}`)}
        >
          Info
        </Button>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            size='small'
            startIcon={<PlayArrowIcon />}
            color='success'
            variant='contained'
            disabled={!canStart}
            onClick={() => onStart(session.id)}
          >
            Start
          </Button>
          <Button
            size='small'
            startIcon={<StopIcon />}
            color='error'
            variant='outlined'
            disabled={!isRunning}
            onClick={() => onStop(session.id)}
          >
            Stop
          </Button>
        </Box>
      </CardActions>
    </Card>
  );
};

export default SessionCard;
