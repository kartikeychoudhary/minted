---
title: API Feature Documentation Index
layer: api
---

# API Feature Documentation

Backend (Spring Boot) feature docs. Each file covers one domain: database schema, business rules, endpoints, and DTOs.

## Features

| File | Feature | Package | Routes |
|------|---------|---------|--------|
| [auth.md](auth.md) | Authentication & Signup | `com.minted.api.auth` | `/api/v1/auth/**` |
| [accounts.md](accounts.md) | Accounts & Account Types | `com.minted.api.account` | `/api/v1/accounts`, `/api/v1/account-types` |
| [transactions.md](transactions.md) | Transactions & Categories | `com.minted.api.transaction` | `/api/v1/transactions`, `/api/v1/categories` |
| [budgets.md](budgets.md) | Budgets | `com.minted.api.budget` | `/api/v1/budgets` |
| [analytics.md](analytics.md) | Analytics & Dashboard Cards | `com.minted.api.analytics`, `.dashboard`, `.dashboardconfig` | `/api/v1/analytics/**`, `/api/v1/dashboard/**`, `/api/v1/dashboard-config` |
| [recurring.md](recurring.md) | Recurring Transactions | `com.minted.api.recurring` | `/api/v1/recurring-transactions` |
| [notifications.md](notifications.md) | Notifications | `com.minted.api.notification` | `/api/v1/notifications/**` |
| [import.md](import.md) | Bulk CSV Import | `com.minted.api.bulkimport` | `/api/v1/bulk-import/**` |
| [statements.md](statements.md) | Credit Card Statement Parser & LLM | `com.minted.api.statement`, `.llm` | `/api/v1/statements/**`, `/api/v1/llm-config/**` |
| [splits.md](splits.md) | Splits & Friends | `com.minted.api.split`, `.friend` | `/api/v1/splits/**`, `/api/v1/friends/**` |
| [admin.md](admin.md) | Admin (Users, Jobs, Settings, Defaults) | `com.minted.api.admin` | `/api/v1/admin/**` |
| [infrastructure.md](infrastructure.md) | Project Setup, Security, Error Handling, Logging | `com.minted.api.common` | (cross-cutting) |

## Flyway Migration Map

| Migration | Description |
|-----------|-------------|
| V0_0_1 | users table |
| V0_0_2 | account_types table |
| V0_0_3 | accounts table |
| V0_0_4 | transaction_categories table |
| V0_0_5 | transactions table |
| V0_0_6 | budgets table |
| V0_0_7 | dashboard_cards table |
| V0_0_16 | job_schedule_configs, job_executions, job_step_executions |
| V0_0_17 | system_settings table |
| V0_0_20 | bulk_imports table |
| V0_0_22 | notifications table |
| V0_0_23 | llm_models table |
| V0_0_24 | llm_configurations table |
| V0_0_25 | credit_card_statements table |
| V0_0_26 | merchant_category_mappings table |
| V0_0_27 | system settings seed (parser + LLM key settings) |
| V0_0_30 | EMI default category |
| V0_0_31 | friends, split_transactions, split_shares tables |
| V0_0_36 | dashboard_configurations table |
| V0_0_38 | avatar columns for users |
| V0_0_39 | avatar columns for friends |
