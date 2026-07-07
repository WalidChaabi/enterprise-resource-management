import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MfaVerifyComponent } from './mfa-verify.component';
import { AuthService } from '../../../core/services/auth.service';

describe('MfaVerifyComponent', () => {
  let fixture: ComponentFixture<MfaVerifyComponent>;
  let authService: jasmine.SpyObj<Pick<AuthService, 'verifyMfa'>>;
  let router: jasmine.SpyObj<Pick<Router, 'navigateByUrl'>>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AuthService', ['verifyMfa']);
    router = jasmine.createSpyObj('Router', ['navigateByUrl']);

    await TestBed.configureTestingModule({
      imports: [MfaVerifyComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParams: { token: 'mfa-token', returnUrl: '/dashboard' } },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MfaVerifyComponent);
    fixture.detectChanges();
  });

  it('submits MFA code and navigates on success', () => {
    authService.verifyMfa.and.returnValue(of({
      accessToken: 'token',
      tokenType: 'Bearer',
      userId: '1',
      email: 'user@example.com',
      firstName: 'User',
      lastName: 'Example',
      organizationId: 'org-1',
      roles: ['EMPLOYEE'],
    }));

    fixture.componentInstance.form.setValue({ code: '123456' });
    fixture.componentInstance.onSubmit();

    expect(authService.verifyMfa).toHaveBeenCalledWith({ mfaToken: 'mfa-token', code: '123456' });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard');
  });

  it('shows an error when MFA verification fails', () => {
    authService.verifyMfa.and.returnValue(throwError(() => ({ details: ['Invalid code'] })));

    fixture.componentInstance.form.setValue({ code: '123456' });
    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.errorMessage()).toBe('Invalid code');
  });
});
