export type WebhookEventType = 'leave.approved' | 'timeentry.submitted' | 'employee.created';

export type WebhookDeliveryStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'DEAD';

export interface WebhookEndpoint {
  id: string;
  url: string;
  description?: string;
  eventTypes: WebhookEventType[];
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface WebhookEndpointCreated extends WebhookEndpoint {
  secret: string;
}

export interface WebhookEndpointRequest {
  url: string;
  description?: string;
  eventTypes: WebhookEventType[];
  active: boolean;
}

export interface WebhookDelivery {
  id: string;
  endpointId: string;
  endpointUrl?: string;
  eventType: string;
  status: WebhookDeliveryStatus;
  attemptCount: number;
  maxAttempts: number;
  nextRetryAt?: string;
  lastAttemptAt?: string;
  responseStatus?: number;
  responseBody?: string;
  createdAt: string;
  deliveredAt?: string;
}

export interface WebhookDeliveryPage {
  content: WebhookDelivery[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const WEBHOOK_EVENT_OPTIONS: { value: WebhookEventType; labelKey: string }[] = [
  { value: 'leave.approved', labelKey: 'settings.webhooks.eventTypes.leaveApproved' },
  { value: 'timeentry.submitted', labelKey: 'settings.webhooks.eventTypes.timeentrySubmitted' },
  { value: 'employee.created', labelKey: 'settings.webhooks.eventTypes.employeeCreated' },
];
