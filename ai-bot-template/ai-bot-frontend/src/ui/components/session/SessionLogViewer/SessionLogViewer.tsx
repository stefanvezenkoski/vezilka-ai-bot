import { CheckCircleOutlined, ErrorOutlined } from '@mui/icons-material';
import { Box, Chip, List, ListItem, ListItemIcon, ListItemText, Paper, Typography } from '@mui/material';
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
  if (logs.length === 0) {
    return <Typography color='text.secondary'>No bot actions have been recorded yet.</Typography>;
  }

  return (
    <Paper variant='outlined'>
      <List dense disablePadding>
        {logs.map((log, index) => (
          <ListItem key={log.id} divider={index < logs.length - 1} alignItems='flex-start'>
            <ListItemIcon sx={{ minWidth: 34, mt: 0.25 }}>
              {log.successful ? <CheckCircleOutlined color='success' fontSize='small'/> : <ErrorOutlined color='error' fontSize='small'/>}
            </ListItemIcon>
            <ListItemText
              primary={<Chip label={log.actionType} size='small' color={log.successful ? 'primary' : 'error'}/>}
              secondary={
                <Box component='span' sx={{ display: 'block', mt: 0.5 }}>
                  <Typography component='span' variant='body2' sx={{ display: 'block' }}>{log.details || 'No additional details.'}</Typography>
                  <Typography component='span' variant='caption' color='text.secondary' sx={{ display: 'block' }}>
                    {new Date(log.occurredAt).toLocaleString()}
                  </Typography>
                </Box>
              }
            />
          </ListItem>
        ))}
      </List>
    </Paper>
  );
};

export default SessionLogViewer;
