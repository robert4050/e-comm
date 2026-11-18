export interface AdminUser {
  id?: string;
  username: string;
  email?: string;
  roles: string[];
  avatarUrl?: string;
  authProvider?: 'google' | 'password' | 'clerk';
}

export interface SystemUser {
  id: string;
  email: string;
  fullName: string;
  role: 'SUPER_ADMIN' | 'ADMIN' | 'USER' | string;
  status: 'ACTIVE' | 'INACTIVE' | string;
  avatarUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateUserPayload {
  email: string;
  password: string;
  fullName: string;
  role: string;
  status: string;
  avatarUrl?: string;
}

export interface UpdateUserPayload {
  email?: string;
  password?: string;
  fullName?: string;
  role?: string;
  status?: string;
  avatarUrl?: string;
}
