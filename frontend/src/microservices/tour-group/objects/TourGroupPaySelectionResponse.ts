import type { TourGroupDetailsResponse } from './TourGroupDetailsResponse'

import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'

export type TourGroupPaySelectionResponse = {
  group: TourGroupDetailsResponse
  orders: OrderResponse[]
}
export const tourGroupPaySelectionResponseFromJson = (json: string): TourGroupPaySelectionResponse =>
  JSON.parse(json) as TourGroupPaySelectionResponse

export const tourGroupPaySelectionResponseToJson = (value: TourGroupPaySelectionResponse): string =>
  JSON.stringify(value)
