import type { SearchSuggestionResponse } from '@/lib/mvp-types/index'

import type { SearchTarget } from '@/pages/TourGroupsPage/components/TourGroupPlanComposer.types'

function renderSuggestionList(suggestions: SearchSuggestionResponse[], onPick: (value: string) => void) {
  if (suggestions.length === 0) return null
  return (
    <ul className="grid gap-2 border border-sky-200 bg-white/85 p-3 shadow-sm shadow-sky-100/40">
      {suggestions.map(suggestion => (
        <li key={`${suggestion.resourceType}:${suggestion.value}`}>
          <button
            type="button"
            className="inline-flex items-center justify-center text-sm font-bold text-sky-600 underline-offset-4 hover:underline"
            onClick={() => onPick(suggestion.value)}
          >
            <strong>{suggestion.title}</strong>
          </button>
          <p className="text-sm leading-6 text-slate-500">{suggestion.subtitle}</p>
        </li>
      ))}
    </ul>
  )
}

export function TourGroupPlanComposerSearchForm({
  itemType,
  setItemType,
  date,
  setDate,
  hotelCheckOutDate,
  setHotelCheckOutDate,
  departureLocation,
  setDepartureLocation,
  arrivalLocation,
  setArrivalLocation,
  location,
  setLocation,
  departureLocationSuggestions,
  arrivalLocationSuggestions,
  locationSuggestions,
  onLoadLocationSuggestions,
  onPickDepartureLocation,
  onPickArrivalLocation,
  onPickLocation,
  onSubmitSearch,
  isRouteItem,
  formGridClassName,
  translate,
}: {
  itemType: string
  setItemType: (value: string) => void
  date: string
  setDate: (value: string) => void
  hotelCheckOutDate: string
  setHotelCheckOutDate: (value: string) => void
  departureLocation: string
  setDepartureLocation: (value: string) => void
  arrivalLocation: string
  setArrivalLocation: (value: string) => void
  location: string
  setLocation: (value: string) => void
  departureLocationSuggestions: SearchSuggestionResponse[]
  arrivalLocationSuggestions: SearchSuggestionResponse[]
  locationSuggestions: SearchSuggestionResponse[]
  onLoadLocationSuggestions: (value: string, target: SearchTarget) => void
  onPickDepartureLocation: (value: string) => void
  onPickArrivalLocation: (value: string) => void
  onPickLocation: (value: string) => void
  onSubmitSearch: () => Promise<void>
  isRouteItem: boolean
  formGridClassName: string
  translate: (translationKey: string) => string
}) {
  return (
    <form
      className="grid gap-4 border border-sky-200 bg-white/85 p-5 text-slate-950 shadow-sm shadow-sky-100/40"
      onSubmit={async event => {
        event.preventDefault()
        await onSubmitSearch()
      }}
    >
      <div className={itemType === 'Hotel' ? 'grid gap-4 md:grid-cols-4 hotel' : formGridClassName}>
        <label className="grid gap-2">
          {translate('tourGroups.itemType')}
          <select value={itemType} onChange={event => setItemType(event.target.value)}>
            <option value="Flight">{translate('tourGroups.itemType.flight')}</option>
            <option value="Hotel">{translate('tourGroups.itemType.hotel')}</option>
            <option value="Train">{translate('tourGroups.itemType.train')}</option>
            <option value="Attraction">{translate('tourGroups.itemType.attraction')}</option>
          </select>
        </label>

        <label className="grid gap-2">
          {translate(itemType === 'Hotel' ? 'tourGroups.hotelCheckInDate' : 'tourGroups.search.date')}
          <input type="date" value={date} onChange={event => setDate(event.target.value)} required />
        </label>

        {isRouteItem ? (
          <label className="grid gap-2">
            出发地点
            <input
              value={departureLocation}
              onFocus={() => void onLoadLocationSuggestions(departureLocation, 'departure')}
              onChange={event => {
                const nextValue = event.target.value
                setDepartureLocation(nextValue)
                void onLoadLocationSuggestions(nextValue, 'departure')
              }}
              required
            />
            {renderSuggestionList(departureLocationSuggestions, value => {
              setDepartureLocation(value)
              onPickDepartureLocation(value)
            })}
          </label>
        ) : null}

        {isRouteItem ? (
          <label className="grid gap-2">
            到达地点
            <input
              value={arrivalLocation}
              onFocus={() => void onLoadLocationSuggestions(arrivalLocation, 'arrival')}
              onChange={event => {
                const nextValue = event.target.value
                setArrivalLocation(nextValue)
                void onLoadLocationSuggestions(nextValue, 'arrival')
              }}
              required
            />
            {renderSuggestionList(arrivalLocationSuggestions, value => {
              setArrivalLocation(value)
              onPickArrivalLocation(value)
            })}
          </label>
        ) : null}

        {!isRouteItem ? (
          <label className="grid gap-2">
            {translate('tourGroups.search.locationLabel')}
            <input
              value={location}
              onFocus={() => void onLoadLocationSuggestions(location, 'location')}
              onChange={event => {
                const nextLocation = event.target.value
                setLocation(nextLocation)
                void onLoadLocationSuggestions(nextLocation, 'location')
              }}
              required
            />
            {renderSuggestionList(locationSuggestions, value => {
              setLocation(value)
              onPickLocation(value)
            })}
          </label>
        ) : null}

        {itemType === 'Hotel' ? (
          <label className="grid gap-2">
            {translate('tourGroups.hotelCheckOutDate')}
            <input type="date" value={hotelCheckOutDate} onChange={event => setHotelCheckOutDate(event.target.value)} required />
          </label>
        ) : null}
      </div>
    </form>
  )
}
