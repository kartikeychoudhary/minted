#!/usr/bin/env node
/**
 * Seeds a running Minted backend with realistic demo data for screenshots.
 *
 * Talks only to the public REST API, so it works against any fresh install
 * (local, Docker, CI). Data is deterministic (seeded PRNG) and dated relative
 * to "today" so charts always look populated.
 *
 * Usage:
 *   node scripts/screenshots/seed-demo-data.mjs          # skip if demo data exists
 *   node scripts/screenshots/seed-demo-data.mjs --force  # seed again anyway
 *
 * Env:
 *   MINTED_API_URL           default http://localhost:5500/api/v1
 *   MINTED_USERNAME          default admin
 *   MINTED_PASSWORD          default Admin@1234  (set on first login if the
 *                            account still has the initial password)
 *   MINTED_INITIAL_PASSWORD  default admin
 */

const API = (process.env.MINTED_API_URL || 'http://localhost:5500/api/v1').replace(/\/$/, '');
const USERNAME = process.env.MINTED_USERNAME || 'admin';
const PASSWORD = process.env.MINTED_PASSWORD || 'Admin@1234';
const INITIAL_PASSWORD = process.env.MINTED_INITIAL_PASSWORD || 'admin';
const FORCE = process.argv.includes('--force');

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

let token = null;

async function api(method, path, body, { raw = false, form = null, allowFail = false } = {}) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  let payload;
  if (form) {
    payload = form;
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }
  const res = await fetch(`${API}${path}`, { method, headers, body: payload });
  const text = await res.text();
  if (!res.ok) {
    if (allowFail) return null;
    throw new Error(`${method} ${path} -> ${res.status}: ${text.slice(0, 300)}`);
  }
  if (raw) return text;
  if (!text) return {};
  const json = JSON.parse(text);
  return json.data !== undefined ? json.data : json;
}

