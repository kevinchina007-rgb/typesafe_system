# Travel Platform Backend

This backend is organized as a Scala 3 multi-project build for a type-safe travel booking and group-tour platform.

## Modules

- `shared-kernel`: typed identifiers, shared value objects, time models, and cross-domain errors.
- `identity-domain`: placeholder bounded context for future `User` and identity lifecycle logic.
- `traveler-domain`: traveler profiles, profile status transitions, repository ports, and services.
- `flight-domain`: placeholder bounded context for airlines, flights, and cabin inventory.
- `hotel-domain`: placeholder bounded context for hotels, room types, and room inventory.
- `order-domain`: order aggregate skeleton, order items, payment/refund transitions, repository ports, and services.
- `content-domain`: placeholder bounded context for reviews and blog posts.
- `tour-group-domain`: placeholder bounded context for groups, membership, and join requests.
- `operations-domain`: placeholder bounded context for audit and support operations.
- `api-gateway`: future integration layer for HTTP, persistence wiring, and orchestration.

## Current Scope

This first slice implements:

- shared typed building blocks
- traveler domain
- initial order domain skeleton
- test skeletons for pure domain behavior
- design documentation for architecture, database, and frontend state

## Run Tests

```bash
sbt test
```
