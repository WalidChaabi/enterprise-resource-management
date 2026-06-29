import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { WebhookService } from '../../../core/services/webhook.service';
import { EntitlementService } from '../../../core/services/entitlement.service';
import {
  WEBHOOK_EVENT_OPTIONS,
  WebhookDelivery,
  WebhookEndpoint,
  WebhookEventType,
} from '../../../core/models/webhook.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import {
  CardComponent,
  CardHeaderComponent,
  CardTitleComponent,
  CardDescriptionComponent,
  CardContentComponent,
} from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';

@Component({
  selector: 'app-webhook-settings',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DatePipe,
    TranslatePipe,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    AlertComponent,
    ButtonComponent,
    BadgeComponent,
    InputComponent,
  ],
  templateUrl: './webhook-settings.component.html',
})
export class WebhookSettingsComponent implements OnInit {
  private readonly webhookService = inject(WebhookService);
  private readonly entitlementService = inject(EntitlementService);
  private readonly fb = inject(FormBuilder);

  readonly eventOptions = WEBHOOK_EVENT_OPTIONS;
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly endpoints = signal<WebhookEndpoint[]>([]);
  readonly deliveries = signal<WebhookDelivery[]>([]);
  readonly createdSecret = signal('');
  readonly editingId = signal<string | null>(null);

  readonly hasWebhooks = () => this.entitlementService.has('WEBHOOKS');

  form: FormGroup = this.fb.group({
    url: ['', [Validators.required, Validators.maxLength(2048)]],
    description: ['', Validators.maxLength(500)],
    eventTypes: this.fb.control<WebhookEventType[]>([], Validators.required),
    active: [true],
  });

  ngOnInit(): void {
    this.entitlementService.load();
    if (this.hasWebhooks()) {
      this.load();
    }
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.webhookService.listEndpoints().subscribe({
      next: (endpoints) => {
        this.endpoints.set(endpoints);
        this.loading.set(false);
        this.loadDeliveries();
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.webhooks.loadError');
        this.loading.set(false);
      },
    });
  }

  loadDeliveries(): void {
    this.webhookService.listDeliveries(0, 10).subscribe({
      next: (page) => this.deliveries.set(page.content),
      error: () => this.deliveries.set([]),
    });
  }

  toggleEvent(eventType: WebhookEventType, checked: boolean): void {
    const current = [...(this.form.value.eventTypes as WebhookEventType[])];
    const next = checked
      ? Array.from(new Set([...current, eventType]))
      : current.filter((value) => value !== eventType);
    this.form.patchValue({ eventTypes: next });
    this.form.get('eventTypes')?.markAsTouched();
  }

  isEventSelected(eventType: WebhookEventType): boolean {
    return (this.form.value.eventTypes as WebhookEventType[]).includes(eventType);
  }

  startCreate(): void {
    this.editingId.set(null);
    this.createdSecret.set('');
    this.form.reset({ url: '', description: '', eventTypes: [], active: true });
  }

  startEdit(endpoint: WebhookEndpoint): void {
    this.editingId.set(endpoint.id);
    this.createdSecret.set('');
    this.form.patchValue({
      url: endpoint.url,
      description: endpoint.description ?? '',
      eventTypes: [...endpoint.eventTypes],
      active: endpoint.active,
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    this.success.set('');
    const payload = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id
      ? this.webhookService.updateEndpoint(id, payload)
      : this.webhookService.createEndpoint(payload);

    request$.subscribe({
      next: (result) => {
        if ('secret' in result && typeof result.secret === 'string') {
          this.createdSecret.set(result.secret);
        }
        this.saving.set(false);
        this.success.set(id ? 'settings.webhooks.updateSuccess' : 'settings.webhooks.createSuccess');
        this.startCreate();
        this.load();
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.webhooks.saveError');
        this.saving.set(false);
      },
    });
  }

  remove(endpoint: WebhookEndpoint): void {
    if (!confirm(`Delete webhook ${endpoint.url}?`)) {
      return;
    }
    this.webhookService.deleteEndpoint(endpoint.id).subscribe({
      next: () => {
        this.success.set('settings.webhooks.deleteSuccess');
        this.load();
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.webhooks.deleteError');
      },
    });
  }

  deliveryVariant(status: string): 'default' | 'secondary' | 'destructive' | 'outline' {
    if (status === 'SUCCESS') return 'default';
    if (status === 'FAILED' || status === 'DEAD') return 'destructive';
    return 'secondary';
  }
}
