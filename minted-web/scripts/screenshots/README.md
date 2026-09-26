# Screenshot automation

Regenerates every image in [`/screenshots`](../../../screenshots) (used by the root `README.md`
and `screenshots/README.md`) from a running Minted instance, using realistic demo data.

| Script | What it does |
|--------|--------------|
| `seed-demo-data.mjs` | Seeds demo data through the REST API: 4 accounts, ~6 months of transactions, recurring schedules (one paused), budgets, friends & splits, a CSV import, a statement upload, an extra user and a job run. Deterministic, and skips itself if the demo data already exists. |
| `capture-screenshots.mjs` | Signs in with Playwright and captures every page/dialog listed in `SHOTS` at 1920×950 @2x (3840×1900 PNGs). |

## Quick start

Use a **fresh database** so the numbers only come from the demo data.

```bash
# 1. Start MySQL + backend (default http://localhost:5500) and the frontend (default http://localhost:4200)
cd minted-api && ./gradlew bootRun          # in one terminal
cd minted-web && npm start                   # in another

# 2. One-time: install the Playwright browser
cd minted-web
npx playwright install chromium

# 3. Seed demo data (changes the admin password from "admin" to Admin@1234 on first run)
npm run screenshots:seed

# 4. Capture everything into ../screenshots
npm run screenshots
```

With Docker Compose instead (frontend served by nginx on port 80, API proxied under `/api`):

```bash
docker compose up -d
MINTED_API_URL=http://localhost/api/v1 npm run screenshots:seed
MINTED_WEB_URL=http://localhost npm run screenshots
```

## Options

```bash
npm run screenshots -- --theme=light                       # default: dark (Dashboard_Light is always light)
npm run screenshots -- --only=Dashboard,Transactions       # capture a subset
npm run screenshots -- --scale=1                           # 1920×950 instead of 2x
npm run screenshots:seed -- --force                        # seed again even if demo data exists
```

| Env var | Default | Used by |
|---------|---------|---------|
| `MINTED_API_URL` | `http://localhost:5500/api/v1` | seed |
| `MINTED_WEB_URL` | `http://localhost:4200` | capture |
| `MINTED_USERNAME` | `admin` | both |
| `MINTED_PASSWORD` | `Admin@1234` | both |
| `MINTED_INITIAL_PASSWORD` | `admin` | seed (first-login password change) |
| `MINTED_SCREENSHOTS_DIR` | `<repo>/screenshots` | capture |
| `CHROMIUM_PATH` | Playwright's bundled Chromium | capture |

## Adding a screenshot

Add an entry to `SHOTS` in `capture-screenshots.mjs`:

```js
{ name: 'My_Page', path: '/my-page' },                                  // simple page
{ name: 'My_Dialog', path: '/my-page', action: (page) => page.getByRole('button', { name: 'Open' }).click() },
{ name: 'My_Light', path: '/my-page', theme: 'light' },                 // force a theme
```

Then reference `./screenshots/My_Page.png` from the README. Privacy mode is always turned off and the
mouse is parked in a corner before each capture, so there are no blurred amounts or stray hover states.
