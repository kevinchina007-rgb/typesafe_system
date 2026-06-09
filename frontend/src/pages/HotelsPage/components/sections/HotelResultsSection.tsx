import { formatIsoDateTime, localizeBedType, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { HotelResultsSectionProps } from '@/pages/HotelsPage/objects'

export function HotelResultsSection({
  currentLanguage,
  hotelResponses,
  isBusy,
  isGuestMode,
  defaultRoomCount,
  searchCheckInDate,
  searchCheckOutDate,
  selectedTravelerIds,
  translate,
  onRequireLogin,
  onBookHotel,
}: HotelResultsSectionProps) {
  return (
    <div className="grid gap-4">
      {hotelResponses.length > 0 ? (
        hotelResponses.map(hotelResponse => (
          <article key={hotelResponse.hotelId} className="grid gap-5 border border-rose-100 bg-gradient-to-br from-white via-rose-50 to-sky-50 p-5 text-slate-950 shadow-lg shadow-rose-100/50">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div className="grid gap-2">
                <div className="flex flex-wrap items-center gap-3">
                  <strong className="text-3xl font-black tracking-tight text-slate-950">{hotelResponse.hotelName}</strong>
                  <span className="inline-flex w-fit bg-sky-500 px-3 py-1 text-xs font-black uppercase tracking-[0.16em] text-white">{translate('hotels.status')}</span>
                </div>
                <p className="text-lg font-medium text-slate-600">{hotelResponse.location}</p>
              </div>
              <span className="inline-flex min-h-10 items-center justify-center border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-black text-emerald-700">
                {mapBackendStatusToProductLabel(hotelResponse.status, currentLanguage)}
              </span>
            </div>

            <div className="grid gap-3 rounded-2xl border border-slate-200 bg-white/90 p-4 md:grid-cols-2">
              <div className="grid gap-1">
                <span className="text-xs font-black uppercase tracking-[0.16em] text-cyan-600">{translate('hotels.createdAt')}</span>
                <strong className="text-lg font-bold text-slate-950">{formatIsoDateTime(hotelResponse.createdAt, '-')}</strong>
              </div>
              <div className="grid gap-1">
                <span className="text-xs font-black uppercase tracking-[0.16em] text-violet-600">{translate('hotels.status')}</span>
                <strong className="text-lg font-bold text-slate-950">{mapBackendStatusToProductLabel(hotelResponse.status, currentLanguage)}</strong>
              </div>
            </div>

            <ul className="grid gap-4">
              {hotelResponse.roomTypes.map(roomTypeResponse => (
                <li key={roomTypeResponse.roomTypeId} className="border border-slate-200 bg-white p-4 shadow-sm shadow-slate-200/40">
                  <div className="grid gap-4 lg:grid-cols-[minmax(0,1.15fr)_minmax(220px,0.82fr)_minmax(300px,1fr)] lg:items-center">
                    <div className="grid gap-2">
                      <div className="flex flex-wrap items-center gap-3">
                        <strong className="text-2xl font-black text-slate-950">{roomTypeResponse.roomTypeName}</strong>
                        <span className="inline-flex w-fit bg-amber-100 px-2 py-1 text-xs font-bold text-amber-700">{localizeBedType(roomTypeResponse.bedType, currentLanguage)}</span>
                      </div>
                      <p className="text-base font-medium text-slate-600">
                        {`${translate('hotels.capacity')}: ${roomTypeResponse.capacity} | ${translate('hotels.availableRooms')}: ${roomTypeResponse.availableRoomsForRequestedStay ?? '-'}`}
                      </p>
                      <p className="text-lg font-black text-orange-500">
                        {`${translate('hotels.priceFrom')}: ${roomTypeResponse.basePrice} ${roomTypeResponse.currency}`}
                      </p>
                    </div>

                    <div className="grid gap-2">
                      <BackendAssetImage
                        assetUrl={roomTypeResponse.imageUrl}
                        alt={roomTypeResponse.roomTypeName}
                        className="flex aspect-[4/3] w-full items-center justify-center rounded-2xl border border-slate-200 bg-slate-50 object-cover text-sm font-semibold text-slate-400 shadow-sm shadow-slate-200/40"
                        fallbackContent="暂无图片"
                      />
                    </div>

                    <form
                      className="grid gap-3 rounded-2xl border border-slate-200 bg-gradient-to-br from-slate-50 to-white p-4"
                      onSubmit={async event => {
                        event.preventDefault()
                        if (isGuestMode) {
                          onRequireLogin()
                          return
                        }

                        const formData = new FormData(event.currentTarget)
                        await onBookHotel({
                          roomTypeId: roomTypeResponse.roomTypeId,
                          guestTravelerIds: selectedTravelerIds,
                          checkInDate: searchCheckInDate,
                          checkOutDate: searchCheckOutDate,
                          roomCount: Number(formData.get('roomCount') ?? 1),
                        })
                      }}
                    >
                      <div className="grid gap-3">
                        <label className="grid gap-2 text-sm font-black text-slate-700">
                          <span className="text-xs uppercase tracking-[0.14em] text-amber-600">{translate('hotels.roomCount')}</span>
                          <input
                            name="roomCount"
                            type="number"
                            min={1}
                            max={roomTypeResponse.availableRoomsForRequestedStay ?? undefined}
                            defaultValue={defaultRoomCount}
                            disabled={isBusy || !roomTypeResponse.isBookableForRequestedStay}
                            className="min-h-12 border-2 border-amber-200 bg-amber-50/70 px-4 text-base font-semibold text-slate-950 outline-none focus:border-amber-500 focus:bg-white disabled:opacity-50"
                          />
                        </label>
                      </div>

                      <button
                        className="inline-flex min-h-12 items-center justify-center bg-gradient-to-r from-fuchsia-500 via-pink-500 to-orange-500 px-5 py-2 text-base font-black text-white shadow-lg shadow-pink-200/70 transition hover:from-fuchsia-600 hover:via-pink-600 hover:to-orange-600 disabled:cursor-not-allowed disabled:opacity-55"
                        type="submit"
                        disabled={isBusy || !roomTypeResponse.isBookableForRequestedStay || selectedTravelerIds.length === 0}
                      >
                        {translate('hotels.bookNow')}
                      </button>
                    </form>
                  </div>
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
