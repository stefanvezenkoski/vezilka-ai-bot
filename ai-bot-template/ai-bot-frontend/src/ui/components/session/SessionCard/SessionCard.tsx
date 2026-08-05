import { Button, Card, CardActions, CardContent, Chip, Typography } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import StopIcon from '@mui/icons-material/Stop';
import InfoIcon from '@mui/icons-material/Info';
import { useNavigate } from 'react-router';
import type { SessionResponse } from '../../../../api/types/session.ts';
import useSessions from '../../../../hooks/useSessions.ts';

interface SessionCardProps {
  session: SessionResponse;
}

/**
 * TODO(student): Extend this card — show the targets, the timestamps and a
 * status-appropriate color, and disable start/stop according to the
 * session's lifecycle (see SessionStatus).
 */
const SessionCard = ({ session }: SessionCardProps) => {
  const navigate = useNavigate();
  const { onStart, onStop } = useSessions();

  return (
    <Card sx={{ maxWidth: 300, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        <Typography variant='h5'>{session.socialNetwork}</Typography>
        <Typography variant='subtitle1' sx={{ flexGrow: 1 }}>{session.description}</Typography>
        <Chip label={session.status} size='small' sx={{ alignSelf: 'flex-start' }}/>
      </CardContent>
      <CardActions sx={{ justifyContent: 'space-between' }}>
        <Button
          startIcon={<InfoIcon/>}
          onClick={() => navigate(`/sessions/${session.id}`)}
        >
          Info
        </Button>
        <Button
          startIcon={<PlayArrowIcon/>}
          color='success'
          onClick={() => onStart(session.id)}
        >
          Start
        </Button>
        <Button
          startIcon={<StopIcon/>}
          color='error'
          onClick={() => onStop(session.id)}
        >
          Stop
        </Button>
      </CardActions>
    </Card>
  );
};

export default SessionCard;
