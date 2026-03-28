import { useState } from 'react'

import type { AppLanguage, TrainResponse, TravelerResponse } from '../lib/mvp-types'
import { formatIsoDateTime, localizeTrainSeatClass, mapBackendStatusToProductLabel } from '../lib/view-models'

type TrainsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onSearchTrains: (payload: {
    fromStation?: string
    toStation?: string
    date?: string
  }) => Promise<TrainResponse[]>
  onBookTrain: (payload: {
    trainId: string
    travelerIds: string[]
    fromStationCode: string
    toStationCode: string
    seatClass: string
    orderCurrency: string
  }) => Promise<void>
}

function renderTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

function quoteTrainSegmentAmount(
  train: TrainResponse,
  fromStationCode: string,
  toStationCode: string,
  seatClass: string,
): { amount: string; currency: string } | null {
  const normalizedFrom = fromStationCode.trim().toUpperCase()
  const normalizedTo = toStationCode.trim().toUpperCase()
  const normalizedSeatClass = seatClass.trim().toLowerCase()
  const fromIndex = train.stops.findIndex(stop => stop.stationCode.toUpperCase() === normalizedFrom)
  const toIndex = train.stops.findIndex(stop => stop.stationCode.toUpperCase() === normalizedTo)

  if (fromIndex < 0 || toIndex < 0 || fromIndex >= toIndex) {
    return null
  }

  const pathStops = train.stops.slice(fromIndex, toIndex + 1)
  const matchingSegmentPrices = pathStops.slice(0, -1).map((currentStop, index) =>
    train.segmentPrices.find(
      segmentPrice =>
        segmentPrice.fromStationCode.toUpperCase() === currentStop.stationCode.toUpperCase() &&
        segmentPrice.toStationCode.toUpperCase() === pathStops[index + 1].stationCode.toUpperCase() &&
        segmentPrice.seatClass.trim().toLowerCase() === normalizedSeatClass,
    ),
  )

  if (matchingSegmentPrices.some(segmentPrice => !segmentPrice)) {
    return null
  }

  const segmentPrices = matchingSegmentPrices.flatMap(segmentPrice => (segmentPrice ? [segmentPrice] : []))
  const currency = segmentPrices[0]?.currency

  if (!currency || segmentPrices.some(segmentPrice => segmentPrice.currency !== currency)) {
    return null
  }

  const amount = segmentPrices.reduce((currentAmount, segmentPrice) => currentAmount + Number(segmentPrice.amount), 0)
  return { amount: amount.toString(), currency }
}

function renderStopSummary(train: TrainResponse): string {
  return train.stops.map(stop => stop.stationCode).join(' -> ')
}

export function TrainsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onSearchTrains,
  onBookTrain,
}: TrainsPanelProps) {
  const [trainResponses, setTrainResponses] = useState<TrainResponse[]>([])
  const [hasSearchedTrains, setHasSearchedTrains] = useState(false)
  const [searchDate, setSearchDate] = useState('2026-04-05')
  const [searchFromStation, setSearchFromStation] = useState('SHH')
  const [searchToStation, setSearchToStation] = useState('NJN')

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.trains')}</p>
          <h2>{translate('trains.title')}</h2>
        </div>
      </div>

      <p className="hero-copy">{translate('trains.description')}</p>
      <p className="empty-state">{translate('trains.searchHint')}</p>

      <form
        className="stack-form panel-card"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const nextFromStation = String(formData.get('fromStation') ?? '').trim()
          const nextToStation = String(formData.get('toStation') ?? '').trim()
          const nextDate = String(formData.get('date') ?? '').trim()
          setSearchFromStation(nextFromStation)
          setSearchToStation(nextToStation)
          setSearchDate(nextDate)
          const nextTrains = await onSearchTrains({
            fromStation: nextFromStation || undefined,
            toStation: nextToStation || undefined,
            date: nextDate || undefined,
          })
          setHasSearchedTrains(true)
          setTrainResponses(nextTrains)
        }}
      >
        <div className="three-column-grid">
          <label>
            {translate('trains.fromStation')}
            <input name="fromStation" placeholder="SHH" defaultValue={searchFromStation} />
          </label>
          <label>
            {translate('trains.toStation')}
            <input name="toStation" placeholder="NJN" defaultValue={searchToStation} />
          </label>
          <label>
            {translate('trains.date')}
            <input name="date" type="date" defaultValue={searchDate} />
          </label>
        </div>

        <button type="submit" disabled={isBusy}>
          {translate('trains.search')}
        </button>
      </form>

      {isGuestMode ? <p className="empty-state">{translate('trains.guest')}</p> : null}

      {hasSearchedTrains ? (
        <div className="entity-list flights-list">
          {trainResponses.length > 0 ? (
            trainResponses.map(trainResponse => (
              <article key={trainResponse.trainId} className="panel-card hotel-card">
                <div className="panel-heading">
                  <div>
                    <strong>{trainResponse.trainNumber}</strong>
                    <p>{renderStopSummary(trainResponse)}</p>
                  </div>
                  <span className="tag-chip">{mapBackendStatusToProductLabel(trainResponse.status, currentLanguage)}</span>
                </div>

                <div className="detail-grid">
                  <div>
                    <span className="detail-label">{translate('trains.saleStartsAt')}</span>
                    <strong>{formatIsoDateTime(trainResponse.saleStartsAt, '-')}</strong>
                  </div>
                  <div>
                    <span className="detail-label">{translate('trains.stopCount')}</span>
                    <strong>{trainResponse.stops.length}</strong>
                  </div>
                </div>

                <ul className="entity-list">
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
                          className="compact-action-block"
                          onSubmit={async event => {
                            event.preventDefault()
                            if (!quote) {
                              throw new Error('train_price_not_defined')
                            }

                            const formData = new FormData(event.currentTarget)
                            const selectedTravelerIds = formData
                              .getAll('travelerIds')
                              .map(value => String(value))
                              .filter(Boolean)

                            await onBookTrain({
                              trainId: trainResponse.trainId,
                              travelerIds: selectedTravelerIds,
                              fromStationCode: searchFromStation,
                              toStationCode: searchToStation,
                              seatClass: seatInventory.seatClass,
                              orderCurrency: quote.currency,
                            })
                          }}
                        >
                          <div className="checkbox-list">
                            <p className="detail-label">{translate('trains.selectTravelers')}</p>
                            {travelers.map(traveler => (
                              <label key={traveler.travelerId} className="checkbox-row">
                                <input
                                  type="checkbox"
                                  name="travelerIds"
                                  value={traveler.travelerId}
                                  disabled={isGuestMode || isBusy || !quote}
                                />
                                {renderTravelerOptionLabel(traveler)}
                              </label>
                            ))}
                          </div>
                          <button type="submit" disabled={isGuestMode || isBusy || !quote}>
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
            <p className="empty-state">{translate('trains.empty')}</p>
          )}
        </div>
      ) : null}
    </section>
  )
}
