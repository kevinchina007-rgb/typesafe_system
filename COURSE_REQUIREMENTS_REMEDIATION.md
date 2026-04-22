# Course Requirements Remediation

This file records a focused remediation pass against `lec1` to `lec6`, prioritising the project issues that most directly affect grading, maintainability, and day-to-day development reliability.

## What Was Fixed In This Pass

### 1. Repository hygiene and local/runtime isolation

Course expectation alignment:
- `lec2`: stable local development environment
- `lec5`: correct Git workflow and clean repository discipline

Changes:
- Added a root `.gitignore`
- Ignored local launcher logs, local PostgreSQL data, diagnostic logs, frontend build output, and backend JVM/Scala build artifacts

Why this matters:
- Prevents accidental commits of machine-specific data
- Keeps Git history reviewable
- Avoids breaking teammates' environments with local-only files

### 2. Flight booking page decomposition

Course expectation alignment:
- `lec1`: modular system thinking
- `lec3`: clear input/output and pure logic separation
- `lec6`: page decomposition and reusable frontend components

Changes:
- Extracted flight search defaults, hot routes, display helpers, price insight logic, duration formatting, and quick-date calculations into:
  - `frontend/src/components/flights/flightSearchModel.ts`

Why this matters:
- Keeps `FlightsPanel.tsx` focused on rendering and user interaction
- Makes business/UI helper logic easier to test and reuse
- Reduces the size of a previously overloaded page component

### 3. Workspace overview model extraction

Course expectation alignment:
- `lec1`: architecture and structure over ad-hoc page coding
- `lec3`: pure transformations and declarative data shaping
- `lec6`: page decomposition and maintainable component structure

Changes:
- Extracted overview page data shaping into:
  - `frontend/src/pages/WorkspaceOverviewPage/overviewModel.ts`

Moved logic includes:
- upcoming trip derivation
- recent order summarisation
- community activity mapping
- dashboard shortcuts construction
- hero stat construction
- recommended next-step computation

Why this matters:
- The page component now reads more like orchestration than a large script
- Repeated data shaping rules now live in one place
- Further dashboard changes can happen without re-growing the page file

### 4. Hotel and order page component decomposition

Course expectation alignment:
- `lec1`: decompose a big system into clearer modules
- `lec3`: separate display logic from state orchestration
- `lec6`: page splitting and reusable component structure

Changes:
- Added:
  - `frontend/src/components/hotels/hotelBookingModel.ts`
  - `frontend/src/components/orders/orderViewModel.ts`
  - `frontend/src/components/orders/OrderLineItemDetails.tsx`
  - `frontend/src/components/orders/RefundActionForm.tsx`
- Refactored `HotelsPanel.tsx` and `OrderPanel.tsx` to consume these extracted modules

Why this matters:
- Search defaults and traveler-label formatting no longer live inline in the hotel page
- Order item details and refund form are no longer buried inside one oversized file
- The order page now better matches a route-shell plus subcomponent structure

### 5. Train and attraction page component decomposition

Course expectation alignment:
- `lec1`: break product features into smaller responsibilities
- `lec3`: keep calculations and formatting outside of rendering when practical
- `lec6`: use page decomposition instead of growing one monolithic component per feature

Changes:
- Added:
  - `frontend/src/components/trains/trainBookingModel.ts`
  - `frontend/src/components/attractions/attractionBookingModel.ts`
- Refactored:
  - `frontend/src/components/TrainsPanel.tsx`
  - `frontend/src/components/AttractionsPanel.tsx`

Why this matters:
- Route pricing and station summary logic are no longer embedded directly in the train page
- Attraction session filtering and rule formatting are now reusable helpers instead of inline page code

### 6. Attraction-domain serialization edge tightening

Course expectation alignment:
- `lec1`: preserve clean domain boundaries
- `lec3`: keep pure domain logic readable and reduce mixed responsibilities
- `lec4`: serialization is an infrastructure concern and should trend toward the edge

Changes:
- Added:
  - `backend/src/main/scala/attraction-domain/objects/AttractionRuleJson.scala`
- Refactored:
  - `backend/src/main/scala/attraction-domain/objects/Attraction.scala`
  - `backend/src/main/scala/attraction-domain/utils/AttractionUtils.scala`

Why this matters:
- Circe codec definitions are no longer buried directly inside the main attraction domain object file
- Domain files now depend on a narrower serialization helper instead of carrying all codec boilerplate inline

## Validation

Completed successfully:
- `npm.cmd run build` in `frontend`
- `sbt compile` in `backend`

## Remaining Gaps Still Worth Addressing

### Frontend
- Some pages remain large and should continue to be split into:
  - route shell
  - view model helpers
  - input/form components
  - result/list components
- A few legacy text/content blocks still mix display copy with behavior logic

### Backend
- Continue checking that HTTP/transport concerns stay in `routes/*`
- Prefer keeping domain/application logic isolated from request/response formatting
- More serialization helpers could still be moved out of domain-adjacent files where practical

### Project workflow
- Push/authentication workflow still needs to be stabilised for this machine
- Local PostgreSQL runtime data should remain local-only and never enter Git history

## Suggested Next Remediation Pass

1. Split another large feature page such as community or account-side detail flows
2. Introduce focused tests for extracted frontend helper modules
3. Continue reducing serialization/infrastructure leakage in domain-adjacent Scala files
4. Add a short architecture note describing frontend shell / page / view-model conventions
