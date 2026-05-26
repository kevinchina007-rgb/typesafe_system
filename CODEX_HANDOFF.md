# Codex Handoff

## 1. User's Core Expectations

The user is rebuilding this project to match course requirements and strongly prefers:

- Business-first code, not over-engineered industrial layering.
- Frontend and backend structures that mirror each other by business domain.
- Direct, explicit implementations that are easy for classmates and the instructor to read.
- No unnecessary Repository / Service / ApplicationService / DTO layers when the teacher's sample uses direct planners, direct objects, and direct tables.
- When the user gives a requirement, implement it fully rather than partially.
- When modifying a domain, do a closed-loop cleanup: change the feature, update callers, remove obsolete code, and compile.

## 2. Backend Rules Agreed With User

### Objects

- Domain `objects` must be passive data only.
- `final case class ...` is preferred.
- No business methods inside objects.
- No repository traits or sorting helpers left in `objects`.
- No `F[_]` in object files.
- Encoder / Decoder should be written directly beside the source/domain classes when possible.
- Prefer Circe auto derivation when it is enough.
- Avoid DTOs unless temporarily unavoidable for compilation.

### Tables

- Use plain JDBC SQL only.
- Do not use Doobie.
- Do not use `F[_]`.
- All database methods must accept `Connection`.
- All blocking JDBC work must be wrapped in `IO.blocking { ... }`.
- Return `IO[...]`.
- Use `PreparedStatement` / `ResultSet` with proper `try/finally`.
- SQL belongs in `tables` or `PlainSql` files, never in routes or planners.

### API / Planner Style

- Follow the teacher's planner logic, not a complex enterprise architecture.
- Ordinary business flow should be:

  `PlannerRouter -> XxxPlanner -> XxxTable / XxxPlainSql -> JDBC Connection`

- Planner must be a singleton `object`, never a `class`.
- Do not constructor-inject repositories or services.
- Use `cats.effect.IO`, never `F[_]`.
- If DB is needed:

  `object XxxPlanner extends ConnectionApiPlan[Request, Response]`

  `def plan(input: Request, connection: Connection): IO[Response]`

- If DB is not needed:

  `object XxxPlanner extends PlainApiPlan[Request, Response]`

  `def plan(input: Request): IO[Response]`

- Planner calls table functions directly, never repositories.
- Existing REST routes are being migrated away. Ordinary business entry should be `POST /api/XxxPlanner`.

### Routes

- Keep routes minimal.
- Target route layer should only retain shared routing infrastructure such as:
  - `Main.scala`
  - `ApiRouter.scala`
  - `HealthRouter.scala`
  - `PlannerRouter.scala`
  - `PlannerRegistry.scala`
  - `PlannerDefinitions.scala`
  - only minimal wiring/support files if still necessary
- Remove migrated `XxxApiRoutes.scala` files once their functionality is fully moved.
- Do not merely delete route files; every deleted route must have the matching planner/table/object migration completed first.

### Old-Layer Cleanup

- Remove old:
  - `Repository`
  - `Doobie...Repository`
  - `InMemory...`
  - `Service`
  - `ApplicationService`
  - obsolete DTOs
- Only keep what is still required for compilation while a feature is genuinely mid-migration.

## 3. Backend Work Already Done Earlier In This Thread

The user previously asked to:

- Convert many domain object files from OO style to passive data models.
- Remove object business methods.
- Add in-place Encoder / Decoder support.
- Convert many table operations to `IO` + JDBC plain SQL.
- Remove Doobie usage across many domains.
- Replace `F[_]` with `IO`.
- Move repository-like behavior from `objects` into `api`.
- Migrate route logic toward planner style and progressively delete old route files.
- Compress route structure toward the teacher's template.
- Move static-related concerns away from routes.
- Inspect and remove unnecessary `utils`, old repositories, services, application services, and in-memory leftovers across many domains.

The exact backend state should be re-checked before further work, but the user's rule is clear: whenever touching a backend domain, inspect the whole loop and leave it cleaner than before.

## 4. Frontend Structural Rules Agreed With User

- Components that belong to a page must live inside that page folder.
- Global state should use Zustand.
- Store files should be grouped by business meaning, e.g. user-related state in `user-store`.
- API and objects should be grouped by microservice/domain and mirror backend naming.
- Delete frontend DTOs when backend objects already define the contract.
- The user wants frontend and backend business directories to line up as much as practical.
- Avoid sprawling generic folders when a file really belongs to a business module.

