import type { AppLanguage, ResourceReviewSummaryResponse, ReviewResponse, TrainResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeTrainSeatClass, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { ResourceReviewSummaryLoader } from '@/pages/shared/content/ResourceReviewSummaryLoader'
import { quoteTrainSegmentAmount, renderTrainStopSummary, renderTrainTravelerOptionLabel } from '@/app/stores/models/train-booking-model'

type TrainResultsSectionProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  searchFromStation: string
  searchToStation: string
  trainResponses: TrainResponse[]
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onBookTrain: (payload: {
    trainId: string
    travelerIds: string[]
    fromStationCode: string
    toStationCode: string
    seatClass: string
    seatPreference?: string | null
    orderCurrency: string
  }) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export function TrainResultsSection({
  currentLanguage,
  isBusy,
  isGuestMode,
  searchFromStation,
  searchToStation,
  trainResponses,
  travelers,
  translate,
  onRequireLogin,
  onBookTrain,
  onLoadReviewSummary,
  onLoadReviews,
}: TrainResultsSectionProps) {
  return (
    <div className="grid gap-3 grid gap-3">
      {trainResponses.length > 0 ? (
        trainResponses.map(trainResponse => (
          <article key={trainResponse.trainId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-3">
            <div className="text-lg font-bold text-slate-950">
              <div>
                <strong>{trainResponse.trainNumber}</strong>
                <p>{renderTrainStopSummary(trainResponse)}</p>
                <ResourceReviewSummaryLoader
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  isEnabled={!isGuestMode}
                  resourceType="Train"
                  resourceId={trainResponse.trainId}
                  title={trainResponse.trainNumber}
                  translate={translate}
                  onLoadSummary={onLoadReviewSummary}
                  onLoadReviews={onLoadReviews}
                />
              </div>
              <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{mapBackendStatusToProductLabel(trainResponse.status, currentLanguage)}</span>
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <div>
                <span className="text-sm font-medium text-slate-500">{translate('trains.saleStartsAt')}</span>
                <strong>{formatIsoDateTime(trainResponse.saleStartsAt, '-')}</strong>
              </div>
              <div>
                <span className="text-sm font-medium text-slate-500">{translate('trains.stopCount')}</span>
                <strong>{trainResponse.stops.length}</strong>
              </div>
            </div>

            <ul className="grid gap-3">
              {trainResponse.seatInventories.map(seatInventory => {
                const quote = quoteTrainSegmentAmount(trainResponse, searchFromStation, searchToStation, seatInventory.seatClass)

                return (
                  <li key={seatInventory.inventoryId}>
                    <div>
                      <strong>{localizeTrainSeatClass(seatInventory.seatClass, currentLanguage)}</strong>
                      <p>{`${translate('trains.saleableSeats')}: ${seatInventory.saleableSeats} / ${seatInventory.totalSeats}`}</p>
                      <p>{`${translate('trains.routePrice')}: ${quote ? `${quote.amount} ${quote.currency}` : translate('trains.routePriceUnavailable')}`}</p>
                    </div>

                    <form
                      className="flex flex-wrap items-center gap-3"
                      onSubmit={async event => {
                        event.preventDefault()
                        if (isGuestMode) {
                          onRequireLogin()
                          return
                        }
                        if (!quote) {
                          throw new Error('train_price_not_defined')
                        }

                        const formData = new FormData(event.currentTarget)
                        const selectedTravelerIds = formData.getAll('travelerIds').map(value => String(value)).filter(Boolean)

                        await onBookTrain({
                          trainId: trainResponse.trainId,
                          travelerIds: selectedTravelerIds,
                          fromStationCode: searchFromStation,
                          toStationCode: searchToStation,
                          seatClass: seatInventory.seatClass,
                          seatPreference: String(formData.get('seatPreference') ?? '').trim() || null,
                          orderCurrency: quote.currency,
                        })
                      }}
                    >
                      <label>
                        {translate('trains.seatPreference')}
                        <select name="seatPreference" defaultValue="no_preference" disabled={isBusy || !quote}>
                          <option value="no_preference">{translate('trains.noPreference')}</option>
                          <option value="window">{translate('trains.window')}</option>
                          <option value="aisle">{translate('trains.aisle')}</option>
                          <option value="middle">{translate('trains.middle')}</option>
                        </select>
                      </label>
                      <div className="grid gap-2">
                        <p className="text-sm font-medium text-slate-500">{translate('trains.selectTravelers')}</p>
                        {travelers.map(traveler => (
                          <label key={traveler.travelerId} className="flex items-center gap-2">
                            <input type="checkbox" name="travelerIds" value={traveler.travelerId} disabled={isBusy || !quote} />
                            {renderTrainTravelerOptionLabel(traveler)}
                          </label>
                        ))}
                      </div>
                      <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy || !quote}>
                        {translate('trains.bookNow')}
                      </button>
                    </form>
                  </li>
                )
              })}
            </ul>
          </article>
        ))
      ) : (
        <p className="text-sm leading-6 text-slate-500">{translate('trains.empty')}</p>
      )}
    </div>
  )
}
