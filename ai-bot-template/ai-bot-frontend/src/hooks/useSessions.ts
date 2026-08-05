import { useContext } from 'react';
import SessionsContext, { type SessionsContextType } from '../contexts/sessionsContext.ts';

const useSessions = () => useContext<SessionsContextType>(SessionsContext);

export default useSessions;
