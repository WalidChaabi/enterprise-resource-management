export type PrivacyErasureStatus = 'PENDING' | 'REJECTED' | 'COMPLETED';

export interface ErasureRequest {
  id: string;
  userId: string;
  userEmail: string;
  userFullName: string;
  status: PrivacyErasureStatus;
  requestedAt: string;
  processedAt?: string;
  adminNotes?: string;
}
