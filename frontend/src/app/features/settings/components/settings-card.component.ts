import { Component, input } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import {
  CardComponent,
  CardContentComponent,
  CardDescriptionComponent,
  CardFooterComponent,
  CardHeaderComponent,
  CardTitleComponent,
} from '../../../shared/components/ui/card/card.component';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-settings-card',
  imports: [
    TranslatePipe,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    CardFooterComponent,
    LucideAngularModule,
  ],
  template: `
    <app-card>
      @if (titleKey()) {
        <app-card-header class="space-y-1 px-5 pb-3 pt-5">
          <div class="flex items-center gap-2.5">
            @if (icon()) {
              <lucide-icon [name]="icon()!" [size]="18" class="shrink-0 text-muted-foreground" />
            }
            <app-card-title class="settings-card-title">{{ titleKey() | translate }}</app-card-title>
          </div>
          @if (descriptionKey()) {
            <app-card-description class="settings-description">
              {{ descriptionKey() | translate }}
            </app-card-description>
          }
        </app-card-header>
      }
      <app-card-content class="px-5 pb-5 pt-0">
        @if (divided()) {
          <div class="divide-y divide-border/60">
            <ng-content />
          </div>
        } @else {
          <ng-content />
        }
      </app-card-content>
      @if (hasFooter()) {
        <app-card-footer class="justify-end px-5 py-3">
          <ng-content select="[settingsCardFooter]" />
        </app-card-footer>
      }
    </app-card>
  `,
})
export class SettingsCardComponent {
  readonly titleKey = input<string>('');
  readonly descriptionKey = input<string>('');
  readonly icon = input<string>('');
  readonly hasFooter = input(false);
  readonly divided = input(true);
}
