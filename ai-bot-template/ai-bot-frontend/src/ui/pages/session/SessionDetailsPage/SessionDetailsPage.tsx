import { ArrowBack } from '@mui/icons-material';
import { Box, Button, Chip, CircularProgress, Divider, Stack, Typography } from '@mui/material';
import { useNavigate, useParams } from 'react-router';
import useSessionDetails from '../../../../hooks/useSessionDetails.ts';
import SessionLogViewer from '../../../components/session/SessionLogViewer/SessionLogViewer.tsx';

/**
 * TODO(student): Show one session in detail: its targets, timestamps and the
 * live agentic-loop trace (SessionLogViewer + useSessionDetails).
 */
const SessionDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { session, logs, loading } = useSessionDetails(id!);

  if (loading && !session) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress/></Box>;
  }

  if (!session) {
    return (
      <Box>
        <Typography variant='h5'>Session not found</Typography>
        <Button startIcon={<ArrowBack/>} onClick={() => navigate('/sessions')} sx={{ mt: 2 }}>Back to sessions</Button>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant='h5' gutterBottom>
        Session #{id} — {session.socialNetwork}
      </Typography>
      <Button startIcon={<ArrowBack/>} onClick={() => navigate('/sessions')} sx={{ mb: 2 }}>Back to sessions</Button>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 2, alignItems: { sm: 'center' } }}>
        <Chip label={session.status} color={session.status === 'COMPLETED' ? 'success' : session.status === 'FAILED' ? 'error' : 'primary'}/>
        <Typography color='text.secondary'>{session.description || 'No description provided.'}</Typography>
      </Stack>
      <Typography variant='body2' color='text.secondary'>Started: {session.startedAt ? new Date(session.startedAt).toLocaleString() : '—'}</Typography>
      <Typography variant='body2' color='text.secondary' sx={{ mb: 2 }}>Finished: {session.finishedAt ? new Date(session.finishedAt).toLocaleString() : '—'}</Typography>
      <Typography variant='subtitle1' gutterBottom>Targets</Typography>
      <Stack direction='row' spacing={1} useFlexGap sx={{ mb: 3, flexWrap: 'wrap' }}>
        {session.targets.map((target) => <Chip key={target.id} label={`${target.type}: ${target.value}`} variant='outlined'/>) }
      </Stack>
      <Divider sx={{ mb: 2 }}/>
      <Typography variant='h6' gutterBottom>Bot activity</Typography>
      <SessionLogViewer logs={logs}/>
    </Box>
  );
};

export default SessionDetailsPage;
