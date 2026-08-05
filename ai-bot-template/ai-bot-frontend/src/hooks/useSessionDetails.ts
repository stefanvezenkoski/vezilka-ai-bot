import { useState } from 'react';
import type { BotActionLogResponse, SessionResponse } from '../api/types/session.ts';

/**
 * TODO(student): Load one session (sessionApi.findById) together with its
 * action logs (sessionApi.findLogs) for the SessionDetailsPage, including
 * loading/error state. Consider polling the logs while the session is RUNNING
 * so the trace updates live.
 */
const useSessionDetails = (id: string) => {
  void id;

  const [session] = useState<SessionResponse | null>(null);
  const [logs] = useState<BotActionLogResponse[]>([]);
  const [loading] = useState<boolean>(false);

  return { session, logs, loading };
};

export default useSessionDetails;
