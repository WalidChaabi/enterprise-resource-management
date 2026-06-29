export interface AuditEvent {
  id: string;
  organizationId: string;
  actorEmail: string;
  actorId?: string;
  action: string;
  entityType: string;
  entityId?: string;
  payloadJson?: string;
  createdAt: string;
}

export interface AuditEventPage {
  content: AuditEvent[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
