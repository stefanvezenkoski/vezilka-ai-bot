import { Box, Container, Typography } from '@mui/material';

/**
 * TODO(student): Turn this into a small dashboard: total extracted posts,
 * posts above your Macedonian-confidence threshold, donated pages, and the
 * status of the latest extraction session.
 */
const HomePage = () => {
  return (
    <Box sx={{ m: 0, p: 0 }}>
      <Container maxWidth='xl' sx={{ mt: 3, py: 3 }}>
        <Typography variant='h4' gutterBottom>
          AI Bot for doniraj.vezilka.ai 🤖
        </Typography>
        <Typography variant='body1' sx={{ mb: 4 }}>
          This bot navigates a social network, extracts Macedonian content and
          donates it to the Vezilka language-preservation platform. Use the
          Sessions page to run the bot, the Posts page to browse what it
          collected, and the Donations page to review and submit batches.
        </Typography>
      </Container>
    </Box>
  );
};

export default HomePage;
