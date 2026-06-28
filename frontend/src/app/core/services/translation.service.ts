import { ApplicationRef, Injectable, inject, signal, effect } from '@angular/core';

export type Language = 'en' | 'fr';

export interface Translations {
  [key: string]: string | Translations;
}

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private readonly appRef = inject(ApplicationRef);
  private readonly LANGUAGE_KEY = 'app-language';
  private readonly translations: Map<Language, Translations> = new Map();
  
  readonly currentLanguage = signal<Language>(this.getInitialLanguage());
  readonly translationsLoaded = signal<boolean>(false);
  readonly languageTick = signal(0);
  
  private readonly translationMemo = new Map<string, string>();

  constructor() {
    // Load translations on initialization
    this.loadTranslations();
    
    // Apply language whenever it changes
    effect(() => {
      const lang = this.currentLanguage();
      this.applyLanguage(lang);
    });
  }

  setLanguage(language: Language): void {
    this.currentLanguage.set(language);
    this.languageTick.update((value) => value + 1);
    this.translationMemo.clear();
    localStorage.setItem(this.LANGUAGE_KEY, language);
    this.appRef.tick();
  }

  translate(key: string, params?: { [key: string]: string | number }): string {
    const language = this.currentLanguage();
    const cacheKey = `${language}:${key}:${JSON.stringify(params ?? {})}`;
    const cached = this.translationMemo.get(cacheKey);
    if (cached !== undefined) {
      return cached;
    }

    const translations = this.translations.get(language);
    if (!translations) {
      return key;
    }

    const value = this.getNestedValue(translations, key);

    if (typeof value !== 'string') {
      return key;
    }

    const result = params ? this.replaceParams(value, params) : value;
    this.translationMemo.set(cacheKey, result);
    return result;
  }

  private getNestedValue(obj: Translations, path: string): string | Translations | undefined {
    const keys = path.split('.');
    let current: string | Translations | undefined = obj;

    for (const key of keys) {
      if (current && typeof current === 'object' && key in current) {
        current = current[key];
      } else {
        return undefined;
      }
    }

    return current;
  }

  private replaceParams(text: string, params: { [key: string]: string | number }): string {
    let result = text;
    for (const [key, value] of Object.entries(params)) {
      result = result.replace(new RegExp(`{{${key}}}`, 'g'), String(value));
    }
    return result;
  }

  private getInitialLanguage(): Language {
    const saved = localStorage.getItem(this.LANGUAGE_KEY) as Language;
    if (saved === 'en' || saved === 'fr') {
      return saved;
    }

    // Fall back to browser language
    const browserLang = navigator.language.split('-')[0].toLowerCase();
    return browserLang === 'fr' ? 'fr' : 'en';
  }

  private applyLanguage(language: Language): void {
    document.documentElement.setAttribute('lang', language);
  }

  private async loadTranslations(): Promise<void> {
    try {
      const [enResponse, frResponse] = await Promise.all([
        fetch('/assets/i18n/en.json'),
        fetch('/assets/i18n/fr.json')
      ]);

      const [enTranslations, frTranslations] = await Promise.all([
        enResponse.json(),
        frResponse.json()
      ]);

      this.translations.set('en', enTranslations);
      this.translations.set('fr', frTranslations);
      this.translationMemo.clear();
      this.languageTick.update((value) => value + 1);
      this.translationsLoaded.set(true);
      this.appRef.tick();
    } catch (error) {
      console.error('Failed to load translations:', error);
      // Fallback to empty translations
      this.translations.set('en', {});
      this.translations.set('fr', {});
      this.translationsLoaded.set(true);
    }
  }
}