## 5. Frontend Work Already Done Earlier In This Thread

- Refactored AccountPage components into the page folder.
- Reorganized stores, including user-related global state.
- Began removing CSS-file-heavy patterns in favor of Tailwind.
- Moved frontend API/object structure toward backend-aligned microservice folders.
- Deleted DTO layer in favor of using aligned object definitions directly.
- Removed disposable files such as `frontend/.vite-dev*.log`, `logs/`, and `sample/` when safe.
- Fixed:
  - Chinese-only mode
  - removed language selector
  - route arrow乱码
  - city auto-suggest visual behavior while preserving actual suggestion functionality

## 6. Current Homepage / Navigation Direction

The user likes the Waseda homepage style and wants:

- Full-screen hero slideshow at the top.
- Hero images slowly zoom while displayed.
- Lower-left location + romantic short tagline.
- Typewriter-style tagline.
- Top navigation over the hero image.
- No sticky nav after scrolling.
- Hover menus instead of a second fixed navigation bar.
- A logo at the upper left.
- Below the hero, empty business windows that will later be assigned to specific microservices.

### Current Hero Assets

Hero images are stored in:

`frontend/public/images/home-hero-candidates`

Current candidates:

- sea x2
- mountains x2
- roads x2
- snow x2
- daytime cities x3
- night cities x3

Source tracking:

`frontend/public/images/home-hero-candidates/_sources.md`

### Current Homepage Files

- `frontend/src/pages/HomePage/index.tsx`
- `frontend/src/pages/HomePage/homeSlides.ts`

### Current Hero Style Preferences

- Chinese copy should be concise and romantic.
- Taglines should stay short, e.g.:
  - `海与岩壁在这里相爱`
  - `群山把黄昏留给旅人`
  - `沿着风的方向继续出发`
- Location line should include Chinese + local/native spelling.
- Hero text should remain white.
- Hero tagline uses a more calligraphic / elegant Chinese serif feel.

### Current Navigation Preferences

- Light mode only.
- Overall UI should use sharper, square corners rather than rounded cards.
- Homepage:
  - nav over image
  - hover feedback should be light
  - submenu should be a white panel
- Non-home pages:
  - nav only at the top, not sticky
  - hover/active behavior should imitate the Waseda example with black blocks
  - submenu should be black
- Secondary nav menu:
  - centered labels
  - no individual item borders
  - no item fill backgrounds

### Current Logo

Current site logo asset:

`frontend/public/images/fly-pig-logo.png`

The original user-provided image had a fake checkerboard background, so it was processed to remove background and crop transparent margins.

## 7. Most Recent Frontend Changes

Recent files touched:

- `frontend/src/pages/HomePage/index.tsx`
- `frontend/src/pages/HomePage/homeSlides.ts`
- `frontend/src/app/shell/AppShell.tsx`
- `frontend/src/app/shell/TopNavBar.tsx`
- `frontend/src/app/shell/TopNavButton.tsx`
- `frontend/src/app/stores/app-shell-store.ts`
- `frontend/src/pages/AppPage/index.tsx`
- `frontend/src/styles/base.css`
- `frontend/src/main.tsx`

Recent behavior:

- dark mode removed from runtime logic
- light mode forced as the only theme
- homepage hero now full-screen
- old feature placeholders were restored below hero
- nav no longer sticky
- square visual treatment introduced
- homepage dropdown white
- non-home dropdown black

## 8. Important Caution For Next Codex

- Do not drift back into enterprise patterns.
- Do not add abstractions because they "feel clean."
- Before changing backend domains, inspect the whole domain for lingering violations:
  - methods in objects
  - `F[_]`
  - Doobie
  - repositories/services/application services/in-memory leftovers
- Before deleting old code, complete the replacement path first.
- On frontend tasks, favor business-module alignment over generic catch-all folders.
- The user dislikes repeated partial completion and will notice if only half the requested loop is done.
- The user asked that work stop when remaining context is extremely low instead of leaving a broken partial change.

## 9. Suggested Next Steps

1. Continue converting old frontend CSS page by page into Tailwind, starting with visible navigation and booking pages.
2. Keep refining the non-home navigation toward the reference screenshot.
3. Let the user decide which microservices should populate the homepage placeholder windows.
4. When returning to backend work, resume full closed-loop cleanup per domain rather than broad shallow edits.
