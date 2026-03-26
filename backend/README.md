# Travel Platform Backend MVP

This backend now exposes a minimal runnable HTTP layer for the travel booking MVP. The current loop is intentionally small and in-memory only, so we can manually demonstrate the core identity, traveler, avatar, flight, hotel, and order flow before adding persistence.

## Current Scope

- `shared-kernel`: typed IDs, value objects, time models, and shared validation errors
- `identity-domain`: `User`, avatar reference, membership, points, repository/service traits
- `traveler-domain`: traveler ownership, default traveler rules, repository/service traits
- `flight-domain`: airlines, flights, cabin inventory, search, and booking availability rules
- `hotel-domain`: hotels, room types, room inventory, stay availability, and booking rules
- `order-domain`: `Order` aggregate root, flight and hotel line items, payment/refund state flow, repository/service traits
- `api-gateway`: DTO mapping, in-memory repositories, application services, HTTP API, wiring, and startup

## Development Ports

- Backend HTTP API: `http://localhost:8080`
- Health check: `GET http://localhost:8080/api/health`
- Frontend dev server: `http://localhost:5173`

The backend enables CORS for local frontend development.

## Run Backend

Use the project-local JDK 21 before running any `sbt` command:

```powershell
$env:JAVA_HOME='E:\typesafe\template\backend\.jdks\temurin-21-unpacked\jdk-21.0.10+7'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
cd E:\typesafe\template\backend
sbt test
sbt run
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

- All repository implementations are in-memory only.
- Uploaded avatars are stored locally under `backend/uploads/avatars` and exposed through `/uploads/avatars/...`.
- Restarting the backend resets all users, travelers, orders, payments, and refunds.
- Cabin availability is checked only when adding a flight item. This MVP does not implement inventory locking or concurrency control yet.
- Room availability is checked day by day for the requested stay only when adding a hotel item. This MVP does not implement inventory locking or concurrency control yet.
- No database, migration, authentication, or external integrations are included in this MVP phase.
