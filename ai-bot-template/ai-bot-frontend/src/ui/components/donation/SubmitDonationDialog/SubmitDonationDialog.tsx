import { Dialog, DialogContent, DialogTitle, Typography } from '@mui/material';

interface SubmitDonationDialogProps {
  open: boolean;
  onClose: () => void;
}

/**
 * TODO(student): Implement the "create donation batch" flow: let the user
 * pick not-yet-donated posts (postApi.findAll with donated=false), review
 * their content, and create the batch via useDonations().onCreate.
 */
const SubmitDonationDialog = ({ open, onClose }: SubmitDonationDialogProps) => {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth='md'>
      <DialogTitle>New Donation Batch</DialogTitle>
      <DialogContent>
        <Typography color='text.secondary'>
          TODO(student): Implement this dialog.
        </Typography>
      </DialogContent>
    </Dialog>
  );
};

export default SubmitDonationDialog;
