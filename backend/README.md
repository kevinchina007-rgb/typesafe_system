# Travel Platform Backend MVP

This backend now exposes a minimal runnable HTTP layer for the travel booking MVP. The current loop is intentionally small and in-memory only, so we can manually demonstrate the core identity, traveler, and order flow before adding persistence.

## Current Scope

- `shared-kernel`: typed IDs, value objects, time models, and shared validation errors
- `identity-domain`: `User`, membership, points, repository/service traits
- `traveler-domain`: traveler ownership, default traveler rules, repository/service traits
- `order-domain`: `Order` aggregate root, line items, payment/refund state flow, repository/service traits
- `api-gateway`: DTO mapping, in-memory repositories, HTTP API, application wiring, and startup

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
3. Create a user.
4. Add a traveler for that user.
5. Confirm the traveler list and default traveler assignment.
6. Create a simplified order using either the hotel or flight demo item form.
7. Submit the order.
8. Authorize a payment, then capture it.
9. Request a refund, approve it, and settle it.

## MVP API Summary

- `GET /api/health`
- `POST /api/users`
- `GET /api/users/:userId`
- `POST /api/users/:userId/travelers`
- `GET /api/users/:userId/travelers`
- `POST /api/orders`
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
- Restarting the backend resets all users, travelers, orders, payments, and refunds.
- No database, migration, authentication, or external integrations are included in this MVP phase.
