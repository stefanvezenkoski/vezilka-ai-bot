import { Box, Typography } from '@mui/material';
import type { BotActionLogResponse } from '../../../../api/types/session.ts';

interface SessionLogViewerProps {
  logs: BotActionLogResponse[];
}

/**
 * TODO(student): Render the agentic-loop trace: one row per action with its
 * type, details, success indicator and timestamp — the live view of what
 * your bot is doing during a session.
 */
const SessionLogViewer = ({ logs }: SessionLogViewerProps) => {
  return (
    <Box>
      <Typography color='text.secondary'>
        TODO(student): Render the {logs.length} bot action log(s) here.
      </Typography>
    </Box>
  );
};

export default SessionLogViewer;
