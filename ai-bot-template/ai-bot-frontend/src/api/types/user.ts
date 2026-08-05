import type { JwtPayload } from 'jwt-decode';

export type Role = 'ROLE_ADMINISTRATOR' | 'ROLE_USER';

export interface RegisterRequest {
  name: string;
  surname: string;
  email: string;
  username: string;
  password: string;
}

export interface RegisterResponse {
  username: string;
  name: string;
  surname: string;
  email: string;
  role: Role;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface UserPayload extends JwtPayload {
  username: string;
  roles: string[];
}
