export interface AppNotification {
  id: string;
  title: string;
  body?: string;
  read?: boolean;
  type?: string;
  createdAt?: string;
  href?: string;
}
