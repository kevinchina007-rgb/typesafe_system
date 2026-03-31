import { useMemo } from 'react'

import type { AppLanguage, TrainAdminSessionResponse } from '../lib/mvp-types'
import { formatIsoDateTime, localizeTrainSeatClass, mapBackendStatusToProductLabel } from '../lib/view-models'

function normalizeDateTimeInput(rawValue: string): string {
  const trimmedValue = rawValue.trim()
  if (!trimmedValue) {
    return trimmedValue
  }

  const parsedDate = new Date(trimmedValue)
  if (Number.isNaN(parsedDate.getTime())) {
    return trimmedValue
  }

  return parsedDate.toISOString()
}

type TrainAdminPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  trainAdminSession: TrainAdminSessionResponse | null
  translate: (translationKey: string) => string
  onRegisterRailwayManager: (payload: {
    operatorCode: string
    email: string
    displayName: string
  }) => Promise<void>
  onLoginRailwayManager: (payload: { email: string }) => Promise<void>
  onReloadManagedTrains: () => Promise<void>
  onCreateTrainJourney: (payload: {
    trainNumber: string
    saleStartsAt: string
    stops: Array<{ stationCode: string; stationName: string; arrivalTime?: string | null; departureTime?: string | null }>
    seatInventories: Array<{ seatClass: string; totalSeats: number; saleableSeats: number }>
    segmentPrices: Array<{ fromStationCode: string; toStationCode: string; seatClass: string; amount: string; currency: string }>
    refundPolicies: Array<{ startOffsetMinutesBeforeDeparture: number; endOffsetMinutesBeforeDeparture: number; refundType: string; refundRate: string }>
  }) => Promise<void>
  onLogoutRailwayManager: () => void
}

function parsePipeSeparatedLines<T>(rawValue: string, parser: (parts: string[], lineNumber: number) => T): T[] {
  return rawValue
    .split(/\r?\n/)
    .map(line => line.trim())
    .filter(Boolean)
    .map((line, index) => parser(line.split('|').map(part => part.trim()), index + 1))
}

