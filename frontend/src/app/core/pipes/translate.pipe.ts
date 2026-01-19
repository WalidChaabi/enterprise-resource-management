import { Pipe, PipeTransform, inject, ChangeDetectorRef } from '@angular/core';
import { TranslationService } from '../services/translation.service';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false // Make it impure to react to language changes
})
export class TranslatePipe implements PipeTransform {
  private readonly translationService = inject(TranslationService);
  private readonly cdr = inject(ChangeDetectorRef);

  transform(key: string, params?: { [key: string]: string | number }): string {
    // Access the signal to trigger change detection when language changes
    this.translationService.currentLanguage();
    
    return this.translationService.translate(key, params);
  }
}
