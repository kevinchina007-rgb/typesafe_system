import type { AttractionResultCardProps } from '../../objects'
import { mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import { ResourceReviewSummaryLoader } from '@/pages/shared/content/ResourceReviewSummaryLoader'
import { filterAttractionSessionsForUseDate, formatAttractionRules, renderAttractionTravelerOptionLabel } from '@/app/stores/models/attraction-booking-model'
import { readAttractionTicketBookingForm } from '../../functions'

export function AttractionResultCard({
  attractionResponse,
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  useDateDraft,
  onRequireLogin,
  onBookAttraction,
  onLoadReviewSummary,
  onLoadReviews,
}: AttractionResultCardProps) {
  return (
    <article className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="flex flex-wrap items-start justify-between gap-3 text-lg font-bold text-slate-950">
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
        <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
          {mapBackendStatusToProductLabel(attractionResponse.status, currentLanguage)}
        </span>
      </div>

      <p>{attractionResponse.description}</p>

      <ul className="grid gap-3">
        {attractionResponse.ticketTypes.map(ticketType => (
          <li key={ticketType.ticketTypeId} className="grid gap-3 border border-slate-200 bg-slate-50 p-4">
            <div>
              <strong>{ticketType.ticketTypeName}</strong>
              <p>{ticketType.description}</p>
              <p>{`${translate('attractions.ticketPrice')}: ${ticketType.priceAmount} ${ticketType.priceCurrency}`}</p>
              <p>{`${translate('attractions.availableDateRange')}: ${ticketType.availableFromDate} - ${ticketType.availableToDate}`}</p>
              <p>{`${translate('attractions.totalQuantity')}: ${ticketType.totalQuantity}`}</p>
              <p>{`${translate('attractions.remainingTickets')}: ${ticketType.availableQuantityForRequestedDate ?? '-'}`}</p>
              <p>{`${translate('attractions.validWeekdays')}: ${ticketType.validWeekdays.map(weekday => translate(`weekdays.${weekday.toLowerCase()}`)).join(' / ')}`}</p>
              {!ticketType.isAvailableForRequestedDate ? <p>{translate('attractions.unavailableForDate')}</p> : null}
              <p>{`${translate('attractions.ticketRules')}: ${formatAttractionRules(ticketType.rules.map(rule => rule.summary), translate)}`}</p>
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
                const { sessionId, travelerIds, useDate } = readAttractionTicketBookingForm(formData)

                await onBookAttraction({
                  attractionId: attractionResponse.attractionId,
                  ticketTypeId: ticketType.ticketTypeId,
                  sessionId,
                  travelerIds,
                  useDate,
                  orderCurrency: ticketType.priceCurrency,
                })
              }}
            >
              <label>
                {translate('attractions.useDate')}
                <input name="useDate" type="date" defaultValue={useDateDraft} required disabled={isBusy} />
              </label>
              {ticketType.sessions.length > 0 ? (
                <label>
                  {translate('attractions.session')}
                  <select name="sessionId" defaultValue="" required disabled={isBusy}>
                    <option value="" disabled>
                      {translate('attractions.selectSession')}
                    </option>
                    {filterAttractionSessionsForUseDate(ticketType.sessions, useDateDraft).map(session => (
                      <option key={session.sessionId} value={session.sessionId}>
                        {`${session.sessionName} | ${session.startsAt.slice(11, 16)}-${session.endsAt.slice(11, 16)}`}
                      </option>
                    ))}
                  </select>
                </label>
              ) : null}

              <div className="grid gap-2">
                <p className="text-sm font-medium text-slate-500">{translate('attractions.selectTravelers')}</p>
                {travelers.map(traveler => (
                  <label key={traveler.travelerId} className="flex items-center gap-2">
                    <input type="checkbox" name="travelerIds" value={traveler.travelerId} disabled={isBusy} />
                    {renderAttractionTravelerOptionLabel(traveler)}
                  </label>
                ))}
              </div>

              <button
                className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                type="submit"
                disabled={isBusy || !ticketType.isAvailableForRequestedDate || (ticketType.availableQuantityForRequestedDate ?? 0) <= 0}
              >
                {translate('attractions.bookNow')}
              </button>
            </form>
          </li>
        ))}
      </ul>
    </article>
  )
}

