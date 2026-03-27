import { useState } from 'react'

import type { AppLanguage, HotelResponse, TravelerResponse } from '../lib/mvp-types'
import { formatIsoDateTime, localizeBedType, mapBackendStatusToProductLabel } from '../lib/view-models'

type HotelsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onSearchHotels: (payload: {
    location?: string
    checkInDate?: string
    checkOutDate?: string
  }) => Promise<HotelResponse[]>
  onBookHotel: (payload: {
    roomTypeId: string
    guestTravelerIds: string[]
    checkInDate: string
    checkOutDate: string
    roomCount: number
  }) => Promise<void>
}

function renderTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function HotelsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onSearchHotels,
  onBookHotel,
}: HotelsPanelProps) {
  const [hotelResponses, setHotelResponses] = useState<HotelResponse[]>([])
  const [hasSearchedHotels, setHasSearchedHotels] = useState(false)
  const [searchCheckInDate, setSearchCheckInDate] = useState('2026-04-05')
  const [searchCheckOutDate, setSearchCheckOutDate] = useState('2026-04-07')

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.hotels')}</p>
          <h2>{translate('hotels.title')}</h2>
        </div>
      </div>

      <p className="hero-copy">{translate('hotels.description')}</p>
      <p className="empty-state">{translate('hotels.searchHint')}</p>

      <form
        className="stack-form panel-card"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const nextCheckInDate = String(formData.get('checkInDate') ?? '').trim()
          const nextCheckOutDate = String(formData.get('checkOutDate') ?? '').trim()
          setSearchCheckInDate(nextCheckInDate)
          setSearchCheckOutDate(nextCheckOutDate)
          const nextHotelResponses = await onSearchHotels({
            location: String(formData.get('location') ?? '').trim() || undefined,
            checkInDate: nextCheckInDate || undefined,
            checkOutDate: nextCheckOutDate || undefined,
          })
          setHasSearchedHotels(true)
          setHotelResponses(nextHotelResponses)
        }}
      >
        <div className="three-column-grid">
          <label>
            {translate('hotels.location')}
            <input name="location" placeholder="Hangzhou / West Lake / 杭州 / 外滩" />
          </label>
          <label>
            {translate('hotels.checkInDate')}
            <input name="checkInDate" type="date" defaultValue={searchCheckInDate} />
          </label>
          <label>
            {translate('hotels.checkOutDate')}
            <input name="checkOutDate" type="date" defaultValue={searchCheckOutDate} />
          </label>
        </div>

        <button type="submit" disabled={isBusy}>
          {translate('hotels.search')}
        </button>
      </form>

      {isGuestMode ? <p className="empty-state">{translate('hotels.guest')}</p> : null}

      {hasSearchedHotels ? (
        <div className="entity-list flights-list">
          {hotelResponses.length > 0 ? (
            hotelResponses.map(hotelResponse => (
              <article key={hotelResponse.hotelId} className="panel-card hotel-card">
                <div className="panel-heading">
                  <div>
                    <strong>{hotelResponse.hotelName}</strong>
                    <p>{hotelResponse.location}</p>
                  </div>
                  <span className="tag-chip">{mapBackendStatusToProductLabel(hotelResponse.status, currentLanguage)}</span>
                </div>

                <div className="detail-grid">
                  <div>
                    <span className="detail-label">{translate('hotels.createdAt')}</span>
                    <strong>{formatIsoDateTime(hotelResponse.createdAt, '-')}</strong>
                  </div>
                  <div>
                    <span className="detail-label">{translate('hotels.status')}</span>
                    <strong>{mapBackendStatusToProductLabel(hotelResponse.status, currentLanguage)}</strong>
                  </div>
                </div>

                <ul className="entity-list">
                  {hotelResponse.roomTypes.map(roomTypeResponse => (
                    <li key={roomTypeResponse.roomTypeId}>
                      <div>
                        <strong>{roomTypeResponse.roomTypeName}</strong>
                        <p>
                          {`${translate('hotels.bedType')}: ${localizeBedType(roomTypeResponse.bedType, currentLanguage)} | ${translate('hotels.capacity')}: ${roomTypeResponse.capacity}`}
                        </p>
                        <p>{`${translate('hotels.priceFrom')}: ${roomTypeResponse.basePrice} ${roomTypeResponse.currency}`}</p>
                        {roomTypeResponse.availableRoomsForRequestedStay !== null ? (
                          <p>{`${translate('hotels.availableRooms')}: ${roomTypeResponse.availableRoomsForRequestedStay}`}</p>
                        ) : null}
                      </div>
                      <form
                        className="compact-action-block"
                        onSubmit={async event => {
                          event.preventDefault()
                          const formData = new FormData(event.currentTarget)
                          const selectedGuestTravelerIds = formData
                            .getAll('guestTravelerIds')
                            .map(value => String(value))
                            .filter(Boolean)
                          await onBookHotel({
                            roomTypeId: roomTypeResponse.roomTypeId,
                            guestTravelerIds: selectedGuestTravelerIds,
                            checkInDate: String(formData.get('checkInDate') ?? searchCheckInDate),
                            checkOutDate: String(formData.get('checkOutDate') ?? searchCheckOutDate),
                            roomCount: Number(formData.get('roomCount') ?? 1),
                          })
                        }}
                      >
                        <input name="checkInDate" type="date" defaultValue={searchCheckInDate} />
                        <input name="checkOutDate" type="date" defaultValue={searchCheckOutDate} />
                        <input
                          name="roomCount"
                          type="number"
                          min={1}
                          max={roomTypeResponse.availableRoomsForRequestedStay ?? undefined}
                          defaultValue={1}
                          disabled={isGuestMode || isBusy || !roomTypeResponse.isBookableForRequestedStay}
                        />
                        <div className="checkbox-list">
                          {travelers.map(traveler => (
                            <label key={traveler.travelerId} className="checkbox-row">
                              <input
                                type="checkbox"
                                name="guestTravelerIds"
                                value={traveler.travelerId}
                                disabled={isGuestMode || isBusy || !roomTypeResponse.isBookableForRequestedStay}
                              />
                              {renderTravelerOptionLabel(traveler)}
                            </label>
                          ))}
                        </div>
                        <button
                          type="submit"
                          disabled={isGuestMode || isBusy || !roomTypeResponse.isBookableForRequestedStay}
                        >
                          {translate('hotels.bookNow')}
                        </button>
                      </form>
                    </li>
                  ))}
                </ul>
              </article>
            ))
          ) : (
            <p className="empty-state">{translate('hotels.empty')}</p>
          )}
        </div>
      ) : null}
    </section>
  )
}
