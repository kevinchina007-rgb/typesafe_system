import { useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AttractionResponse, FlightPlannerResponse, HotelPlannerResponse, SearchSuggestionResponse, TrainPlannerResponse } from '@/lib/mvp-types/index'

import { TourGroupPlanComposerActions } from '@/pages/TourGroupsPage/components/TourGroupPlanComposerActions'
import type { TourGroupPlanComposerProps, SearchTarget } from '@/pages/TourGroupsPage/components/TourGroupPlanComposer.types'
import { TourGroupFlightResults, TourGroupHotelResults, TourGroupTrainResults, TourGroupAttractionResults } from '@/pages/TourGroupsPage/components/TourGroupPlanComposerResultCards'
import { TourGroupPlanComposerSearchForm } from '@/pages/TourGroupsPage/components/TourGroupPlanComposerSearchForm'
import { TourGroupPlanComposerValidation } from '@/pages/TourGroupsPage/components/TourGroupPlanComposerValidation'
import { nextDay } from '@/pages/TourGroupsPage/components/TourGroupPlanComposer.utils'

export function TourGroupPlanComposer({
  currentLanguage,
  isBusy,
  existingPlanItems,
  translate,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
  onCreatePlanItem,
  onCreateOptionForPlanItem,
}: TourGroupPlanComposerProps) {
  const [itemType, setItemType] = useState('Flight')
  const [date, setDate] = useState('')
  const [hotelCheckOutDate, setHotelCheckOutDate] = useState('')
  const [departureLocation, setDepartureLocation] = useState('')
  const [arrivalLocation, setArrivalLocation] = useState('')
  const [location, setLocation] = useState('')
  const [searchMessage, setSearchMessage] = useState('')
  const [departureLocationSuggestions, setDepartureLocationSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [arrivalLocationSuggestions, setArrivalLocationSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [locationSuggestions, setLocationSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [flightResults, setFlightResults] = useState<FlightPlannerResponse[]>([])
  const [hotelResults, setHotelResults] = useState<HotelPlannerResponse[]>([])
  const [trainResults, setTrainResults] = useState<TrainPlannerResponse[]>([])
  const [attractionResults, setAttractionResults] = useState<AttractionResponse[]>([])

  const nextSequenceNo = useMemo(
    () => Math.max(0, ...existingPlanItems.map(planItem => planItem.sequenceNo)) + 1,
    [existingPlanItems],
  )

  const isRouteItem = itemType === 'Flight' || itemType === 'Train'
  const formGridClassName = itemType === 'Attraction' ? 'grid gap-4 md:grid-cols-3' : 'grid gap-4 md:grid-cols-4'

  async function loadLocationSuggestions(nextLocation: string, target: SearchTarget) {
    const normalizedLocation = nextLocation.trim()
    if (normalizedLocation.length < 2) {
      if (target === 'departure') setDepartureLocationSuggestions([])
      if (target === 'arrival') setArrivalLocationSuggestions([])
      if (target === 'location') setLocationSuggestions([])
      return
    }

    try {
      const response = await travelMvpApiClient.listExploreSuggestions({ q: normalizedLocation })
      const allowedTypes =
        itemType === 'Flight'
          ? new Set(['flight'])
          : itemType === 'Hotel'
            ? new Set(['hotel'])
            : itemType === 'Train'
              ? new Set(['train'])
              : new Set(['attraction'])

      const nextSuggestions = response.suggestions.filter(suggestion => allowedTypes.has(suggestion.resourceType)).slice(0, 6)
      if (target === 'departure') setDepartureLocationSuggestions(nextSuggestions)
      if (target === 'arrival') setArrivalLocationSuggestions(nextSuggestions)
      if (target === 'location') setLocationSuggestions(nextSuggestions)
    } catch {
      if (target === 'departure') setDepartureLocationSuggestions([])
      if (target === 'arrival') setArrivalLocationSuggestions([])
      if (target === 'location') setLocationSuggestions([])
    }
  }

  async function runSearch() {
    setSearchMessage('')
    setFlightResults([])
    setHotelResults([])
    setTrainResults([])
    setAttractionResults([])
    setDepartureLocationSuggestions([])
    setArrivalLocationSuggestions([])
    setLocationSuggestions([])

    if (!date) {
      setSearchMessage(translate('tourGroups.searchInputHint'))
      return
    }

    if (itemType === 'Flight') {
      const departure = departureLocation.trim()
      const arrival = arrivalLocation.trim()
      if (!departure || !arrival) {
        setSearchMessage('请先填写出发地点和到达地点。')
        return
      }
      setFlightResults(await onSearchFlights({ departureAirport: departure, arrivalAirport: arrival, date }))
      return
    }

    if (itemType === 'Hotel') {
      if (!location.trim()) {
        setSearchMessage(translate('tourGroups.searchInputHint'))
        return
      }
      const effectiveCheckOutDate = hotelCheckOutDate || nextDay(date)
      if (effectiveCheckOutDate <= date) {
        setSearchMessage(translate('tourGroups.hotelDateRangeHint'))
        return
      }
      setHotelResults(await onSearchHotels({ location, checkInDate: date, checkOutDate: effectiveCheckOutDate }))
      return
    }

    if (itemType === 'Train') {
      const departure = departureLocation.trim()
      const arrival = arrivalLocation.trim()
      if (!departure || !arrival) {
        setSearchMessage('请先填写出发地点和到达地点。')
        return
      }
      setTrainResults(await onSearchTrains({ fromStation: departure, toStation: arrival, date }))
      return
    }

    if (!location.trim()) {
      setSearchMessage(translate('tourGroups.searchInputHint'))
      return
    }
    setAttractionResults(await onSearchAttractions({ city: location }))
  }

  async function createPlanWithOption(
    planPayload: {
      itemType: string
      title: string
      description: string
      scheduledAt: string
      endsAt?: string | null
    },
    optionPayload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) {
    setSearchMessage('')
    const createdPlanItem = await onCreatePlanItem({
      ...planPayload,
      sequenceNo: nextSequenceNo,
    })
    if (!createdPlanItem) {
      setSearchMessage(translate('tourGroups.createPlanFailedHint'))
      return
    }
    await onCreateOptionForPlanItem(createdPlanItem.planItemId, optionPayload)
    setSearchMessage(translate('tourGroups.createPlanSuccessHint'))
  }

  return (
    <section className="grid gap-3 border border-sky-200 bg-white/85 p-4 text-slate-950 shadow-sm shadow-sky-100/40">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('tourGroups.createPlanEyebrow')}</p>
          <h3>{translate('tourGroups.createPlanSearchTitle')}</h3>
          <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.createPlanSearchHint')}</p>
        </div>
      </div>

      <TourGroupPlanComposerSearchForm
        itemType={itemType}
        setItemType={setItemType}
        date={date}
        setDate={setDate}
        hotelCheckOutDate={hotelCheckOutDate}
        setHotelCheckOutDate={setHotelCheckOutDate}
        departureLocation={departureLocation}
        setDepartureLocation={setDepartureLocation}
        arrivalLocation={arrivalLocation}
        setArrivalLocation={setArrivalLocation}
        location={location}
        setLocation={setLocation}
        departureLocationSuggestions={departureLocationSuggestions}
        arrivalLocationSuggestions={arrivalLocationSuggestions}
        locationSuggestions={locationSuggestions}
        onLoadLocationSuggestions={(value, target) => void loadLocationSuggestions(value, target)}
        onPickDepartureLocation={() => setDepartureLocationSuggestions([])}
        onPickArrivalLocation={() => setArrivalLocationSuggestions([])}
        onPickLocation={() => setLocationSuggestions([])}
        onSubmitSearch={runSearch}
        isRouteItem={isRouteItem}
        formGridClassName={formGridClassName}
        translate={translate}
      />

      <TourGroupPlanComposerActions isBusy={isBusy} translate={translate} />
      <TourGroupPlanComposerValidation message={searchMessage} />

      <TourGroupFlightResults
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        flightResults={flightResults}
        onCreatePlanWithOption={createPlanWithOption}
      />

      <TourGroupHotelResults
        isBusy={isBusy}
        hotelResults={hotelResults}
        date={date}
        hotelCheckOutDate={hotelCheckOutDate}
        onCreatePlanWithOption={createPlanWithOption}
      />

      <TourGroupTrainResults
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        trainResults={trainResults}
        departureLocation={departureLocation}
        arrivalLocation={arrivalLocation}
        date={date}
        onCreatePlanWithOption={createPlanWithOption}
      />

      <TourGroupAttractionResults
        isBusy={isBusy}
        attractionResults={attractionResults}
        date={date}
        onCreatePlanWithOption={createPlanWithOption}
      />
    </section>
  )
}
