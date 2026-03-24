# Step 1. Project Skeleton

## Proposed Module Structure

- `shared-kernel`
  - responsibility: typed ids, shared value objects, shared time models, common domain errors
  - packages:
    - `com.typesafe.travel.shared.kernel`
- `identity-domain`
  - responsibility: `User`, auth/account lifecycle, ownership boundaries for orders and traveler profiles
  - packages:
    - `com.typesafe.travel.identity.domain`
    - future: `.repository`, `.service`, `.dto`, `.codec`, `.persistence`
- `traveler-domain`
  - responsibility: traveler profile aggregate, identity documents, traveler preferences, owner-scoped operations
  - packages:
    - `com.typesafe.travel.traveler.domain`
- `flight-domain`
  - responsibility: airlines, flights, schedules, cabin inventory, sellability constraints
- `hotel-domain`
  - responsibility: hotels, room types, room inventory, stay constraints
- `order-domain`
  - responsibility: `Order` aggregate root, item snapshots, payment/refund coordination
- `content-domain`
  - responsibility: reviews, blog posts, publication/moderation lifecycle
- `tour-group-domain`
  - responsibility: groups, group plans, members, join requests
- `operations-domain`
  - responsibility: staff, audit tasks, support tickets, cross-domain operational workflows
- `api-gateway`
  - responsibility: HTTP endpoints, DTO assembly, effectful wiring, repository implementations

## Design Decisions

- The backend is now an sbt multi-project build so each bounded context can evolve independently.
- Cross-domain references flow through typed IDs and snapshots rather than deep object coupling.
- Only `shared-kernel`, `traveler-domain`, and `order-domain` contain concrete first-slice code; the rest are reserved as explicit extension points.

## Files Created Or Modified

- `build.sbt`
- `README.md`
- `docs/01-foundation-and-first-slice.md`
- `modules/...` project directories

## Open Questions / Future Work

- Should the identity context own authentication only, or also customer account policy like loyalty points and KYC?
- Do you want repository implementations based on Doobie, Skunk, or plain JDBC in a later slice?
- Should API transport be http4s JSON first, or GraphQL/BFF style for the frontend?

# Step 2. Domain Model Proposal

## Aggregates

- `User` aggregate in `identity-domain`
- `TravelerProfile` aggregate in `traveler-domain`
- `Flight` aggregate with `CabinInventory` child entities in `flight-domain`
- `Hotel` aggregate with `RoomType` and `RoomInventory` in `hotel-domain`
- `Order` aggregate root in `order-domain`
- `Review` aggregate and `BlogPost` aggregate in `content-domain`
- `TourGroup` aggregate root in `tour-group-domain`
- `AuditTask` and `SupportTicket` aggregates in `operations-domain`

## Entities

- identity: `User`
- traveler: `TravelerProfile`, `IdentityDocument`, `EmergencyContact`, `TravelerLoyaltyMembership`
- flight: `Airline`, `Flight`, `CabinInventory`
- hotel: `Hotel`, `RoomType`, `RoomInventory`
- order: `Order`, `FlightOrderItem`, `HotelOrderItem`, `Payment`, `Refund`
- content: `Review`, `BlogPost`
- group: `TourGroup`, `TourGroupMember`, `TourGroupPlanItem`, `GroupJoinRequest`
- operations: `Staff`, `AuditTask`, `SupportTicket`

## Shared Value Objects

- ids: `UserId`, `TravelerId`, `AirlineId`, `FlightId`, `HotelId`, `RoomTypeId`, `OrderId`, `OrderItemId`, `PaymentId`, `RefundId`, `ReviewId`, `BlogId`, `TourGroupId`, `StaffId`, `AuditTaskId`, `SupportTicketId`
- money and quantities: `Money`, `Currency`, `Rating`, `Points`, `Capacity`, `SeatCount`, `RoomCount`
- time: `FlightSchedule`, `StayPeriod`, `TravelPeriod`
- reusable small types:
  - future candidates: `EmailAddress`, `PhoneNumber`, `CabinCode`, `AirportCode`, `LocationCode`

## Enums / ADTs

- `UserStatus`
- `TravelerProfileStatus`
- `FlightStatus`
- `RoomTypeStatus`
- `OrderStatus`
- `OrderItemStatus`
- `PaymentStatus`
- `RefundStatus`
- `ReviewStatus`
- `BlogStatus`
- `TourGroupStatus`
- `JoinRequestStatus`
- `AuditStatus`
- `TicketStatus`

