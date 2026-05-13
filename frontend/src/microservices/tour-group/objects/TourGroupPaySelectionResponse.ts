import type { TourGroupDetailsResponse } from './TourGroupDetailsResponse'

import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'

export type TourGroupPaySelectionResponse = {
  group: TourGroupDetailsResponse
  orders: OrderResponse[]
}
