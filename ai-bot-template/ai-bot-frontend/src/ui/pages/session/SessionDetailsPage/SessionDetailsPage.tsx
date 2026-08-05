import { Box, Typography } from '@mui/material';
import { useParams } from 'react-router';
import useSessionDetails from '../../../../hooks/useSessionDetails.ts';
import SessionLogViewer from '../../../components/session/SessionLogViewer/SessionLogViewer.tsx';

/**
 * TODO(student): Show one session in detail: its targets, timestamps and the
 * live agentic-loop trace (SessionLogViewer + useSessionDetails).
 */
const SessionDetailsPage = () => {
  const { id } = useParams<{ id: string }>();
  const { session, logs } = useSessionDetails(id!);

  return (
    <Box>
      <Typography variant='h5' gutterBottom>
        Session {session ? `#${id} — ${session.socialNetwork}` : `#${id}`}
      </Typography>
      <Typography color='text.secondary' sx={{ mb: 2 }}>
        TODO(student): Implement this page.
      </Typography>
      <SessionLogViewer logs={logs}/>
    </Box>
  );
};

export default SessionDetailsPage;