## Key Invariants

- a `TravelerProfile` belongs to exactly one `User`
- a verified traveler profile must have at least one non-expired identity document
- an `Order` belongs to exactly one user and keeps immutable snapshots for booked travel products
- order item currency must match the parent order currency
- an order cannot move to payment workflow without at least one item
- review creation should require a completed order or completed order item reference
- a join request targets exactly one group and one user-traveler pair
- support tickets reference one order and are handled by zero or one current staff assignee
- audit tasks target one domain object through a typed target ADT, not loose strings

## Design Decisions

- Aggregate roots are chosen around consistency boundaries rather than tables.
- State transitions live on domain objects as pure methods returning typed errors.
- Snapshots are part of the order model to preserve history even if supply-side records change later.

## Files Created Or Modified

- `modules/shared-kernel/src/main/scala/com/typesafe/travel/shared/kernel/*`
- `modules/traveler-domain/src/main/scala/com/typesafe/travel/traveler/domain/*`
- `modules/order-domain/src/main/scala/com/typesafe/travel/order/domain/*`

## Open Questions / Future Work

- Should traveler verification require manual audit approval or support automatic rules first?
- Do hotel order items need per-night pricing breakdowns in the first release?
- Should refunds be item-scoped, payment-scoped, or both?

# Step 3. Database Proposal

## Main Tables

- identity:
  - `users`
- traveler:
  - `traveler_profiles`
  - `traveler_identity_documents`
  - `traveler_loyalty_memberships`
- flight:
  - `airlines`
  - `flights`
  - `cabin_inventory`
- hotel:
  - `hotels`
  - `room_types`
  - `room_inventory`
- order:
  - `orders`
  - `order_flight_items`
  - `order_hotel_items`
  - `payments`
  - `refunds`
- content:
  - `reviews`
  - `blog_posts`
  - `blog_post_mentions`
- group:
  - `tour_groups`
  - `tour_group_members`
  - `tour_group_plan_items`
  - `group_join_requests`
- operations:
  - `staff`
  - `audit_tasks`
  - `support_tickets`

## Important Foreign Keys

- `traveler_profiles.owner_user_id -> users.id`
- `traveler_identity_documents.traveler_id -> traveler_profiles.id`
- `flights.airline_id -> airlines.id`
- `cabin_inventory.flight_id -> flights.id`
- `room_types.hotel_id -> hotels.id`
- `room_inventory.room_type_id -> room_types.id`
- `orders.user_id -> users.id`
- `order_flight_items.order_id -> orders.id`
- `order_flight_items.flight_id -> flights.id`
- `order_flight_items.traveler_id -> traveler_profiles.id`
- `order_hotel_items.order_id -> orders.id`
- `order_hotel_items.hotel_id -> hotels.id`
- `order_hotel_items.room_type_id -> room_types.id`
- `payments.order_id -> orders.id`
- `refunds.order_id -> orders.id`
- `reviews.order_id -> orders.id`
- `reviews.order_item_id -> order item record when present`
- `blog_post_mentions.blog_id -> blog_posts.id`
- `tour_groups.owner_user_id -> users.id`
- `tour_group_members.group_id -> tour_groups.id`
- `tour_group_members.user_id -> users.id`
- `tour_group_members.traveler_id -> traveler_profiles.id`
- `group_join_requests.group_id -> tour_groups.id`
- `support_tickets.order_id -> orders.id`
- `support_tickets.assigned_staff_id -> staff.id`

## Snapshot Strategy

- `order_flight_items` should store flight number, airline name, departure/arrival airports, schedule snapshot, booked cabin, booked price snapshot, and traveler snapshot columns or JSON payload.
- `order_hotel_items` should store hotel name, room type name, stay period, guest summary, nightly pricing snapshot, and booked price snapshot.
- `payments` and `refunds` should keep gateway/provider references and status history metadata.

## Design Decisions

- Core entity tables use strong foreign keys for present-time relationships.
- Order item snapshot columns preserve business history and enable reviews even after catalog updates.
- Join tables are used only where the relationship is naturally many-to-many or membership-oriented.

## Files Created Or Modified

- `docs/01-foundation-and-first-slice.md`

## Open Questions / Future Work

- Should snapshots be decomposed into columns for reporting, JSON for flexibility, or a hybrid?
- Do you want temporal inventory history tables for flights and hotels later?

# Step 4. Frontend State Model Proposal

## Shared Principles

