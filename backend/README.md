# Travel Platform Backend MVP

This backend now exposes a minimal runnable HTTP layer for the travel booking MVP. The current loop supports both in-memory repositories and a first database-backed persistence mode for the main booking chain.

## Current Scope

- `shared-kernel`: typed IDs, value objects, time models, and shared validation errors
- `identity-domain`: `User`, avatar reference, membership, points, repository/service traits
- `traveler-domain`: traveler ownership, default traveler rules, repository/service traits
- `flight-domain`: airlines, flights, cabin inventory, search, and booking availability rules
- `hotel-domain`: hotels, room types, room inventory, stay availability, and booking rules
- `order-domain`: `Order` aggregate root, flight and hotel line items, payment/refund state flow, repository/service traits
- `persistence-jdbc`: schema init, Doobie repositories, JSON snapshot codecs, H2-backed development persistence
- `api-gateway`: DTO mapping, application services, HTTP API, wiring, and startup

## Development Ports

- Backend HTTP API: `http://localhost:8080`
- Health check: `GET http://localhost:8080/api/health`
- Frontend dev server: `http://localhost:5173`

The backend enables CORS for local frontend development.

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
$env:JAVA_HOME='E:\typesafe\template\backend\.jdks\temurin-21-unpacked\jdk-21.0.10+7'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:TRAVEL_REPOSITORY_MODE='database'
cd E:\typesafe\template\backend
sbt test
sbt run
```

You can also use the bundled PowerShell scripts:

Foreground start:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File E:\typesafe\template\backend\scripts\start-backend.ps1 -RepositoryMode database
```

Background start without going through `cmd start` window-title syntax:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File E:\typesafe\template\backend\scripts\start-backend-background.ps1 -RepositoryMode database
```

## Run Frontend

```powershell
cd E:\typesafe\template\frontend
npm install
npm run dev
```

## Manual Demo Flow

1. Open `http://localhost:5173`.
2. Wait for the page to show backend health from `GET /api/health`.
3. Create a user and sign in.
4. Optionally upload a PNG or JPG avatar for that account.
5. Add a traveler for that user.
6. Create an empty booking shell.
7. Browse flights with `departureAirport`, `arrivalAirport`, and `date`.
8. Choose a cabin and add the flight to the booking.
9. Browse hotels with `location`, `checkInDate`, and `checkOutDate`.
10. Choose a room type and add the hotel stay to the booking.
11. Open the booking page and confirm the snapshots are now part of the order.
12. Submit the order.
13. Authorize a payment, then capture it.
14. Request a refund, approve it, and settle it.
15. Restart the backend in `database` mode and verify the same user, travelers, and booking are still present.

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
- `POST /api/orders`
- `POST /api/orders/:orderId/flight-items`
- `POST /api/orders/:orderId/hotel-items`
- `GET /api/orders/:orderId`
- `POST /api/orders/:orderId/submit`
- `POST /api/orders/:orderId/payments`
- `POST /api/orders/:orderId/payments/:paymentId/capture`
- `POST /api/orders/:orderId/cancel`
- `POST /api/orders/:orderId/refunds`
- `POST /api/orders/:orderId/refunds/:refundId/approve`
- `POST /api/orders/:orderId/refunds/:refundId/settle`

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
- Cabin availability is checked only when adding a flight item. This MVP does not implement inventory locking or concurrency control yet.
- Room availability is checked day by day for the requested stay only when adding a hotel item. This MVP does not implement inventory locking or concurrency control yet.
- In-memory mode is still available as a fallback for comparison and rollback during migration.
