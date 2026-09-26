#!/usr/bin/env node
/**
 * Captures the README / gallery screenshots from a running Minted instance.
 *
 * Prerequisites: backend + frontend running, demo data seeded
 * (see scripts/screenshots/README.md). Writes PNGs to <repo>/screenshots/.
 *
 * Usage:
 *   node scripts/screenshots/capture-screenshots.mjs [--theme=dark|light] [--only=Dashboard,Transactions] [--scale=2]
 *
 * Env:
 *   MINTED_WEB_URL       default http://localhost:4200
 *   MINTED_USERNAME      default admin
 *   MINTED_PASSWORD      default Admin@1234
 *   MINTED_SCREENSHOTS_DIR  default <repo>/screenshots
 *   CHROMIUM_PATH        optional path to a Chromium executable
 */
import { chromium } from 'playwright';
import { mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const arg = (name, fallback) => {
  const hit = process.argv.find((a) => a.startsWith(`--${name}=`));
  return hit ? hit.split('=')[1] : fallback;
};

const WEB = (process.env.MINTED_WEB_URL || 'http://localhost:4200').replace(/\/$/, '');
const USERNAME = process.env.MINTED_USERNAME || 'admin';
const PASSWORD = process.env.MINTED_PASSWORD || 'Admin@1234';
const OUT = resolve(process.env.MINTED_SCREENSHOTS_DIR || resolve(here, '../../../screenshots'));
const THEME = arg('theme', 'dark');
const SCALE = Number(arg('scale', '2'));
const ONLY = arg('only', '')
  .split(',')
  .map((s) => s.trim())
  .filter(Boolean);
const VIEWPORT = { width: 1920, height: 950 };

// ---------------------------------------------------------------------------
// Shot list — file name => how to get there. Names match the README images.
// ---------------------------------------------------------------------------

const settingsTab = (name) => async (page) => {
  await page
    .locator('.settings-shell p-tab')
    .filter({ hasText: new RegExp(`^\\s*${name}\\s*$`) })
    .click();
};

const SHOTS = [
  { name: 'Login', path: '/login', auth: false },
  { name: 'Dashboard', path: '/' },
  { name: 'Dashboard_Light', path: '/', theme: 'light' },
  { name: 'Transactions', path: '/transactions' },
  { name: 'Recurring', path: '/recurring' },
  { name: 'Analytics', path: '/analytics' },
  { name: 'Splits', path: '/splits' },
  {
    name: 'Splits_Add_Transaction',
    path: '/splits',
    action: async (page) => page.getByRole('button', { name: 'Add Split' }).click(),
  },
  { name: 'Notifications', path: '/notifications' },
  { name: 'Import', path: '/import' },
  { name: 'Statements', path: '/statements' },
  { name: 'Upload_Statement', path: '/statements/new' },
  { name: 'Settings_Account_Types', path: '/settings', action: settingsTab('Account Types') },
  { name: 'Settings_Accounts', path: '/settings', action: settingsTab('Accounts') },
  { name: 'Settings_Categories', path: '/settings', action: settingsTab('Categories') },
  { name: 'Settings_Budgets', path: '/settings', action: settingsTab('Budgets') },
  { name: 'Settings_Profile', path: '/settings', action: settingsTab('Profile') },
  { name: 'Settings_LLM_Configuration', path: '/settings', action: settingsTab('LLM Configuration') },
  { name: 'Settings_Dashboard', path: '/settings', action: settingsTab('Dashboard') },
  { name: 'Admin_User_Management', path: '/admin/users' },
  { name: 'Admin_Server_Jobs', path: '/admin/jobs' },
  { name: 'Admin_Server_Settings_1', path: '/admin/settings' },
  {
    name: 'Admin_Server_Settings_2',
    path: '/admin/settings',
    action: async (page) => page.locator('.app-content').evaluate((el) => el.scrollTo(0, el.scrollHeight)),
  },
  { name: 'Sidebar_closed', path: '/', sidebarCollapsed: true },
];

// ---------------------------------------------------------------------------

async function newContext(browser, { theme, sidebarCollapsed = false }) {
  const context = await browser.newContext({ viewport: VIEWPORT, deviceScaleFactor: SCALE, reducedMotion: 'reduce' });
  await context.addInitScript(
    ({ dark, collapsed }) => {
      try {
        localStorage.setItem('minted-dark-mode', String(dark));
        localStorage.setItem('minted-privacy-mode', 'false');
        localStorage.setItem('minted-sidebar-collapsed', String(collapsed));
      } catch {
        /* storage unavailable */
      }
    },
    { dark: theme === 'dark', collapsed: sidebarCollapsed },
  );
  return context;
}

async function signIn(page) {
  await page.goto(`${WEB}/login`);
  const username = page.locator('input[formcontrolname=username]');
  await username.waitFor();
  await username.fill(USERNAME);
  await page.locator('p-password input').fill(PASSWORD);
  await page.locator('form button[type=submit]').click();
  // The app shell only renders for signed-in users
  await page.locator('.app-shell').waitFor({ timeout: 20000 }).catch(async () => {
    const shot = resolve(OUT, '_login-failure.png');
    await page.screenshot({ path: shot });
    throw new Error(`Sign-in as "${USERNAME}" failed (see ${shot}). Check MINTED_USERNAME / MINTED_PASSWORD.`);
  });
}

async function settle(page) {
  await page.waitForLoadState('networkidle').catch(() => {});
  // Let charts / skeletons finish rendering
  await page.waitForTimeout(1200);
}

async function main() {
  mkdirSync(OUT, { recursive: true });
  const shots = ONLY.length ? SHOTS.filter((s) => ONLY.includes(s.name)) : SHOTS;
  if (!shots.length) throw new Error(`No shots match --only=${ONLY.join(',')}`);

  const browser = await chromium.launch(process.env.CHROMIUM_PATH ? { executablePath: process.env.CHROMIUM_PATH } : {});
  const contexts = new Map(); // one signed-in context per theme/sidebar combination
  let failures = 0;

  try {
    for (const shot of shots) {
      const theme = shot.theme || THEME;
      const key = `${theme}|${!!shot.sidebarCollapsed}|${shot.auth !== false}`;
      if (!contexts.has(key)) {
        const context = await newContext(browser, { theme, sidebarCollapsed: shot.sidebarCollapsed });
        const page = await context.newPage();
        if (shot.auth !== false) await signIn(page);
        contexts.set(key, page);
      }
      const page = contexts.get(key);

      try {
        await page.goto(`${WEB}${shot.path}`);
        await settle(page);
        if (shot.action) {
          await shot.action(page);
          await settle(page);
        }
        await page.mouse.move(VIEWPORT.width - 1, VIEWPORT.height - 1); // no stray hover states
        await page.waitForTimeout(200);
        const file = resolve(OUT, `${shot.name}.png`);
        await page.screenshot({ path: file });
        console.log(`✔ ${shot.name}.png  (${theme})`);
      } catch (err) {
        failures++;
        console.error(`✖ ${shot.name}: ${err.message.split('\n')[0]}`);
      }
    }
  } finally {
    await browser.close();
  }

  console.log(`\n${shots.length - failures}/${shots.length} screenshots written to ${OUT}`);
  if (failures) process.exit(1);
}

main().catch((err) => {
  console.error(`✖ ${err.message}`);
  process.exit(1);
});
