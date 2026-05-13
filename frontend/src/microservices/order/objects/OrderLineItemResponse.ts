import type { FlightItemDetailsResponse } from './FlightItemDetailsResponse'
import type { HotelItemDetailsResponse } from './HotelItemDetailsResponse'
import type { TrainItemDetailsResponse } from './TrainItemDetailsResponse'
import type { AttractionItemDetailsResponse } from './AttractionItemDetailsResponse'
import type { SupplierReviewDecisionResponse } from './SupplierReviewDecisionResponse'

export type OrderLineItemResponse = {
  orderItemId: string
  orderItemKind: string
  orderItemStatus: string
  supplierReviewStatus: string
  supplierReviewDecision: SupplierReviewDecisionResponse | null
  bookedAmount: string
  bookedCurrency: string
  summaryLabel: string
  flightDetails: FlightItemDetailsResponse | null
  hotelDetails: HotelItemDetailsResponse | null
  trainDetails: TrainItemDetailsResponse | null
  attractionDetails: AttractionItemDetailsResponse | null
}
