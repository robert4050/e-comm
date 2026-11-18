export interface UserProfile {
  id?: string;
  username: string;
  email: string;
  roles: string[];
  avatarUrl?: string;
  authProvider?: 'google' | 'password' | 'clerk';
}
