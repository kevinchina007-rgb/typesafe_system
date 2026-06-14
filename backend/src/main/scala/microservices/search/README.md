# Search Module

This backend module is a shared search foundation, not a planner-style microservice.

## Responsibilities

- `SearchModels.scala`: resource types and search result models.
- `SearchRanking.scala`: keyword normalization and ranking helpers.
- `TravelSearchAliases.scala`: alias matching and search expansion helpers.

## Notes

- This module is backend-only.
- It is intentionally organized as an internal search foundation rather than a planner domain.
- There is no one-to-one frontend microservice counterpart for this module.
