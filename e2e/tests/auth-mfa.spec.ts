import { test, expect } from '@playwright/test';

test.describe('MFA verify page', () => {
  test('shows error when MFA session token is missing', async ({ page }) => {
    await page.goto('/mfa-verify');
    await page.getByLabel(/verification code/i).fill('123456');
    await page.getByRole('button', { name: /verify/i }).click();
    await expect(page.getByText(/session|invalid/i)).toBeVisible();
  });
});
