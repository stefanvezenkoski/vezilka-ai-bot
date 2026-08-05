import { Card, CardContent, Chip, Typography } from '@mui/material';
import type { DonationBatchResponse } from '../../../../api/types/donation.ts';

interface DonationBatchCardProps {
  batch: DonationBatchResponse;
}

/**
 * TODO(student): Show the batch: number of posts, status, the Vezilka
 * reference once submitted, and approve/submit actions
 * (useDonations().onApprove / onSubmit) enabled according to the status.
 */
const DonationBatchCard = ({ batch }: DonationBatchCardProps) => {
  return (
    <Card>
      <CardContent>
        <Typography variant='h6'>Batch #{batch.id}</Typography>
        <Chip label={batch.status} size='small'/>
        <Typography variant='body2' color='text.secondary' sx={{ mt: 1 }}>
          TODO(student): Render this donation batch.
        </Typography>
      </CardContent>
    </Card>
  );
};

export default DonationBatchCard;
