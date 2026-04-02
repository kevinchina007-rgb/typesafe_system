import { useState } from 'react'

import type { AppLanguage, AttractionResponse, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse } from '../lib/mvp-types'
import { ResourceReviewSummaryLoader } from './ResourceReviewSummaryLoader'
import { mapBackendStatusToProductLabel } from '../lib/view-models'

type AttractionsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onSearchAttractions: (payload: { city?: string; useDate?: string }) => Promise<AttractionResponse[]>
  onBookAttraction: (payload: {
    attractionId: string
    ticketTypeId: string
    travelerIds: string[]
    useDate: string
    orderCurrency: string
  }) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

function renderTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function AttractionsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onSearchAttractions,
  onBookAttraction,
  onLoadReviewSummary,
  onLoadReviews,
}: AttractionsPanelProps) {
  const [attractionResponses, setAttractionResponses] = useState<AttractionResponse[]>([])
  const [hasSearchedAttractions, setHasSearchedAttractions] = useState(false)
  const [searchCity, setSearchCity] = useState('')
  const [useDateDraft, setUseDateDraft] = useState('2026-04-10')

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.attractions')}</p>
          <h2>{translate('attractions.title')}</h2>
        </div>
      </div>

      <p className="hero-copy">{translate('attractions.description')}</p>

      <form
        className="stack-form panel-card"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const nextCity = String(formData.get('city') ?? '').trim()
          setSearchCity(nextCity)
          const nextAttractions = await onSearchAttractions({
            city: nextCity || undefined,
            useDate: useDateDraft || undefined,
          })
          setHasSearchedAttractions(true)
          setAttractionResponses(nextAttractions)
        }}
      >
        <div className="three-column-grid">
          <label>
            {translate('attractions.city')}
            <input name="city" placeholder={translate('attractions.cityPlaceholder')} defaultValue={searchCity} />
          </label>
          <label>
            {translate('attractions.useDate')}
            <input
              name="useDateDraft"
              type="date"
              value={useDateDraft}
              onChange={event => setUseDateDraft(event.target.value)}
              required
            />
          </label>
        </div>

        <button type="submit" disabled={isBusy}>
          {translate('attractions.search')}
        </button>
      </form>

      {isGuestMode ? <p className="empty-state">{translate('attractions.guest')}</p> : null}

      {hasSearchedAttractions ? (
        <div className="entity-list flights-list">
          {attractionResponses.length > 0 ? (
            attractionResponses.map(attractionResponse => (
              <article key={attractionResponse.attractionId} className="panel-card hotel-card">
                <div className="panel-heading">
                  <div>
                    <strong>{attractionResponse.attractionName}</strong>
                    <p>{`${attractionResponse.city} | ${attractionResponse.location}`}</p>
                    <ResourceReviewSummaryLoader
                      currentLanguage={currentLanguage}
                      isBusy={isBusy}
                      isEnabled={!isGuestMode}
                      resourceType="Attraction"
                      resourceId={attractionResponse.attractionId}
                      title={attractionResponse.attractionName}
                      translate={translate}
                      onLoadSummary={onLoadReviewSummary}
                      onLoadReviews={onLoadReviews}
                    />
                  </div>
                  <span className="tag-chip">{mapBackendStatusToProductLabel(attractionResponse.status, currentLanguage)}</span>
                </div>

                <p>{attractionResponse.description}</p>

                <ul className="entity-list">
                  {attractionResponse.ticketTypes.map(ticketType => (
                    <li key={ticketType.ticketTypeId}>
                      <div>
                        <strong>{ticketType.ticketTypeName}</strong>
                        <p>{ticketType.description}</p>
                        <p>{`${translate('attractions.ticketPrice')}: ${ticketType.priceAmount} ${ticketType.priceCurrency}`}</p>
                        <p>{`${translate('attractions.availableDateRange')}: ${ticketType.availableFromDate} - ${ticketType.availableToDate}`}</p>
                        <p>{`${translate('attractions.totalQuantity')}: ${ticketType.totalQuantity}`}</p>
                        <p>{`${translate('attractions.remainingTickets')}: ${ticketType.availableQuantityForRequestedDate ?? '-'}`}</p>
                        <p>{`${translate('attractions.validWeekdays')}: ${ticketType.validWeekdays.map(weekday => translate(`weekdays.${weekday.toLowerCase()}`)).join(' / ')}`}</p>
                        {!ticketType.isAvailableForRequestedDate ? <p>{translate('attractions.unavailableForDate')}</p> : null}
                        <p>{`${translate('attractions.ticketRules')}: ${ticketType.rules.length > 0 ? ticketType.rules.map(rule => rule.summary).join(' | ') : translate('attractions.noRules')}`}</p>
                      </div>

                      <form
                        className="compact-action-block"
                        onSubmit={async event => {
                          event.preventDefault()
                          const formData = new FormData(event.currentTarget)
                          const travelerIds = formData.getAll('travelerIds').map(value => String(value)).filter(Boolean)
                          const useDate = String(formData.get('useDate') ?? '').trim()

                          await onBookAttraction({
                            attractionId: attractionResponse.attractionId,
                            ticketTypeId: ticketType.ticketTypeId,
                            travelerIds,
                            useDate,
                            orderCurrency: ticketType.priceCurrency,
                          })
                        }}
                      >
                        <label>
                          {translate('attractions.useDate')}
                          <input name="useDate" type="date" defaultValue={useDateDraft} required disabled={isBusy || isGuestMode} />
                        </label>

                        <div className="checkbox-list">
                          <p className="detail-label">{translate('attractions.selectTravelers')}</p>
                          {travelers.map(traveler => (
                            <label key={traveler.travelerId} className="checkbox-row">
                              <input type="checkbox" name="travelerIds" value={traveler.travelerId} disabled={isGuestMode || isBusy} />
                              {renderTravelerOptionLabel(traveler)}
                            </label>
                          ))}
                        </div>

                        <button
                          type="submit"
                          disabled={isGuestMode || isBusy || !ticketType.isAvailableForRequestedDate || (ticketType.availableQuantityForRequestedDate ?? 0) <= 0}
                        >
                          {translate('attractions.bookNow')}
                        </button>
                      </form>
                    </li>
                  ))}
                </ul>
              </article>
            ))
          ) : (
            <p className="empty-state">{translate('attractions.empty')}</p>
          )}
        </div>
      ) : null}
    </section>
  )
}
