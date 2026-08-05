import { useContext } from 'react';
import AuthContext, { type AuthContextType } from '../contexts/authContext.ts';

const useAuth = () => useContext<AuthContextType>(AuthContext);

export default useAuth;
