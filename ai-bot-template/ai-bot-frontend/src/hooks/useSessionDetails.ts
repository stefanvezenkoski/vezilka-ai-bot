import { useCallback, useEffect, useState } from 'react';
import sessionApi from '../api/sessionApi.ts';
import type { BotActionLogResponse, SessionResponse } from '../api/types/session.ts';

const useSessionDetails = (id: string) => {
  const [session, setSession] = useState<SessionResponse | null>(null);
  const [logs, setLogs] = useState<BotActionLogResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  const fetchSessionData = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [sessionRes, logsRes] = await Promise.all([
        sessionApi.findById(id),
        sessionApi.findLogs(id)
      ]);
      setSession(sessionRes.data);
      setLogs(logsRes.data);
    } catch (error) {
      console.error(`Failed to fetch details for session ${id}`, error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchSessionData();
  }, [fetchSessionData]);

  // Live polling of logs while session is running
  useEffect(() => {
    if (!session || session.status !== 'RUNNING') return;

    const interval = setInterval(async () => {
      try {
        const [sessionRes, logsRes] = await Promise.all([
          sessionApi.findById(id),
          sessionApi.findLogs(id)
        ]);
        setSession(sessionRes.data);
        setLogs(logsRes.data);
      } catch (error) {
        console.error('Error polling session logs', error);
      }
    }, 2000);

    return () => clearInterval(interval);
  }, [id, session]);

  return { session, logs, loading, refetch: fetchSessionData };
};

export default useSessionDetails;
