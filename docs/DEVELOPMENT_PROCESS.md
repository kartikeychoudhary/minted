# DEVELOPMENT_PROCESS.md — How to Build Each Feature

> Follow this process for EVERY feature. No shortcuts.

---

## Phase 0: Pre-Flight Checks

Before starting ANY work:

- [ ] Read `docs/MISTAKES.md` in full
- [ ] Read `CLAUDE.md` to refresh tech stack rules
- [ ] Confirm which spec file is relevant (`docs/BACKEND_SPEC.md`, `docs/FRONTEND_SPEC.md`, `docs/UI_UX_SPEC.md`, `docs/API_SPEC.md`)
- [ ] Ensure the project builds cleanly (`./gradlew build` and `ng build`)

---

## Phase 1: Understand the Requirement

1. Re-read the feature description from the spec document.
2. Identify all **entities** involved (database tables, DTOs, models).
3. Identify all **API endpoints** needed (from `API_SPEC.md`).
4. Identify which **frontend module** this belongs to.
5. Write a short 3–5 line summary of what you will do before writing code.

**Gate:** Do NOT proceed until you can articulate clearly: "I will create X entity, Y endpoint, Z component."

---

## Phase 2: Backend First — Database → Entity → Repository → Service → Controller

Follow this order strictly:

### Step 2.1: Database Migration
- Create a new Flyway migration file: `V<next_version>__description.sql`
- Versioning format: `V0_0_1`, `V0_0_2`, `V0_0_3`, etc.
- Include `CREATE TABLE`, `ALTER TABLE`, seed data as needed.
- **Always include foreign key constraints.**
- **Always include `created_at` and `updated_at` timestamp columns.**
- Run: `./gradlew flywayMigrate` — confirm it passes.

### Step 2.2: JPA Entity
- Create entity class in `<feature>/entity/` package (e.g., `account/entity/Account.java`).
- Use `@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Use `@Column` with `nullable`, `length`, `unique` as appropriate.
- Add `@CreationTimestamp` and `@UpdateTimestamp` for audit fields.
- Add proper `@ManyToOne`, `@OneToMany` relationships with `@JoinColumn`.
- **Do NOT use `@Data` from Lombok on entities** (causes issues with lazy loading). Use `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` separately.
- **Add explicit imports for cross-module entity references** (e.g., `import com.minted.api.user.entity.User;` in `Account.java`).

### Step 2.3: Repository
- Create interface in `<feature>/repository/` extending `JpaRepository<Entity, Long>`.
- Add custom query methods using Spring Data naming conventions or `@Query`.
- Add methods needed for filtering: `findByUserIdAndDateBetween(...)`, etc.
- **If JPQL queries reference enums by fully-qualified name, use the new package path** (e.g., `com.minted.api.transaction.enums.TransactionType.EXPENSE`).

### Step 2.4: DTOs
- Create Request and Response DTOs in `<feature>/dto/` package.
- Use `record` classes where possible (Java 17 feature).
- Add `@NotNull`, `@NotBlank`, `@Size` validations on request DTOs.
- **Never expose entity objects directly in API responses.**

### Step 2.5: Service Layer
- Create interface in `<feature>/service/`.
- Create implementation in `<feature>/service/` (no separate `impl/` sub-package).
- Handle all business logic here, not in controllers.
- Throw custom exceptions (from `common/exception/` package) — never return null for error cases.

### Step 2.6: Controller
- Create in `<feature>/controller/` package.
- Use `@RestController`, `@RequestMapping("/api/v1/<resource>")`.
- Use `@Valid` on request body parameters.
- Return `ResponseEntity<>` with appropriate HTTP status codes.
- Keep controllers thin — delegate to service layer.

### Step 2.x: Package Structure Reference
The backend uses **feature-based modules** where each domain's files live together:
```
com.minted.api.<feature>/
├── controller/     # REST endpoint(s)
├── dto/            # Request/Response DTOs
├── entity/         # JPA entity(ies)
├── enums/          # Enumerations (if any)
├── job/            # Scheduled jobs (if any)
├── repository/     # Spring Data JPA repository(ies)
└── service/        # Interface(s) + implementation(s)
```
Shared infrastructure lives in `common/` (config, exception, filter, util).

### Step 2.7: Test
- Write at least one happy-path test for the service method.
- Run: `./gradlew test` — confirm it passes.

**Gate:** Backend must compile, migrations must run, and at least basic tests must pass before moving to frontend.

---

## Phase 3: Frontend — Module → Model → Service → Component → Template

### Step 3.1: Model/Interface
- Create TypeScript interfaces in `core/models/` matching backend DTOs.
- Export them from a barrel file (`index.ts`).

### Step 3.2: API Service
- Create or update service in `core/services/`.
- Use `HttpClient` for API calls.
- Return `Observable<T>` from all methods.
- Use the environment file for base URL: `environment.apiUrl`.

### Step 3.3: Module Setup
- If the feature belongs to an existing module, skip.
- Otherwise: `ng generate module modules/<n> --routing`.
- Add route in `app-routing.module.ts` with lazy loading:
  ```typescript
  { path: '<route>', loadChildren: () => import('./modules/<n>/<n>.module').then(m => m.<N>Module) }
  ```

### Step 3.4: Component
- Generate: `ng generate component modules/<n>/components/<comp> --module=modules/<n> --standalone=false`
- **Verify** the component is declared in the module (not standalone).
- If the generated component has `standalone: true`, delete it and regenerate with `--standalone=false`.

### Step 3.5: Template & Styling
- Use PrimeNG components as the primary UI toolkit.
- Use Tailwind CSS for layout and spacing.
- Use AG Grid for any tabular data display.
- Follow the `UI_UX_SPEC.md` for design guidelines.
- **PrimeNG theme must be consistent** — use the configured theme across all pages.

### Step 3.6: Verify
- Run `ng serve` — confirm no compilation errors.
- Navigate to the feature in the browser — confirm it renders.
- Test basic CRUD operations against the running backend.

**Gate:** Feature must be visually functional and connected to the backend before considering it done.

---

## Phase 4: Integration & Polish

1. Test the full flow: login → navigate → use feature → verify data persists.
2. Check responsive design on mobile viewport (375px width minimum).
3. Verify error handling: what happens when the API returns 400, 401, 500?
4. Add loading states (PrimeNG `p-progressSpinner` or skeleton).
5. Add toast notifications for success/error using PrimeNG `MessageService`.

---

## Phase 5: Commit Checkpoint

1. Ensure `./gradlew build` passes.
2. Ensure `ng build` passes with no warnings.
3. Re-read `MISTAKES.md` — did you add any new learnings?
4. Write a clear summary of what was implemented.

---

## Decision Parameters — When to Proceed vs. Ask

### ✅ Proceed Without Asking:
- Standard CRUD for an entity defined in the spec
- Adding a Flyway migration for a new table
- Creating a component within the defined module structure
- Fixing a build error or test failure
- Adding validation annotations to DTOs

### ⛔ STOP and Ask the Developer:
- Adding a new dependency/library not in the tech stack
- Changing the database schema in a way not defined in the spec
- Removing or replacing an existing feature
- Changing the auth mechanism
- Any architectural change (e.g., adding WebSockets, changing folder structure)
- If a feature is ambiguous and could be implemented multiple ways

### ⚠️ Proceed but Flag:
- If a PrimeNG component doesn't fit the UI spec well and you used a workaround
- If an AG Grid feature requires a paid-only feature — flag and use alternative
- If a Flyway migration needs to modify existing data (flag as potentially destructive)
