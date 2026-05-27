# Travel Platform Backend MVP

This backend now exposes a minimal runnable HTTP layer for the travel booking MVP. The current loop supports both in-memory repositories and a first database-backed persistence mode for the main booking chain.

## Current Scope

- `shared-kernel`: typed IDs, value objects, time models, and shared validation errors
- `identity-domain`: `User`, avatar reference, membership, points, repository/service traits
- `traveler-domain`: traveler ownership, default traveler rules, repository/service traits
- `flight-domain`: airlines, flights, cabin inventory, search, and booking availability rules
- `hotel-domain`: hotels, room types, room inventory, stay availability, and booking rules
- `train-domain`: railway managers, train journeys, train stops, whole-train seat inventories, adjacent segment fares, and offset-based refund policy rules
- `order-domain`: `Order` aggregate root, flight and hotel line items, payment/refund state flow, repository/service traits
- `persistence-jdbc`: schema init, Doobie repositories, JSON snapshot codecs, H2-backed development persistence
- `api-gateway`: DTO mapping, application services, HTTP API, wiring, and startup

## Development Ports

- Backend HTTP API: `http://localhost:8080`
- Health check: `GET http://localhost:8080/api/health`
- Frontend dev server: `http://localhost:5173`

The backend enables CORS for local frontend development.

## LAN Access

The bundled shortcut now binds the backend and frontend to `0.0.0.0` and opens the app on the machine's local IPv4 address when one is available.

Typical access pattern:

- Frontend: `http://<your-lan-ip>:5173`
- Backend API: `http://<your-lan-ip>:19095`
- Health check: `http://<your-lan-ip>:19095/api/health`

Open these ports on the host machine if the Windows firewall blocks inbound traffic:

- `19095` for the backend API
- `5173` for the frontend static server

## Public Access

This project can also be exposed on a public IP or domain without changing the application structure. The current startup script supports public-facing origins through environment variables.

Typical setup:

- `TRAVEL_PUBLIC_HOST=your-public-host-or-domain`
- `TRAVEL_PUBLIC_SCHEME=http`
- optional `TRAVEL_PUBLIC_FRONTEND_ORIGIN=http://your-public-host-or-domain:5173`
- optional `TRAVEL_PUBLIC_BACKEND_ORIGIN=http://your-public-host-or-domain:19095`

If the frontend and backend are served from different public origins, also set cookie and CORS policy explicitly:

- `TRAVEL_ALLOWED_ORIGINS=http://your-frontend-origin`
- `TRAVEL_SESSION_COOKIE_SAMESITE=None`
- `TRAVEL_SESSION_COOKIE_SECURE=true`
- optional `TRAVEL_SESSION_COOKIE_DOMAIN=your-domain`

For a minimal public exposure, you still need host-level networking outside the app:

- open inbound ports `5173` and `19095`
- if the machine is behind a router, forward those ports to the host
- ensure your public host name resolves to that machine

This is still a plain HTTP setup. It is suitable for testing, but not a hardened internet deployment.

## Repository Modes

- Default mode: `database`
- Optional fallback: `in-memory`

The mode is controlled by `TRAVEL_REPOSITORY_MODE`.

Examples:

```powershell
$env:TRAVEL_REPOSITORY_MODE='database'
```

```powershell
$env:TRAVEL_REPOSITORY_MODE='in-memory'
```

Database mode uses a local H2 file database by default:

```text
backend/data/travel-platform.mv.db
```

You can override the JDBC settings with:

- `TRAVEL_DB_URL`
- `TRAVEL_DB_USER`
- `TRAVEL_DB_PASSWORD`
- `TRAVEL_DB_DRIVER`

## Run Backend

Use the project-local JDK 21 before running any `sbt` command:

```powershell
cd .\backend
$env:JAVA_HOME = '.\.jdks\temurin-21-unpacked\jdk-21.0.10+7'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:TRAVEL_REPOSITORY_MODE='database'
sbt test
sbt run
```

You can also use the bundled PowerShell scripts:

Foreground start:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\backend\scripts\start-backend.ps1 -RepositoryMode database
```

Background start without going through `cmd start` window-title syntax:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\backend\scripts\start-backend-background.ps1 -RepositoryMode database
```

## Run Frontend

```powershell
cd .\frontend
npm install
npm run dev
```

## Manual Demo Flow