// Deterministic PRNG (mulberry32) so every run produces the same data
function rng(seed) {
  return () => {
    seed |= 0;
    seed = (seed + 0x6d2b79f5) | 0;
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}
const rand = rng(20260101);
const pick = (arr) => arr[Math.floor(rand() * arr.length)];
const between = (min, max) => Math.round((min + rand() * (max - min)) * 100) / 100;

const today = new Date();
const iso = (d) => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
const monthStart = (offset) => new Date(today.getFullYear(), today.getMonth() - offset, 1);
const clampToToday = (d) => (d > today ? today : d);

async function login() {
  const tryLogin = async (password) => {
    const res = await fetch(`${API}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: USERNAME, password }),
    });
    if (!res.ok) return null;
    return (await res.json()).data;
  };

  let session = await tryLogin(PASSWORD);
  if (!session) {
    session = await tryLogin(INITIAL_PASSWORD);
    if (!session) throw new Error(`Cannot log in as "${USERNAME}" with MINTED_PASSWORD or MINTED_INITIAL_PASSWORD`);
  }
  token = session.token;

  if (session.user.forcePasswordChange) {
    console.log(`• First login — changing password for "${USERNAME}"`);
    await api('PUT', '/auth/change-password', {
      currentPassword: INITIAL_PASSWORD,
      newPassword: PASSWORD,
      confirmPassword: PASSWORD,
    });
    session = await tryLogin(PASSWORD);
    token = session.token;
  }
  return session.user;
}

// ---------------------------------------------------------------------------
// Demo content
// ---------------------------------------------------------------------------

// Realistic merchants per default category (names match the seeded categories)
const MERCHANTS = {
  'Food & Dining': ['Swiggy', 'Zomato', 'Starbucks Coffee', 'Domino\'s Pizza', 'Third Wave Coffee', 'Burger King', 'Social Bar & Kitchen'],
  Groceries: ['BigBasket', 'DMart', 'Blinkit', 'Zepto', 'Nature\'s Basket'],
  Transportation: ['Uber Ride', 'Ola Cabs', 'Indian Oil Fuel', 'Metro Card Recharge', 'Rapido'],
  Shopping: ['Amazon.in', 'Myntra', 'Flipkart', 'Decathlon', 'IKEA'],
  Entertainment: ['BookMyShow', 'PVR Cinemas', 'Steam Games', 'Spotify Premium'],
  Utilities: ['Tata Power Electricity', 'Airtel Fiber', 'Jio Postpaid', 'Mahanagar Gas'],
  Healthcare: ['Apollo Pharmacy', 'Practo Consultation', 'Dr. Lal PathLabs'],
  'Personal Care': ['Urban Company Salon', 'Nykaa', 'Cult.fit'],
  Travel: ['IndiGo Airlines', 'MakeMyTrip Hotel', 'IRCTC Tickets', 'Airbnb'],
  Education: ['Coursera', 'Udemy Course', 'Kindle Books'],
  'Gifts & Donations': ['Give India Donation', 'Birthday Gift', 'Ferns N Petals'],
  Insurance: ['HDFC Ergo Health Insurance', 'ICICI Lombard Motor'],
};

// Amount ranges per category (INR)
const RANGES = {
  'Food & Dining': [180, 1800], Groceries: [450, 3800], Transportation: [90, 1400], Shopping: [600, 6500],
  Entertainment: [199, 1200], Utilities: [650, 2600], Healthcare: [250, 2200], 'Personal Care': [400, 1800],
  Travel: [2500, 14000], Education: [450, 3200], 'Gifts & Donations': [500, 3000], Insurance: [1500, 4200],
};

// How often each category appears per month
const FREQUENCY = {
  'Food & Dining': 7, Groceries: 4, Transportation: 5, Shopping: 2, Entertainment: 2, Utilities: 2,
  Healthcare: 1, 'Personal Care': 1, Travel: 0.4, Education: 0.4, 'Gifts & Donations': 0.3, Insurance: 0.2,
};

async function main() {
  console.log(`Seeding demo data via ${API}`);
  const user = await login();
  console.log(`• Logged in as ${user.username}`);

  const existingAccounts = await api('GET', '/accounts');
  if (existingAccounts.some((a) => a.name === 'HDFC Savings') && !FORCE) {
    console.log('• Demo data already present — nothing to do (use --force to seed again).');
    return;
  }

  const accountTypes = await api('GET', '/account-types');
  const categories = await api('GET', '/categories');
  const typeId = (name) => (accountTypes.find((t) => t.name === name) || accountTypes[0]).id;
  const cat = (name) => categories.find((c) => c.name === name);

  // --- Accounts ------------------------------------------------------------
  const accountDefs = [
    { name: 'HDFC Savings', type: 'Savings Account', balance: 284250, icon: 'bank' },
    { name: 'ICICI Credit Card', type: 'Credit Card', balance: -23480, icon: 'credit-card' },
    { name: 'Cash Wallet', type: 'Cash', balance: 4200, icon: 'wallet' },
    { name: 'Zerodha Investments', type: 'Investment Account', balance: 512000, icon: 'chart-line' },
  ];
  const accounts = {};
  for (const a of accountDefs) {
    const created = await api('POST', '/accounts', {
      name: a.name, accountTypeId: typeId(a.type), balance: a.balance, currency: 'INR', icon: a.icon,
    });
    accounts[a.name] = created.id;
  }
  console.log(`• ${accountDefs.length} accounts`);

  // --- Transactions (6 months) ---------------------------------------------
  let txCount = 0;
  const addTx = async (tx) => {
    await api('POST', '/transactions', tx);
    txCount++;
  };
  const spendAccounts = [accounts['HDFC Savings'], accounts['ICICI Credit Card'], accounts['ICICI Credit Card'], accounts['Cash Wallet']];

  for (let m = 5; m >= 0; m--) {
    const start = monthStart(m);
    const on = (day) => clampToToday(new Date(start.getFullYear(), start.getMonth(), day));
    if (on(1) > today) continue;

    // Income
    await addTx({ amount: 145000, type: 'INCOME', description: 'Salary — Acme Technologies', transactionDate: iso(on(1)),
      accountId: accounts['HDFC Savings'], categoryId: cat('Salary').id });
    if (rand() < 0.5) {
      await addTx({ amount: between(12000, 38000), type: 'INCOME', description: 'Freelance design project', transactionDate: iso(on(12)),
        accountId: accounts['HDFC Savings'], categoryId: cat('Freelance').id });
    }
    if (rand() < 0.35) {
      await addTx({ amount: between(800, 4200), type: 'INCOME', description: 'Mutual fund dividend', transactionDate: iso(on(18)),
        accountId: accounts['Zerodha Investments'], categoryId: cat('Investments').id });
    }

    // Fixed monthly expenses
    await addTx({ amount: 32000, type: 'EXPENSE', description: 'Apartment rent', transactionDate: iso(on(3)),
      accountId: accounts['HDFC Savings'], categoryId: cat('Housing & Rent').id });
    await addTx({ amount: 649, type: 'EXPENSE', description: 'Netflix', transactionDate: iso(on(5)),
      accountId: accounts['ICICI Credit Card'], categoryId: cat('Subscriptions').id });
    await addTx({ amount: 119, type: 'EXPENSE', description: 'Spotify Premium', transactionDate: iso(on(7)),
      accountId: accounts['ICICI Credit Card'], categoryId: cat('Subscriptions').id });
    if (cat('EMI')) {
      await addTx({ amount: 18450, type: 'EXPENSE', description: 'Car loan EMI', transactionDate: iso(on(10)),
        accountId: accounts['HDFC Savings'], categoryId: cat('EMI').id });
    }

    // Variable spending
    for (const [category, perMonth] of Object.entries(FREQUENCY)) {
      const c = cat(category);
      if (!c) continue;
      const count = Math.floor(perMonth) + (rand() < perMonth % 1 ? 1 : 0);
      for (let i = 0; i < count; i++) {
        const [min, max] = RANGES[category];
        await addTx({
          amount: between(min, max), type: 'EXPENSE', description: pick(MERCHANTS[category]),
          transactionDate: iso(on(1 + Math.floor(rand() * 27))), accountId: pick(spendAccounts), categoryId: c.id,
        });
      }
    }
  }
  console.log(`• ${txCount} transactions over 6 months`);

  // --- Recurring -----------------------------------------------------------
  const recurringDefs = [
    { name: 'Salary — Acme Technologies', amount: 145000, type: 'INCOME', category: 'Salary', account: 'HDFC Savings', day: 1 },
    { name: 'Apartment Rent', amount: 32000, type: 'EXPENSE', category: 'Housing & Rent', account: 'HDFC Savings', day: 3 },
    { name: 'Netflix Subscription', amount: 649, type: 'EXPENSE', category: 'Subscriptions', account: 'ICICI Credit Card', day: 5 },
    { name: 'Airtel Fiber Broadband', amount: 1178, type: 'EXPENSE', category: 'Utilities', account: 'HDFC Savings', day: 8 },
    { name: 'Cult.fit Membership', amount: 2499, type: 'EXPENSE', category: 'Personal Care', account: 'ICICI Credit Card', day: 12 },
    { name: 'Term Insurance Premium', amount: 3150, type: 'EXPENSE', category: 'Insurance', account: 'HDFC Savings', day: 20 },
  ];
  const recurringIds = [];
  for (const r of recurringDefs) {
    const created = await api('POST', '/recurring-transactions', {
      name: r.name, amount: r.amount, type: r.type, categoryId: cat(r.category).id, accountId: accounts[r.account],
      frequency: 'MONTHLY', dayOfMonth: r.day, startDate: iso(monthStart(0)),
    });
    recurringIds.push(created.id);
  }
  // Pause one so the "paused" state is visible
  await api('PATCH', `/recurring-transactions/${recurringIds[4]}/toggle`, undefined, { allowFail: true });
  console.log(`• ${recurringDefs.length} recurring schedules`);

  // --- Budgets (current month) ---------------------------------------------
  const budgetDefs = [
    ['Food & Dining', 12000], ['Groceries', 10000], ['Transportation', 6000], ['Shopping', 8000], ['Entertainment', 3000],
  ];
  for (const [category, amount] of budgetDefs) {
    await api('POST', '/budgets', {
      name: `${category} budget`, amount, month: today.getMonth() + 1, year: today.getFullYear(), categoryId: cat(category).id,
    });
  }
  console.log(`• ${budgetDefs.length} budgets`);

  // --- Friends & splits ----------------------------------------------------
  const friendDefs = [
    ['Aarav Mehta', '#6366f1'], ['Priya Sharma', '#ec4899'], ['Rohan Gupta', '#10b981'], ['Sneha Iyer', '#f59e0b'], ['Kabir Singh', '#0ea5e9'],
  ];
  const friends = [];
  for (const [name, color] of friendDefs) {
    const f = await api('POST', '/friends', { name, email: `${name.split(' ')[0].toLowerCase()}@example.com`, avatarColor: color });
    friends.push(f.id);
  }
  const split = (description, categoryName, total, days, members) => {
    const share = Math.round((total / (members.length + 1)) * 100) / 100;
    return api('POST', '/splits', {
      description, categoryName, totalAmount: total, splitType: 'EQUAL',
      transactionDate: iso(new Date(today.getTime() - days * 86400000)),
      shares: [{ friendId: null, shareAmount: share, isPayer: true }, ...members.map((id) => ({ friendId: id, shareAmount: share, isPayer: false }))],
    });
  };
  await split('Goa trip — villa booking', 'Travel', 36000, 20, [friends[0], friends[1], friends[2]]);
  await split('Team dinner at Social', 'Food & Dining', 7200, 9, [friends[3], friends[4]]);
  await split('Concert tickets', 'Entertainment', 9000, 4, [friends[1], friends[3]]);
  await split('Weekend groceries', 'Groceries', 2400, 2, [friends[2]]);
  await api('POST', '/splits/settle', { friendId: friends[4] }, { allowFail: true });
  console.log(`• ${friendDefs.length} friends, 4 splits`);

  // --- CSV import (populates import history + a job run) -------------------
  const csv = [
    'date,amount,type,description,categoryName,notes,tags',
    `${iso(new Date(today.getTime() - 3 * 86400000))},540,EXPENSE,Chai Point,Food & Dining,,cafe`,
    `${iso(new Date(today.getTime() - 3 * 86400000))},1299,EXPENSE,Amazon.in — headphones,Shopping,,`,
    `${iso(new Date(today.getTime() - 2 * 86400000))},310,EXPENSE,Uber Ride,Transportation,,commute`,
    `${iso(new Date(today.getTime() - 1 * 86400000))},2150,EXPENSE,DMart,Groceries,,`,
  ].join('\n');
  const importForm = new FormData();
  importForm.append('file', new Blob([csv], { type: 'text/csv' }), 'bank-export.csv');
  importForm.append('accountId', String(accounts['HDFC Savings']));
  const upload = await api('POST', '/imports/upload', undefined, { form: importForm, allowFail: true });
  const importId = upload?.importId ?? upload?.id;
  if (importId) {
    await api('POST', '/imports/confirm', { importId, skipDuplicates: true }, { allowFail: true });
    console.log('• CSV import job');
  }

  // --- Statement upload (text extraction only; parsing needs an LLM key) ---
  const statement = [
    'ICICI Bank Credit Card Statement',
    `Statement period: ${iso(monthStart(1))} to ${iso(monthStart(0))}`,
    'Date, Description, Amount',
    `${iso(monthStart(1))}, SWIGGY BANGALORE, 486.00`,
    `${iso(monthStart(1))}, AMAZON PAY INDIA, 2349.00`,
    `${iso(monthStart(1))}, INDIAN OIL PETROL, 1800.00`,
  ].join('\n');
  const stmtForm = new FormData();
  stmtForm.append('file', new Blob([statement], { type: 'text/csv' }), 'icici-card-statement.csv');
  stmtForm.append('accountId', String(accounts['ICICI Credit Card']));
  if (await api('POST', '/statements/upload', undefined, { form: stmtForm, allowFail: true })) {
    console.log('• Statement upload');
  }

  // --- Admin extras ---------------------------------------------------------
  if (user.role === 'ADMIN') {
    await api('POST', '/admin/users', {
      username: 'priya', password: 'Demo@12345', displayName: 'Priya Sharma', email: 'priya@example.com', role: 'USER',
    }, { allowFail: true });
    await api('POST', '/admin/jobs/RECURRING_TRANSACTION_PROCESSOR/trigger', undefined, { allowFail: true });
    console.log('• Admin: extra user + recurring job run');
  }

  console.log('✔ Demo data ready');
}

main().catch((err) => {
  console.error(`✖ ${err.message}`);
  process.exit(1);
});
