import type { AppLanguage, HotelResponse, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse } from '../../../lib/mvp-types'
import { formatIsoDateTime, localizeBedType, mapBackendStatusToProductLabel } from '../../../lib/view-models'
import { ResourceReviewSummaryLoader } from '../../ResourceReviewSummaryLoader'
import { renderHotelTravelerOptionLabel } from '../hotelBookingModel'

type HotelResultsSectionProps = {
  currentLanguage: AppLanguage
  hotelResponses: HotelResponse[]
  isBusy: boolean
  isGuestMode: boolean
  searchCheckInDate: string
  searchCheckOutDate: string
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onBookHotel: (payload: {
    roomTypeId: string
    guestTravelerIds: string[]
    checkInDate: string
    checkOutDate: string
    roomCount: number
  }) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export function HotelResultsSection({
  currentLanguage,
  hotelResponses,
  isBusy,
  isGuestMode,
  searchCheckInDate,
  searchCheckOutDate,
  travelers,
  translate,
  onRequireLogin,
  onBookHotel,
  onLoadReviewSummary,
  onLoadReviews,
}: HotelResultsSectionProps) {
  return (
    <div className="entity-list flights-list">
      {hotelResponses.length > 0 ? (
        hotelResponses.map(hotelResponse => (
          <article key={hotelResponse.hotelId} className="panel-card hotel-card">
            <div className="panel-heading">
              <div>
                <strong>{hotelResponse.hotelName}</strong>
                <p>{hotelResponse.location}</p>
                <ResourceReviewSummaryLoader
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  isEnabled={!isGuestMode}
                  resourceType="Hotel"
                  resourceId={hotelResponse.hotelId}
                  title={hotelResponse.hotelName}
                  translate={translate}
                  onLoadSummary={onLoadReviewSummary}
                  onLoadReviews={onLoadReviews}
                />
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
                      if (isGuestMode) {
                        onRequireLogin()
                        return
                      }
                      const formData = new FormData(event.currentTarget)
                      const selectedGuestTravelerIds = formData.getAll('guestTravelerIds').map(value => String(value)).filter(Boolean)
                      await onBookHotel({
                        roomTypeId: roomTypeResponse.roomTypeId,
                        guestTravelerIds: selectedGuestTravelerIds,
                        checkInDate: String(formData.get('checkInDate') ?? searchCheckInDate),
                        checkOutDate: String(formData.get('checkOutDate') ?? searchCheckOutDate),
                        roomCount: Number(formData.get('roomCount') ?? 1),
                      })
                    }}
                  >
                    <label>
                      {translate('hotels.checkInDate')}
                      <input name="checkInDate" type="date" defaultValue={searchCheckInDate} />
                    </label>
                    <label>
                      {translate('hotels.checkOutDate')}
                      <input name="checkOutDate" type="date" defaultValue={searchCheckOutDate} />
                    </label>
                    <label>
                      {translate('hotels.roomCount')}
                      <input
                        name="roomCount"
                        type="number"
                        min={1}
                        max={roomTypeResponse.availableRoomsForRequestedStay ?? undefined}
                        defaultValue={1}
                        disabled={isBusy || !roomTypeResponse.isBookableForRequestedStay}
                      />
                    </label>
                    <div className="checkbox-list">
                      <p className="detail-label">{translate('hotels.selectGuests')}</p>
                      {travelers.map(traveler => (
                        <label key={traveler.travelerId} className="checkbox-row">
                          <input
                            type="checkbox"
                            name="guestTravelerIds"
                            value={traveler.travelerId}
                            disabled={isBusy || !roomTypeResponse.isBookableForRequestedStay}
                          />
                          {renderHotelTravelerOptionLabel(traveler)}
                        </label>
                      ))}
                    </div>
                    <button type="submit" disabled={isBusy || !roomTypeResponse.isBookableForRequestedStay}>
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
  )
}
