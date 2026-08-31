export type UserRole = 'LISTENER' | 'ARTIST' | 'ADMIN' | 'MODERATOR' | string;

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  email: string;
}

export interface Account {
  id: string;
  email: string;
  enabled: boolean;
  emailVerified: boolean;
  roles: UserRole[];
  createdAt?: string;
  displayName?: string;
  avatarUrl?: string;
}

export interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  bio?: string;
  avatarUrl?: string;
  country?: string;
  roles?: UserRole[];
  followers?: number;
  following?: number;
  createdAt?: string;
}

export interface UpdateProfileRequest {
  displayName?: string;
  bio?: string;
  avatarUrl?: string;
  country?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: 'LISTENER' | 'ARTIST';
  displayName?: string;
}