export function TrainAdminPanel({
  currentLanguage,
  isBusy,
  trainAdminSession,
  translate,
  onRegisterRailwayManager,
  onLoginRailwayManager,
  onReloadManagedTrains,
  onCreateTrainJourney,
  onLogoutRailwayManager,
}: TrainAdminPanelProps) {
  const defaultStopsExample = useMemo(
    () =>
      [
        'SHH|Shanghai Hongqiao||2026-04-10T08:00:00Z',
        'NJN|Nanjing South|2026-04-10T09:10:00Z|2026-04-10T09:13:00Z',
        'HZH|Hangzhou East|2026-04-10T10:20:00Z|',
      ].join('\n'),
    [],
  )

  const defaultSeatInventoryExample = useMemo(() => ['second-class|240|240', 'first-class|60|60'].join('\n'), [])
  const defaultSegmentPriceExample = useMemo(
    () =>
      ['SHH|NJN|second-class|149|CNY', 'NJN|HZH|second-class|119|CNY', 'SHH|NJN|first-class|239|CNY', 'NJN|HZH|first-class|199|CNY'].join(
        '\n',
      ),
    [],
  )
  const defaultRefundPolicyExample = useMemo(
    () => ['1440|720|full_refund|1', '720|120|partial_refund|0.8', '120|0|non_refundable|0'].join('\n'),
    [],
  )

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.trainAdmin')}</p>
          <h2>{translate('trainAdmin.title')}</h2>
        </div>
        {trainAdminSession ? (
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onLogoutRailwayManager}>
            {translate('trainAdmin.logout')}
          </button>
        ) : null}
      </div>

      <p className="hero-copy">{translate('trainAdmin.description')}</p>

      {!trainAdminSession ? (
        <div className="two-column-grid">
          <form
            className="stack-form panel-card"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onRegisterRailwayManager({
                operatorCode: String(formData.get('operatorCode') ?? '').trim(),
                email: String(formData.get('email') ?? '').trim(),
                displayName: String(formData.get('displayName') ?? '').trim(),
              })
              event.currentTarget.reset()
            }}
          >
            <h3>{translate('trainAdmin.registerManager')}</h3>
            <label>
              {translate('trainAdmin.operatorCode')}
              <input name="operatorCode" placeholder="CRH" required />
            </label>
            <label>
              {translate('trainAdmin.displayName')}
              <input name="displayName" placeholder="Rail Ops" required />
            </label>
            <label>
              {translate('trainAdmin.email')}
              <input name="email" type="email" placeholder="ops@rail.example" required />
            </label>
            <button type="submit" disabled={isBusy}>
              {translate('trainAdmin.createAccount')}
            </button>
          </form>

          <form
            className="stack-form panel-card"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onLoginRailwayManager({
                email: String(formData.get('email') ?? '').trim(),
              })
            }}
          >
            <h3>{translate('trainAdmin.login')}</h3>
            <label>
              {translate('trainAdmin.email')}
              <input name="email" type="email" placeholder="ops@rail.example" required />
            </label>
            <button type="submit" disabled={isBusy}>
              {translate('trainAdmin.login')}
            </button>
          </form>
        </div>
      ) : (
        <>
          <div className="detail-grid">
            <div>
              <span className="detail-label">{translate('trainAdmin.displayName')}</span>
              <strong>{trainAdminSession.displayName}</strong>
            </div>
            <div>
              <span className="detail-label">{translate('trainAdmin.operatorCode')}</span>
              <strong>{trainAdminSession.operatorCode}</strong>
            </div>
            <div>
              <span className="detail-label">{translate('trainAdmin.email')}</span>
              <strong>{trainAdminSession.email}</strong>
            </div>
            <div>
              <span className="detail-label">{translate('trainAdmin.status')}</span>
              <strong>{mapBackendStatusToProductLabel(trainAdminSession.status, currentLanguage)}</strong>
            </div>
          </div>

          <form
            className="stack-form panel-card"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              const stops = parsePipeSeparatedLines(String(formData.get('stops') ?? ''), (parts, lineNumber) => {
                if (parts.length < 4) {
                  throw new Error(`Stop line ${lineNumber} must contain stationCode|stationName|arrivalTime|departureTime`)
                }
                return {
                  stationCode: parts[0],
                  stationName: parts[1],
                  arrivalTime: parts[2] ? normalizeDateTimeInput(parts[2]) : null,
                  departureTime: parts[3] ? normalizeDateTimeInput(parts[3]) : null,
                }
              })
              const seatInventories = parsePipeSeparatedLines(String(formData.get('seatInventories') ?? ''), (parts, lineNumber) => {
                if (parts.length < 3) {
                  throw new Error(`Seat inventory line ${lineNumber} must contain seatClass|totalSeats|saleableSeats`)
                }
                return {
                  seatClass: parts[0],
                  totalSeats: Number(parts[1]),
                  saleableSeats: Number(parts[2]),
                }
              })
              const segmentPrices = parsePipeSeparatedLines(String(formData.get('segmentPrices') ?? ''), (parts, lineNumber) => {
                if (parts.length < 5) {
                  throw new Error(`Segment price line ${lineNumber} must contain from|to|seatClass|amount|currency`)
                }
                return {
                  fromStationCode: parts[0],
                  toStationCode: parts[1],
                  seatClass: parts[2],
                  amount: parts[3],
                  currency: parts[4],
                }
              })
              const refundPolicies = parsePipeSeparatedLines(String(formData.get('refundPolicies') ?? ''), (parts, lineNumber) => {
                if (parts.length < 4) {
                  throw new Error(`Refund policy line ${lineNumber} must contain startOffset|endOffset|refundType|refundRate`)
                }
                return {
                  startOffsetMinutesBeforeDeparture: Number(parts[0]),
                  endOffsetMinutesBeforeDeparture: Number(parts[1]),
                  refundType: parts[2],
                  refundRate: parts[3],
                }
              })

              await onCreateTrainJourney({
                trainNumber: String(formData.get('trainNumber') ?? '').trim(),
                saleStartsAt: normalizeDateTimeInput(String(formData.get('saleStartsAt') ?? '').trim()),
                stops,
                seatInventories,
                segmentPrices,
                refundPolicies,
              })
            }}
          >
            <h3>{translate('trainAdmin.createTrain')}</h3>
            <div className="three-column-grid">
              <label>
                {translate('trainAdmin.trainNumber')}
                <input name="trainNumber" placeholder="G12" required />
              </label>
              <label>
                {translate('trainAdmin.saleStartsAt')}
                <input name="saleStartsAt" type="datetime-local" required />
              </label>
            </div>
            <label>
              {translate('trainAdmin.stops')}
              <textarea name="stops" rows={5} defaultValue={defaultStopsExample} />
            </label>
            <label>
              {translate('trainAdmin.seatInventories')}
              <textarea name="seatInventories" rows={4} defaultValue={defaultSeatInventoryExample} />
            </label>
            <label>
              {translate('trainAdmin.segmentPrices')}
              <textarea name="segmentPrices" rows={6} defaultValue={defaultSegmentPriceExample} />
            </label>
            <label>
              {translate('trainAdmin.refundPolicies')}
              <textarea name="refundPolicies" rows={4} defaultValue={defaultRefundPolicyExample} />
            </label>
            <button type="submit" disabled={isBusy}>
              {translate('trainAdmin.createTrain')}
            </button>
          </form>

          <div className="action-cluster">
            <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onReloadManagedTrains()}>
              {translate('trainAdmin.refresh')}
            </button>
          </div>

          <div className="list-surface">
            {trainAdminSession.managedTrains.length === 0 ? (
              <p className="empty-state">{translate('trainAdmin.empty')}</p>
            ) : (
              <ul className="entity-list">
                {trainAdminSession.managedTrains.map(train => (
                  <li key={train.trainId}>
                    <div>
                      <strong>{train.trainNumber}</strong>
                      <p>{`${translate('trainAdmin.saleStartsAt')}: ${formatIsoDateTime(train.saleStartsAt, '-')}`}</p>
                      <p>{`${translate('trainAdmin.status')}: ${mapBackendStatusToProductLabel(train.status, currentLanguage)}`}</p>
                      <p>{train.stops.map(stop => stop.stationCode).join(' -> ')}</p>
                      <p>
                        {train.seatInventories
                          .map(seatInventory => `${localizeTrainSeatClass(seatInventory.seatClass, currentLanguage)} ${seatInventory.saleableSeats}/${seatInventory.totalSeats}`)
                          .join(' | ')}
                      </p>
                    </div>
                    <span className="tag-chip">{train.segmentPrices.length}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}
    </section>
  )
}
