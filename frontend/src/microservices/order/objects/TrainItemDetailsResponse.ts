import type { TrainSeatAssignmentResponse } from '@/microservices/train/objects/TrainSeatAssignmentResponse'

export type TrainItemDetailsResponse = {
  trainId: string
  trainNumber: string
  fromStationCode: string
  fromStationName: string
  toStationCode: string
  toStationName: string
  departureTime: string
  arrivalTime: string
  seatClass: string
  requestedSeatPreference: string | null
  seatAssignments: TrainSeatAssignmentResponse[]
  travelerIds: string[]
  reservationStatus: string | null
  reservationExpiresAt: string | null
  unitPrice: string
  totalPrice: string
  currency: string
}
