import { Card, CardActions, CardContent, Chip, Typography, Button } from '@mui/material';
import type { DonationBatchResponse } from '../../../../api/types/donation.ts';

interface DonationBatchCardProps {
  batch: DonationBatchResponse;
  onApprove: (id: number) => Promise<void>;
  onSubmit: (id: number) => Promise<void>;
}

/**
 * TODO(student): Show the batch: number of posts, status, the Vezilka
 * reference once submitted, and approve/submit actions
 * (useDonations().onApprove / onSubmit) enabled according to the status.
 */
const DonationBatchCard = ({ batch, onApprove, onSubmit }: DonationBatchCardProps) => {
  return (
    <Card>
      <CardContent>
        <Typography variant='h6'>Batch #{batch.id}</Typography>
        <Chip label={batch.status} size='small'/>
        <Typography variant='body2' color='text.secondary' sx={{ mt: 1 }}>{batch.postIds.length} post(s) in this batch</Typography>
        <Typography variant='caption' color='text.secondary' sx={{ display: 'block' }}>Created: {new Date(batch.createdAt).toLocaleString()}</Typography>
        {batch.vezilkaReference && <Typography variant='caption' sx={{ display: 'block' }}>Vezilka reference: {batch.vezilkaReference}</Typography>}
      </CardContent>
      <CardActions>
        <Button size='small' disabled={batch.status !== 'DRAFT'} onClick={() => onApprove(batch.id)}>Approve</Button>
        <Button size='small' variant='contained' disabled={batch.status !== 'APPROVED'} onClick={() => onSubmit(batch.id)}>Submit</Button>
      </CardActions>
    </Card>
  );
};

export default DonationBatchCard;