- keep a small set of canonical state atoms per page or domain store
- compute filters, badges, totals, and CTA availability as derived selectors
- keep network status explicit with request-state ADTs
- keep mutation flows modeled as typed commands and optimistic state only where safe

## Page State Sketches

### Flight Search

- core state:
  - `query`
  - `resultPage`
  - `selectedFlightId`
  - `selectedTravelerIds`
  - `requestState`
- derived state:
  - valid query flag
  - grouped itineraries
  - remaining seat availability labels
  - price summary for selected travelers

### Hotel Search

- core state:
  - `searchCriteria`
  - `resultPage`
  - `selectedRoomTypeId`
  - `draftGuests`
  - `requestState`
- derived state:
  - total nights
  - occupancy fit warnings
  - cheapest qualifying room per hotel
  - cancellation policy badge

### Order Detail

- core state:
  - `order`
  - `payments`
  - `refunds`
  - `selectedAction`
  - `requestState`
- derived state:
  - payable balance
  - refundable balance
  - state-machine allowed actions
  - review eligibility per item

### Tour Group Hall / My Tour Groups

- core state:
  - `groupFilters`
  - `groups`
  - `myMemberships`
  - `pendingJoinRequests`
  - `requestState`
- derived state:
  - joinable groups
  - owned groups
  - membership role badges
  - plan timeline projection

### Back Office Audit

- core state:
  - `queue`
  - `selectedTaskId`
  - `draftDecision`
  - `staffIdentity`
  - `requestState`
- derived state:
  - SLA breach highlight
  - target summary card
  - available decisions by audit status and target type

### Support Ticket Page

- core state:
  - `ticketList`
  - `selectedTicketId`
  - `draftReply`
  - `requestState`
- derived state:
  - escalation candidates
  - unresolved count
  - related order summary

## Design Decisions

- The frontend should expose a small number of domain-specific stores, not one giant global mutable store.
- Store modules should own side-effect orchestration only; pure selectors and transition helpers should live in separate domain-state files.
- UI components should render from explicit state plus derived selectors rather than re-encoding business rules.

## Files Created Or Modified

- `docs/01-foundation-and-first-slice.md`

## Open Questions / Future Work

- Should the frontend be split by bounded-context routes and stores from day one?
- Do you want route loaders/server-state separation, or a more client-store-centric approach first?

# Step 5. First Slice Implemented

## Included In This Slice

- shared typed ids, money, quantity, rating, and time models
- traveler profile aggregate with explicit verification and archival transitions
- order aggregate skeleton with typed items, payments, refunds, and transition rules
- repository traits and service traits with effect-polymorphic implementations
- initial unit tests for traveler and order domain behavior

## Design Decisions

- Services stay thin and orchestrate repositories plus pure aggregate behavior.
- Errors are domain-specific ADTs extending a shared `DomainError`.
- `Order` owns financial and lifecycle consistency; child records are not allowed to mutate status independently.

## Files Created Or Modified

- `modules/shared-kernel/src/main/scala/com/typesafe/travel/shared/kernel/Identifiers.scala`
- `modules/shared-kernel/src/main/scala/com/typesafe/travel/shared/kernel/Values.scala`
- `modules/shared-kernel/src/main/scala/com/typesafe/travel/shared/kernel/TimeModels.scala`
- `modules/shared-kernel/src/main/scala/com/typesafe/travel/shared/kernel/DomainError.scala`
- `modules/traveler-domain/src/main/scala/com/typesafe/travel/traveler/domain/TravelerProfile.scala`
- `modules/traveler-domain/src/main/scala/com/typesafe/travel/traveler/domain/TravelerProfileRepository.scala`
- `modules/traveler-domain/src/main/scala/com/typesafe/travel/traveler/domain/TravelerProfileService.scala`
- `modules/order-domain/src/main/scala/com/typesafe/travel/order/domain/Order.scala`
- `modules/order-domain/src/main/scala/com/typesafe/travel/order/domain/OrderRepository.scala`
- `modules/order-domain/src/main/scala/com/typesafe/travel/order/domain/OrderService.scala`
- `modules/traveler-domain/src/test/scala/com/typesafe/travel/traveler/domain/TravelerProfileSpec.scala`
- `modules/order-domain/src/test/scala/com/typesafe/travel/order/domain/OrderSpec.scala`

## Open Questions / Future Work

- Add repository implementations and codecs in a persistence slice.
- Introduce inventory reservation policies before confirming payments.
- Add review eligibility and snapshot mappers once content-domain and persistence are wired.
