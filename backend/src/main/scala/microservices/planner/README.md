# Planner Service

This module packages the smart trip planner domain models and planner application logic as one backend service area.

- `objects`: planner data models.
- `api`: planner behavior and orchestration code.
- `support`: internal helpers for the planner engine only.

This is a backend-only planning engine. It does not have a matching frontend microservice directory, so the code here is intentionally organized around generation, scoring, constraints, and matching rather than a frontend/backed one-to-one planner mapping.
