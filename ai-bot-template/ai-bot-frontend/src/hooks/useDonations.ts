import { useState } from 'react';
import type { CreateDonationBatchRequest, DonationBatchResponse } from '../api/types/donation.ts';

/**
 * TODO(student): Drive the donation workflow for the DonationsPage: list the
 * batches (donationApi.findAll) and expose create/approve/submit actions,
 * re-fetching after every mutation, with loading/error state.
 */
const useDonations = () => {
  const [donations] = useState<DonationBatchResponse[]>([]);
  const [loading] = useState<boolean>(false);

  const onCreate = async (data: CreateDonationBatchRequest) => {
    void data;
  };
  const onApprove = async (id: number) => {
    void id;
  };
  const onSubmit = async (id: number) => {
    void id;
  };

  return { donations, loading, onCreate, onApprove, onSubmit };
};

export default useDonations;
