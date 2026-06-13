import type {
  AppLanguage,
  AttractionResponse,
  FlightPlannerResponse,
  GroupPlanItemResponse,
  HotelPlannerResponse,
  SearchSuggestionResponse,
  TrainResponse,
} from '@/lib/mvp-types/index'

export type TourGroupPlanComposerProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  existingPlanItems: GroupPlanItemResponse[]
  translate: (translationKey: string) => string
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightPlannerResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelPlannerResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
  onCreatePlanItem: (payload: {
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<GroupPlanItemResponse | null>
  onCreateOptionForPlanItem: (
    planItemId: string,
    payload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) => Promise<void>
}

export type SearchTarget = 'departure' | 'arrival' | 'location'

export type TourGroupPlanComposerSearchState = {
  itemType: string
  date: string
  hotelCheckOutDate: string
  departureLocation: string
  arrivalLocation: string
  location: string
  searchMessage: string
  departureLocationSuggestions: SearchSuggestionResponse[]
  arrivalLocationSuggestions: SearchSuggestionResponse[]
  locationSuggestions: SearchSuggestionResponse[]
  flightResults: FlightPlannerResponse[]
  hotelResults: HotelPlannerResponse[]
  trainResults: TrainResponse[]
  attractionResults: AttractionResponse[]
}