1. Open `http://<your-lan-ip>:5173` when using the bundled shortcut, or `http://localhost:5173` on the host machine.
2. Wait for the page to show backend health from `GET /api/health`.
3. Create a user and sign in.
4. Optionally upload a PNG or JPG avatar for that account.
5. Add a traveler for that user.
6. Browse flights with `departureAirport`, `arrivalAirport`, and `date`.
7. Choose travelers and create a flight order directly.
8. Browse hotels with `location`, `checkInDate`, and `checkOutDate`.
9. Choose a room type, room count, and guests to create a hotel order directly.
10. Open the booking page and confirm the order items and reservation holds are visible.
11. Complete payment once.
12. Use manager review to confirm or reject booking items.
13. Restart the backend in `database` mode and verify the same user, travelers, orders, and reservations are still present.
14. Optionally create a railway manager, enter train admin, create a train journey, and add a train ticket item into an order shell.

## MVP API Summary

- `GET /api/health`
- `POST /api/users`
- `POST /api/session/login`
- `GET /api/users/:userId`
- `POST /api/users/:userId/avatar`
- `POST /api/users/:userId/travelers`
- `GET /api/users/:userId/travelers`
- `GET /api/flights`
- `GET /api/flights/:flightId`
- `GET /api/hotels`
- `GET /api/hotels/:hotelId`
- `GET /api/trains`
- `GET /api/trains/:trainId`
- `POST /api/flights/book`
- `POST /api/hotels/book`
- `POST /api/orders`
- `POST /api/orders/:orderId/train-items`
- `GET /api/orders/:orderId`
- `GET /api/users/:userId/orders`
- `POST /api/orders/:orderId/payment-session`
- `POST /api/orders/:orderId/payment-session/confirm`
- `POST /api/orders/:orderId/cancel`
- `POST /api/orders/:orderId/refunds`
- `POST /api/orders/:orderId/refunds/:refundId/approve`
- `POST /api/orders/:orderId/refunds/:refundId/settle`
- `POST /api/manager/session/login`
- `POST /api/manager/booking-items/:orderItemId/confirm`
- `POST /api/manager/booking-items/:orderItemId/reject`
- `POST /api/train-admin/managers`
- `POST /api/train-admin/session/login`
- `GET /api/train-admin/trains`
- `POST /api/train-admin/trains`

## Notes

- Database persistence currently covers:
  - users
  - traveler profiles
  - orders, line items, payments, and refunds
  - airlines, flights, cabin inventories
  - hotels, room types, room inventories
- Database schema now uses lightweight versioned SQL migrations under:
  - `modules/persistence-jdbc/src/main/resources/db/migrations`
- Migration execution keeps a `schema_migrations` table and applies versions in order.
- Failed migrations are not recorded as applied, so migration history stays aligned with the actual schema state.
- Order persistence uses a mixed strategy:
  - root table + detail tables
  - `snapshot_json` for flight and hotel booking snapshots
- Order detail persistence is now more structured for future operational flows:
  - `order_line_items` keeps typed columns such as `flight_id`, `room_type_id`, `cabin_class`, stay dates, room count, `traveler_ids_json`, `unit_amount/unit_currency`, and `booked_amount/booked_currency`
  - `unit_amount/unit_currency` represent per-unit price
  - `booked_amount/booked_currency` represent the total booked amount of the line item
  - `snapshot_json` remains the authoritative immutable history snapshot
- Payments and refunds also keep typed operational columns while preserving the current aggregate model:
  - `order_payments` stores amount, currency, method, status, `created_at`, `captured_at`, and `metadata_json`
  - `order_refunds` stores amount, currency, reason, status, `created_at`, `approved_at`, `settled_at`, and `metadata_json`
- Flight and hotel inventory persistence now includes future-ready placeholders:
  - `version_number`
  - `updated_at`
  These are reserved for later locking/workflow evolution and do not implement optimistic locking yet.
- Uploaded avatars are still stored locally under `backend/uploads/avatars` and exposed through `/uploads/avatars/...`.
- Search aliases remain application-layer rules, not database-driven search infrastructure.
- `SchemaInitializer` is idempotent and safe to run on repeated startup.
- Restart/recoverability is covered by file-backed H2 integration tests, not only same-process in-memory tests.
- In-memory mode is still available as a fallback for comparison and rollback during migration.
- Train Ticket Phase 1 reuses the existing order shell and adds a train item into `Order`; it does not introduce a separate train-only order aggregate.
- Train route prices for non-adjacent stations are derived by summing adjacent `train_segment_prices`; the full route price is treated as derived view data.
- Train refund policy segments are modeled only by offsets relative to departure time.
- Train payments auto-settle into booked semantics after payment success and skip supplier review.
- Train locking currently uses a whole-train seat inventory model (`totalSeats` / `saleableSeats`) rather than strict segment-level inventory.

