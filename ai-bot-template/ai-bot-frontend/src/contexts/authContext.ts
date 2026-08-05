import { createContext } from 'react';
import type { UserPayload } from '../api/types/user.ts';

export interface AuthContextType {
  user: UserPayload | null;
  login: (jwtToken: string) => void;
  logout: () => void;
  isLoggedIn: boolean;
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export default AuthContext;
