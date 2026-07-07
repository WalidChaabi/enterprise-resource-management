import { test, expect } from '@playwright/test';

const DEMO_EMAIL = process.env.E2E_DEMO_EMAIL ?? 'admin@demo.com';
const DEMO_PASSWORD = process.env.E2E_DEMO_PASSWORD ?? 'password123';

async function loginAsDemoAdmin(page: import('@playwright/test').Page) {
  await page.goto('/login');
  await page.locator('#email').fill(DEMO_EMAIL);
  await page.locator('#password').fill(DEMO_PASSWORD);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard/);
}

test.describe('Employees', () => {
  test('admin can open employee list', async ({ page }) => {
    await loginAsDemoAdmin(page);
    await page.goto('/employees');
    await expect(page.getByRole('heading', { level: 1, name: /Employees/i })).toBeVisible();
  });
});
