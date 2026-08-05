import { jwtDecode } from 'jwt-decode';
import { useCallback, useMemo, useState } from 'react';
import AuthContext from '../contexts/authContext.ts';
import * as React from 'react';
import type { UserPayload } from '../api/types/user.ts';

const tryDecode = (jwtToken: string): UserPayload | null => {
  try {
    return jwtDecode<UserPayload>(jwtToken);
  } catch {
    return null;
  }
};

const initializeUser = (): UserPayload | null => {
  const jwtToken = localStorage.getItem('token');
  return jwtToken ? tryDecode(jwtToken) : null;
};

const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<UserPayload | null>(initializeUser);

  const login = useCallback((jwtToken: string) => {
    const payload = tryDecode(jwtToken);
    if (!payload) {
      throw new Error('Invalid session token.');
    }
    localStorage.setItem('token', jwtToken);
    setUser(payload);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setUser(null);
  }, []);

  const value = useMemo(() => ({
    user,
    isLoggedIn: !!user,
    login,
    logout
  }), [user, login, logout]);

  return (
    <AuthContext value={value}>
      {children}
    </AuthContext>
  );
};

export default AuthProvider;
