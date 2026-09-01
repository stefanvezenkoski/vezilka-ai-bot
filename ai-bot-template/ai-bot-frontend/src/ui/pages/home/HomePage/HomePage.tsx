import { Box, Card, CardContent, CircularProgress, Container, Grid, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import donationApi from '../../../../api/donationApi.ts';
import postApi from '../../../../api/postApi.ts';
import sessionApi from '../../../../api/sessionApi.ts';
import type { SessionResponse } from '../../../../api/types/session.ts';

/**
 * TODO(student): Turn this into a small dashboard: total extracted posts,
 * posts above your Macedonian-confidence threshold, donated pages, and the
 * status of the latest extraction session.
 */
const HomePage = () => {
  const [loading, setLoading] = useState(true);
  const [postCount, setPostCount] = useState(0);
  const [donationCount, setDonationCount] = useState(0);
  const [latestSession, setLatestSession] = useState<SessionResponse | null>(null);

  useEffect(() => {
    Promise.all([postApi.findAll({}, 0, 1), donationApi.findAll(), sessionApi.findAll()])
      .then(([posts, donations, sessions]) => {
        setPostCount(posts.data.totalElements);
        setDonationCount(donations.data.length);
        setLatestSession(sessions.data[0] ?? null);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <Box sx={{ m: 0, p: 0 }}>
      <Container maxWidth='xl' sx={{ mt: 3, py: 3 }}>
        <Typography variant='h4' gutterBottom>
          AI Bot for doniraj.vezilka.ai 🤖
        </Typography>
        {loading ? <CircularProgress/> : <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 4 }}><Card><CardContent><Typography color='text.secondary'>Extracted posts</Typography><Typography variant='h3'>{postCount}</Typography></CardContent></Card></Grid>
          <Grid size={{ xs: 12, sm: 4 }}><Card><CardContent><Typography color='text.secondary'>Donation batches</Typography><Typography variant='h3'>{donationCount}</Typography></CardContent></Card></Grid>
          <Grid size={{ xs: 12, sm: 4 }}><Card><CardContent><Typography color='text.secondary'>Latest session</Typography><Typography variant='h6'>{latestSession ? latestSession.status : 'No sessions yet'}</Typography><Typography variant='body2'>{latestSession?.description || 'Create a Kajgana session to begin.'}</Typography></CardContent></Card></Grid>
        </Grid>}
      </Container>
    </Box>
  );
};

export default HomePage;
