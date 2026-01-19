export interface Notification {
  id: string;
  userId: string;
  title: string;
  description?: string;
  time: string;
  unread: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
