import { useCallback, useEffect, useState } from 'react';
import donationApi from '../api/donationApi.ts';
import type { CreateDonationBatchRequest, DonationBatchResponse } from '../api/types/donation.ts';

const useDonations = () => {
  const [donations, setDonations] = useState<DonationBatchResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  const fetchDonations = useCallback(async () => {
    setLoading(true);
    try {
      const response = await donationApi.findAll();
      setDonations(response.data);
    } catch (error) {
      console.error('Failed to fetch donation batches', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDonations();
  }, [fetchDonations]);

  const onCreate = async (data: CreateDonationBatchRequest) => {
    try {
      await donationApi.add(data);
      await fetchDonations();
    } catch (error) {
      console.error('Failed to create donation batch', error);
    }
  };

  const onApprove = async (id: number) => {
    try {
      await donationApi.approve(id.toString());
      await fetchDonations();
    } catch (error) {
      console.error(`Failed to approve donation batch ${id}`, error);
    }
  };

  const onSubmit = async (id: number) => {
    try {
      await donationApi.submit(id.toString());
      await fetchDonations();
    } catch (error) {
      console.error(`Failed to submit donation batch ${id}`, error);
    }
  };

  return { donations, loading, onCreate, onApprove, onSubmit, refetch: fetchDonations };
};

export default useDonations;