## Real Inventory Locking Completed Scope

- Flight locking and hotel locking both use persisted `inventory_reservations`.
- Shared reservation fields are:
  - `reservation_id`
  - `resource_type`
  - `resource_id`
  - `order_id`
  - `order_item_id`
  - `quantity`
  - `status`
  - `reserved_at`
  - `expires_at`
  - `confirmed_at`
  - `released_at`
- Hotel reservations additionally store:
  - `check_in_date`
  - `check_out_date`
- Reservation lifecycle is unified as:
  - `Active`
  - `Expired`
  - `Confirmed`
  - `Released`
- State transitions are:
  - booking creates reservation -> `Active`
  - TTL timeout via lazy cleanup -> `Expired`
  - payment success -> `Confirmed`
  - user cancel -> `Released`
  - manager reject of an unconfirmed item -> `Released`
- Reservation linkage is centered on stable `order_item_id`.
- Flight sellable quantity is:
  - `availableSeats - active(unexpired) reservations - confirmed reservations`
- Hotel sellable quantity is evaluated day by day over `[checkInDate, checkOutDate)`:
  - `availableRooms(day) - active(unexpired) overlapping reservations - confirmed overlapping reservations`
  - if any day is short, the whole stay lock fails
- Mixed booking is supported:
  - flight and hotel reservations can coexist under one order
  - each reservation can expire, confirm, or release independently
- one item-side transition should not corrupt the other

## Train Ticket Phase 1 Scope

- Railway managers create and maintain train journeys, but do not review train orders.
- Train journeys include:
  - ordered stops
  - whole-train seat inventories by seat class
  - adjacent segment prices by seat class
  - a sale start time
  - refund policy segments modeled by offsets before departure
- Train items reuse the existing order, payment, refund, persistence, and booking foundations.
- Train route prices are pure derived values:
  - sum adjacent segment prices from `fromStation` to `toStation`
- Train refund amount is computed in the train domain/application layer, and the generic order refund flow only consumes that computed amount.

## Train Locking Limits

- Current train locking only covers whole-train seat inventory by seat class.
- It is not a strict segment-level inventory model between arbitrary stop pairs.
- Strict train segment inventory is explicitly a future enhancement beyond Train Ticket Phase 1.

## Attraction Ticket Phase 1 Scope

- Attraction tickets reuse the existing order shell and add an attraction item into `Order`; there is no separate attraction-only order aggregate.
- Attraction managers configure:
  - attractions
  - ticket types
  - simple eligibility rules
- Eligibility rules are data-driven and stored in the database rather than hard-coded in the domain:
  - age less than
  - age between
  - age at least
  - document type equals
  - document number prefix
- Phase 1 combines multiple eligibility rules on one ticket type with `AND`.
- `useDate` is required across DTOs, application services, snapshots, and persistence.
- The eligibility service evaluates one traveler against one ticket type.
- Multi-traveler validation is aggregated in the attraction booking application service:
  - every traveler must pass
  - one failure rejects the whole attraction item submission
- Attraction ticket snapshots preserve:
  - attraction and ticket type identity
  - unit price
  - `useDate`
  - traveler ids
  - human-readable eligibility rule summaries
  - eligibility validation timestamp
- Attraction tickets do not auto-settle after payment.
- After payment succeeds, attraction items reuse the existing supplier review flow and enter manager review semantics.

## Attraction Limits

- Attraction Phase 1 does not add dedicated attraction inventory locking.
- Attraction admin uses a minimal form-based rule configuration flow, not a full rule editor.
- Eligibility preview endpoints are optional; the authoritative decision remains the server-side check performed when adding an attraction item to an order.

## Future Enhancements Still Not Done

- No distributed locking or multi-instance strong consistency.
- No background scheduler; timeout cleanup is still lazy.
- No optimistic locking or inventory version conflict handling.
- No automatic rollback of already confirmed reservations on manager reject.
- No analytics, reporting, or reservation operations console.
- No commercial-grade high-concurrency guarantees across multiple nodes.
