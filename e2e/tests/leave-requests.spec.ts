import { test, expect } from '@playwright/test';

const DEMO_EMAIL = process.env.E2E_DEMO_EMAIL ?? 'employee@demo.com';
const DEMO_PASSWORD = process.env.E2E_DEMO_PASSWORD ?? 'password123';

test.describe('Leave requests', () => {
  test('employee can open leave request list', async ({ page }) => {
    await page.goto('/login');
    await page.locator('#email').fill(DEMO_EMAIL);
    await page.locator('#password').fill(DEMO_PASSWORD);
    await page.getByRole('button', { name: 'Sign In' }).click();
    await expect(page).toHaveURL(/\/dashboard/);

    await page.goto('/leave-requests');
    await expect(page.getByRole('heading', { level: 1, name: /Leave Requests/i })).toBeVisible();
  });
});
