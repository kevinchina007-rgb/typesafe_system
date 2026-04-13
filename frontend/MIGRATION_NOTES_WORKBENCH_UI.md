# Travel Workbench UI Migration Notes

## What Changed

- Replaced the flat module list with a grouped `Travel Workbench` information architecture.
- Added a typed route metadata layer in `src/app/navigation.ts`.
- Added a premium dark SaaS design system with reusable tokens and UI primitives.
- Added a typed SVG icon system for sidebar and section-level semantics.
- Added a hero background system for the Overview page with low-cost CSS effects.

## New Frontend Building Blocks

- `src/app/designTokens.ts`
  - Design token source for spacing, radius, typography, shadows, and semantic colors.
- `src/components/icons/Icons.tsx`
  - Typed outline icon system for Workbench sections and modules.
- `src/components/ui/UIComponents.tsx`
  - Shared UI primitives:
    - `AppCard`
    - `SectionHeader`
    - `PrimaryButton`
    - `SecondaryButton`
    - `SearchPanel`
    - `EmptyState`
    - `StatCard`
    - `ActionBar`
- `src/components/HeroBackground.tsx`
  - Overview hero background with gradients, grid, travel-route arcs, and glow layers.

## Route and Navigation Notes

- Route semantics stay frontend-only. Backend microservice boundaries are unchanged.
- Legacy compatibility remains in `MvpApp.tsx`:
  - `bookings -> orders`
  - `explore -> smartPlanner`
  - `trainAdmin / attractionAdmin -> manager`

## Recommended Migration Path For Remaining Pages

1. Wrap each feature page in `AppCard` instead of raw `page-card` where practical.
2. Start each page with:
   - Eyebrow label
   - Title
   - Subtitle
   - Action row
   - Content cards
3. Replace repeated action button groups with `ActionBar`.
4. Replace custom summary tiles with `StatCard`.
5. Use `SectionHeader` instead of ad hoc title blocks.

## Scala / Laminar Mapping

The current repository frontend is implemented with React + TypeScript. This refactor mirrors the requested Scala/Laminar concepts in the live codebase:

- `RouteMeta` -> typed route metadata object
- `AppIcon` sealed trait idea -> string union icon enum
- `AppShell` -> unified shell component
- `DashboardPage` -> `WorkspaceOverviewPage`
- `designTokens.scala` -> `designTokens.ts`

This keeps the architecture aligned with your intended Scala model while remaining runnable in the current project.
