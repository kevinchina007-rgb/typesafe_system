import type { TourGroupSummaryResponse } from './TourGroupSummaryResponse'
import type { TourGroupMembershipResponse } from './TourGroupMembershipResponse'
import type { TourGroupMembershipTravelerResponse } from './TourGroupMembershipTravelerResponse'
import type { GroupPlanItemResponse } from './GroupPlanItemResponse'
import type { GroupPlanOptionResponse } from './GroupPlanOptionResponse'
import type { GroupPlanSelectionResponse } from './GroupPlanSelectionResponse'
import type { GroupSelectionOrderLinkResponse } from './GroupSelectionOrderLinkResponse'
import type { GroupSelectionOrderProjectionResponse } from './GroupSelectionOrderProjectionResponse'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'

export type TourGroupDetailsResponse = {
  group: TourGroupSummaryResponse
  memberships: TourGroupMembershipResponse[]
  membershipTravelers: TourGroupMembershipTravelerResponse[]
  planItems: GroupPlanItemResponse[]
  planOptions: GroupPlanOptionResponse[]
  selections: GroupPlanSelectionResponse[]
  selectionOrderLinks: GroupSelectionOrderLinkResponse[]
  selectionOrderProjections: GroupSelectionOrderProjectionResponse[]
  bookings: OrderResponse[]
}
export const tourGroupDetailsResponseFromJson = (json: string): TourGroupDetailsResponse =>
  JSON.parse(json) as TourGroupDetailsResponse

export const tourGroupDetailsResponseToJson = (value: TourGroupDetailsResponse): string =>
  JSON.stringify(value)
