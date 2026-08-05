import { Dialog, DialogContent, DialogTitle, Typography } from '@mui/material';

interface StartSessionDialogProps {
  open: boolean;
  onClose: () => void;
}

/**
 * TODO(student): Implement the "new extraction session" form: a select for
 * the social network (your assigned one), a description field, and a dynamic
 * list of targets (type + value). Submit it via useSessions().onCreate and
 * close the dialog on success.
 */
const StartSessionDialog = ({ open, onClose }: StartSessionDialogProps) => {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth='sm'>
      <DialogTitle>New Extraction Session</DialogTitle>
      <DialogContent>
        <Typography color='text.secondary'>
          TODO(student): Implement this form.
        </Typography>
      </DialogContent>
    </Dialog>
  );
};

export default StartSessionDialog;
