import { test, expect } from '@playwright/test';

const DEMO_EMAIL = process.env.E2E_DEMO_EMAIL ?? 'admin@demo.com';
const DEMO_PASSWORD = process.env.E2E_DEMO_PASSWORD ?? 'password123';

test.describe('Organization context', () => {
  test('dashboard remains available after reload for demo admin', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#email').fill(DEMO_EMAIL);
    await page.locator('#password').fill(DEMO_PASSWORD);
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page).toHaveURL(/\/dashboard/);

    await page.reload();
    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.getByRole('heading', { level: 1, name: 'Dashboard' })).toBeVisible();
  });
});
