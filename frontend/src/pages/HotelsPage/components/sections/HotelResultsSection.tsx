import type { AppLanguage, HotelResponse, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeBedType, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { ResourceReviewSummaryLoader } from '@/pages/shared/content/ResourceReviewSummaryLoader'
import { renderHotelTravelerOptionLabel } from '@/app/stores/models/hotel-booking-model'

type HotelResultsSectionProps = {
  currentLanguage: AppLanguage
  hotelResponses: HotelResponse[]
  isBusy: boolean
  isGuestMode: boolean
  defaultRoomCount: number
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
  defaultRoomCount,
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
    <div className="grid gap-4">
      {hotelResponses.length > 0 ? (
        hotelResponses.map(hotelResponse => (
          <article key={hotelResponse.hotelId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div className="grid gap-1">
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
              <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{mapBackendStatusToProductLabel(hotelResponse.status, currentLanguage)}</span>
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <div>
                <span className="text-sm font-medium text-slate-500">{translate('hotels.createdAt')}</span>
                <strong>{formatIsoDateTime(hotelResponse.createdAt, '-')}</strong>
              </div>
              <div>
                <span className="text-sm font-medium text-slate-500">{translate('hotels.status')}</span>
                <strong>{mapBackendStatusToProductLabel(hotelResponse.status, currentLanguage)}</strong>
              </div>
            </div>

            <ul className="grid gap-3">
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
                    className="flex flex-wrap items-center gap-3"
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
                        defaultValue={defaultRoomCount}
                        disabled={isBusy || !roomTypeResponse.isBookableForRequestedStay}
                      />
                    </label>
                    <div className="grid gap-2">
                      <p className="text-sm font-medium text-slate-500">{translate('hotels.selectGuests')}</p>
                      {travelers.map(traveler => (
                        <label key={traveler.travelerId} className="flex items-center gap-2">
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
                    <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy || !roomTypeResponse.isBookableForRequestedStay}>
                      {translate('hotels.bookNow')}
                    </button>
                  </form>
                </li>
              ))}
            </ul>
          </article>
        ))
      ) : (
        <p className="text-sm leading-6 text-slate-500">{translate('hotels.empty')}</p>
      )}
    </div>
  )
}
