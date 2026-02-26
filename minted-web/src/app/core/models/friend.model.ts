export interface FriendRequest {
  name: string;
  email?: string;
  phone?: string;
  avatarColor?: string;
}

export interface FriendResponse {
  id: number;
  name: string;
  email: string | null;
  phone: string | null;
  avatarColor: string;
  avatarBase64: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
