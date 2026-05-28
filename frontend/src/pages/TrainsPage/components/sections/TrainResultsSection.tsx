import type { AppLanguage, ResourceReviewSummaryResponse, ReviewResponse, TrainResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeTrainSeatClass, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { ResourceReviewSummaryLoader } from '@/pages/shared/content/ResourceReviewSummaryLoader'
import {
  quoteTrainSegmentAmount,
  renderTrainStopSummary,
  renderTrainTravelerOptionLabel,
  resolveTrainStationCodes,
} from '@/app/stores/models/train-booking-model'

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

function formatStopTimeLabel(stop: TrainResponse['stops'][number] | null | undefined): string {
  return formatIsoDateTime(stop?.departureTime ?? stop?.arrivalTime ?? null, '-')
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
    <div className="grid gap-4">
      {trainResponses.length > 0 ? (
        trainResponses.map(trainResponse => {
          const departureStop = trainResponse.stops[0] ?? null
          const arrivalStop = trainResponse.stops[trainResponse.stops.length - 1] ?? null
          const viaStops = trainResponse.stops.slice(1, -1)
          const resolvedStationCodes = resolveTrainStationCodes(trainResponse, searchFromStation, searchToStation)

          return (
            <article key={trainResponse.trainId} className="grid gap-5 border border-sky-100 bg-white p-5 text-slate-950 shadow-sm shadow-sky-100/40">
              <header className="flex flex-wrap items-start justify-between gap-4">
                <div className="grid gap-2">
                  <div className="flex flex-wrap items-center gap-3">
                    <span className="inline-flex min-h-8 items-center justify-center border border-sky-200 bg-sky-50 px-3 py-1 text-xs font-black uppercase tracking-[0.2em] text-sky-700">
                      车次
                    </span>
                    <strong className="text-2xl font-black text-slate-950">{trainResponse.trainNumber}</strong>
                  </div>
                  <p className="text-sm leading-6 text-slate-600">{renderTrainStopSummary(trainResponse)}</p>
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
                <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
                  {mapBackendStatusToProductLabel(trainResponse.status, currentLanguage)}
                </span>
              </header>

              <div className="grid gap-3 xl:grid-cols-[1fr_auto_1fr]">
                <div className="grid gap-2 border border-cyan-100 bg-cyan-50/60 p-4">
                  <span className="text-sm font-medium text-cyan-700">出发</span>
                  <strong className="text-2xl font-black text-slate-950">{departureStop?.stationName ?? '--'}</strong>
                  <p className="text-sm text-slate-600">{formatStopTimeLabel(departureStop)}</p>
                </div>
                <div className="hidden xl:grid place-items-center text-3xl font-black text-slate-300">→</div>
                <div className="grid gap-2 border border-violet-100 bg-violet-50/60 p-4">
                  <span className="text-sm font-medium text-violet-700">到达</span>
                  <strong className="text-2xl font-black text-slate-950">{arrivalStop?.stationName ?? '--'}</strong>
                  <p className="text-sm text-slate-600">{formatStopTimeLabel(arrivalStop)}</p>
                </div>
              </div>

              {viaStops.length > 0 ? (
                <div className="grid gap-2">
                  <span className="text-sm font-medium text-slate-500">经停站</span>
                  <div className="flex flex-wrap items-center gap-2">
                    {viaStops.map(stop => (
                      <span key={stop.stopId} className="inline-flex min-h-8 items-center justify-center border border-slate-200 bg-slate-50 px-3 py-1 text-sm font-medium text-slate-700">
                        {stop.stationName}
                      </span>
                    ))}
                  </div>
                </div>
              ) : null}

              <div className="grid gap-3">
                {trainResponse.seatInventories.map(seatInventory => {
                  const quote = resolvedStationCodes
                    ? quoteTrainSegmentAmount(trainResponse, searchFromStation, searchToStation, seatInventory.seatClass)
                    : null

                  return (
                    <div key={seatInventory.inventoryId} className="grid gap-4 border border-slate-200 bg-white p-4 shadow-sm shadow-slate-100/60">
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div className="grid gap-1">
                          <strong className="text-lg font-black text-slate-950">{localizeTrainSeatClass(seatInventory.seatClass, currentLanguage)}</strong>
                          <p className="text-sm text-slate-500">{`${translate('trains.saleableSeats')}: ${seatInventory.saleableSeats} / ${seatInventory.totalSeats}`}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-medium text-slate-500">{translate('trains.routePrice')}</p>
                          <p className="text-2xl font-black text-slate-950">
                            {quote ? `${quote.amount} ${quote.currency}` : translate('trains.routePriceUnavailable')}
                          </p>
                        </div>
                      </div>

                      <form
                        className="grid gap-4"
                        onSubmit={async event => {
                          event.preventDefault()
                          if (isGuestMode) {
                            onRequireLogin()
                            return
                          }
                          if (!quote || !resolvedStationCodes) {
                            throw new Error('train_station_not_found')
                          }

                          const formData = new FormData(event.currentTarget)
                          const selectedTravelerIds = formData.getAll('travelerIds').map(value => String(value)).filter(Boolean)
                          if (selectedTravelerIds.length === 0) {
                            throw new Error('train_traveler_required')
                          }

                          await onBookTrain({
                            trainId: trainResponse.trainId,
                            travelerIds: selectedTravelerIds,
                            fromStationCode: resolvedStationCodes.fromStationCode,
                            toStationCode: resolvedStationCodes.toStationCode,
                            seatClass: seatInventory.seatClass,
                            seatPreference: String(formData.get('seatPreference') ?? '').trim() || null,
                            orderCurrency: quote.currency,
                          })
                        }}
                      >
                        <div className="grid gap-3 md:grid-cols-[220px_1fr]">
                          <label className="grid gap-2">
                            <span className="text-sm font-medium text-slate-500">{translate('trains.seatPreference')}</span>
                            <select name="seatPreference" defaultValue="no_preference" disabled={isBusy || !quote}>
                              <option value="no_preference">{translate('trains.noPreference')}</option>
                              <option value="window">{translate('trains.window')}</option>
                              <option value="aisle">{translate('trains.aisle')}</option>
                              <option value="middle">{translate('trains.middle')}</option>
                            </select>
                          </label>

                          <div className="grid gap-2">
                            <p className="text-sm font-medium text-slate-500">{translate('trains.selectTravelers')}</p>
                            <div className="flex flex-wrap gap-3">
                              {travelers.map(traveler => (
                                <label key={traveler.travelerId} className="inline-flex items-center gap-2 border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-medium text-slate-700">
                                  <input type="checkbox" name="travelerIds" value={traveler.travelerId} disabled={isBusy || !quote} />
                                  {renderTrainTravelerOptionLabel(traveler)}
                                </label>
                              ))}
                            </div>
                          </div>
                        </div>

                        <div className="flex flex-wrap items-center justify-between gap-3">
                          <div className="text-sm text-slate-500">
                            {searchFromStation && searchToStation ? `${searchFromStation} → ${searchToStation}` : '--'}
                          </div>
                          <button
                            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-slate-950 hover:bg-slate-950 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                            type="submit"
                            disabled={isBusy || !quote}
                          >
                            {translate('trains.bookNow')}
                          </button>
                        </div>
                      </form>
                    </div>
                  )
                })}
              </div>
            </article>
          )
        })
      ) : (
        <p className="text-sm leading-6 text-slate-500">{translate('trains.empty')}</p>
      )}
    </div>
  )
}
