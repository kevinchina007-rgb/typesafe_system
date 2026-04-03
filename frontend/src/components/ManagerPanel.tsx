import { useState } from 'react'

import type {
  AppLanguage,
  FlightResponse,
  ManagerRefundTaskResponse,
  ManagerSessionResponse,
  ManagerTaskResponse,
  ManagerType,
} from '../lib/mvp-types'
import {
  formatIsoDateTime,
  localizeCabinClass,
  localizeManagerTaskType,
  localizeSupplierReviewStatus,
  mapBackendStatusToProductLabel,
} from '../lib/view-models'

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

type ManagerPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  managerSession: ManagerSessionResponse | null
  managedFlights: FlightResponse[]
  managerTasks: ManagerTaskResponse[]
  managerRefundTasks: ManagerRefundTaskResponse[]
  translate: (translationKey: string) => string
  onRegisterAirlineManager: (payload: {
    email: string
    displayName: string
    airlineName: string
    airlineCode: string
    password: string
  }) => Promise<void>
  onRegisterHotelManager: (payload: {
    email: string
    displayName: string
    hotelName: string
    location: string
    password: string
  }) => Promise<void>
  onCreateManagerRoomType: (payload: {
    managerId: string
    roomTypeName: string
    capacity: number
    bedType: string
    nightlyPrice: string
    currency: string
    availableRooms: number
    inventoryStartDate: string
    inventoryEndDate: string
  }) => Promise<void>
  onLoginManager: (payload: { managerType: ManagerType; email: string; password: string }) => Promise<void>
  onValidationError: (message: string) => void
  onReloadTasks: (filters: {
    status: 'pending' | 'all' | 'confirmed' | 'rejected'
    resourceType: 'all' | 'flight' | 'hotel' | 'train' | 'attraction'
  }) => Promise<void>
  onReloadRefundTasks: () => Promise<void>
  onCreateManagerFlight: (payload: {
    flightNumber: string
    departureAirport: string
    arrivalAirport: string
    departureTime: string
    arrivalTime: string
    economySeatCount: number
    economyPrice: string
    businessSeatCount: number
    businessPrice: string
    currency: string
  }) => Promise<void>
  onConfirmTask: (payload: { orderItemId: string; note: string }) => Promise<void>
  onRejectTask: (payload: { orderItemId: string; reason: string }) => Promise<void>
  onBatchConfirmTasks: (payload: { orderItemIds: string[]; note: string }) => Promise<void>
  onBatchRejectTasks: (payload: { orderItemIds: string[]; reason: string }) => Promise<void>
  onApproveRefundTask: (payload: { orderId: string }) => Promise<void>
  onRejectRefundTask: (payload: { orderId: string }) => Promise<void>
  onLogoutManager: () => void
}

export function ManagerPanel({
  currentLanguage,
  isBusy,
  managerSession,
  managedFlights,
  managerTasks,
  managerRefundTasks,
  translate,
  onRegisterAirlineManager,
  onRegisterHotelManager,
  onCreateManagerRoomType,
  onLoginManager,
  onValidationError,
  onReloadTasks,
  onReloadRefundTasks,
  onCreateManagerFlight,
  onConfirmTask,
  onRejectTask,
  onBatchConfirmTasks,
  onBatchRejectTasks,
  onApproveRefundTask,
  onRejectRefundTask,
  onLogoutManager,
}: ManagerPanelProps) {
  const [taskFilter, setTaskFilter] = useState<'pending' | 'all' | 'confirmed' | 'rejected'>('pending')
  const [resourceFilter, setResourceFilter] = useState<'all' | 'flight' | 'hotel' | 'train' | 'attraction'>('all')
  const [draftNotesByTaskId, setDraftNotesByTaskId] = useState<Record<string, string>>({})
  const [draftReasonsByTaskId, setDraftReasonsByTaskId] = useState<Record<string, string>>({})
  const [selectedOrderItemIds, setSelectedOrderItemIds] = useState<string[]>([])
  const [bulkNote, setBulkNote] = useState('')
  const [bulkReason, setBulkReason] = useState('')

  const visiblePendingTaskIds = managerTasks
    .filter(task => task.supplierReviewStatus === 'PendingSupplierConfirmation')
    .map(task => task.orderItemId)

  function toggleTaskSelection(orderItemId: string) {
    setSelectedOrderItemIds(currentIds =>
      currentIds.includes(orderItemId) ? currentIds.filter(currentId => currentId !== orderItemId) : [...currentIds, orderItemId],
    )
  }

  function toggleSelectAllPending(checked: boolean) {
    setSelectedOrderItemIds(checked ? visiblePendingTaskIds : [])
  }

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.manager')}</p>
          <h2>{translate('manager.title')}</h2>
        </div>
        {managerSession ? (
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onLogoutManager}>
            {translate('manager.logout')}
          </button>
        ) : null}
      </div>

      <p className="hero-copy">{translate('manager.description')}</p>

      {!managerSession ? (
        <>
          <div className="two-column-grid">
            <form
              className="stack-form panel-card"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                const password = String(formData.get('password') ?? '')
                const confirmPassword = String(formData.get('confirmPassword') ?? '')
                if (password !== confirmPassword) {
                  onValidationError(translate('error.passwordMismatch'))
                  return
                }
                await onRegisterAirlineManager({
                  email: String(formData.get('email') ?? ''),
                  displayName: String(formData.get('displayName') ?? ''),
                  airlineName: String(formData.get('airlineName') ?? ''),
                  airlineCode: String(formData.get('airlineCode') ?? ''),
                  password,
                })
                event.currentTarget.reset()
              }}
            >
              <h3>{translate('manager.registerAirline')}</h3>
              <label>
                {translate('manager.displayName')}
                <input name="displayName" placeholder={translate('manager.displayName')} required />
              </label>
              <label>
                {translate('manager.email')}
                <input name="email" type="email" placeholder={translate('manager.email')} required />
              </label>
              <label>
                {translate('manager.airlineName')}
                <input name="airlineName" placeholder={translate('manager.airlineName')} required />
              </label>
              <label>
                {translate('manager.airlineCode')}
                <input name="airlineCode" placeholder="MU" required />
              </label>
              <label>
                {translate('account.password')}
                <input name="password" type="password" placeholder={translate('account.password')} required />
              </label>
              <label>
                {translate('account.confirmPassword')}
                <input name="confirmPassword" type="password" placeholder={translate('account.confirmPassword')} required />
              </label>
              <button type="submit" disabled={isBusy}>
                {translate('manager.createAccount')}
              </button>
            </form>

            <form
              className="stack-form panel-card"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onLoginManager({
                  managerType: 'airline',
                  email: String(formData.get('email') ?? ''),
                  password: String(formData.get('password') ?? ''),
                })
              }}
            >
              <h3>{translate('manager.loginAirline')}</h3>
              <label>
                {translate('manager.email')}
                <input name="email" type="email" placeholder={translate('manager.email')} required disabled={isBusy} />
              </label>
              <label>
                {translate('account.password')}
                <input name="password" type="password" placeholder={translate('account.password')} required disabled={isBusy} />
              </label>
              <button type="submit" disabled={isBusy}>
                {translate('manager.loginAirline')}
              </button>
            </form>
          </div>

          <div className="two-column-grid">
            <form
              className="stack-form panel-card"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                const password = String(formData.get('password') ?? '')
                const confirmPassword = String(formData.get('confirmPassword') ?? '')
                if (password !== confirmPassword) {
                  onValidationError(translate('error.passwordMismatch'))
                  return
                }
                await onRegisterHotelManager({
                  email: String(formData.get('email') ?? ''),
                  displayName: String(formData.get('displayName') ?? ''),
                  hotelName: String(formData.get('hotelName') ?? ''),
                  location: String(formData.get('location') ?? ''),
                  password,
                })
                event.currentTarget.reset()
              }}
            >
              <h3>{translate('manager.registerHotel')}</h3>
              <label>
                {translate('manager.displayName')}
                <input name="displayName" placeholder={translate('manager.displayName')} required />
              </label>
              <label>
                {translate('manager.email')}
                <input name="email" type="email" placeholder={translate('manager.email')} required />
              </label>
              <label>
                {translate('manager.hotelName')}
                <input name="hotelName" placeholder={translate('manager.hotelName')} required />
              </label>
              <label>
                {translate('manager.hotelLocation')}
                <input name="location" placeholder={translate('manager.hotelLocation')} required />
              </label>
              <label>
                {translate('account.password')}
                <input name="password" type="password" placeholder={translate('account.password')} required />
              </label>
              <label>
                {translate('account.confirmPassword')}
                <input name="confirmPassword" type="password" placeholder={translate('account.confirmPassword')} required />
              </label>
              <button type="submit" disabled={isBusy}>
                {translate('manager.createAccount')}
              </button>
            </form>

            <form
              className="stack-form panel-card"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onLoginManager({
                  managerType: 'hotel',
                  email: String(formData.get('email') ?? ''),
                  password: String(formData.get('password') ?? ''),
                })
              }}
            >
              <h3>{translate('manager.loginHotel')}</h3>
              <label>
                {translate('manager.email')}
                <input name="email" type="email" placeholder={translate('manager.email')} required disabled={isBusy} />
              </label>
              <label>
                {translate('account.password')}
                <input name="password" type="password" placeholder={translate('account.password')} required disabled={isBusy} />
              </label>
              <button type="submit" disabled={isBusy}>
                {translate('manager.loginHotel')}
              </button>
            </form>
          </div>
        </>
      ) : (
        <>
          <div className="detail-grid">
            <div>
              <span className="detail-label">{translate('manager.profile')}</span>
              <strong>{managerSession.displayName}</strong>
            </div>
            <div>
              <span className="detail-label">{translate('manager.scope')}</span>
              <strong>{managerSession.scopeId}</strong>
            </div>
            <div>
              <span className="detail-label">{translate('manager.status')}</span>
              <strong>{managerSession.status}</strong>
            </div>
            <div>
              <span className="detail-label">{translate('manager.enteredAt')}</span>
              <strong>{formatIsoDateTime(managerSession.createdAt, '-')}</strong>
            </div>
          </div>

          {managerSession.managerType === 'Airline' ? (
            <>
              <form
                className="stack-form panel-card"
                onSubmit={async event => {
                  event.preventDefault()
                  const formData = new FormData(event.currentTarget)
                  await onCreateManagerFlight({
                    flightNumber: String(formData.get('flightNumber') ?? ''),
                    departureAirport: String(formData.get('departureAirport') ?? ''),
                    arrivalAirport: String(formData.get('arrivalAirport') ?? ''),
                    departureTime: normalizeDateTimeInput(String(formData.get('departureTime') ?? '')),
                    arrivalTime: normalizeDateTimeInput(String(formData.get('arrivalTime') ?? '')),
                    economySeatCount: Number(formData.get('economySeatCount') ?? 20),
                    economyPrice: String(formData.get('economyPrice') ?? '880'),
                    businessSeatCount: Number(formData.get('businessSeatCount') ?? 6),
                    businessPrice: String(formData.get('businessPrice') ?? '1880'),
                    currency: String(formData.get('currency') ?? 'CNY'),
                  })
                  event.currentTarget.reset()
                }}
              >
                <h3>{translate('manager.createFlight')}</h3>
                <div className="three-column-grid">
                  <label>
                    {translate('manager.flightNumber')}
                    <input name="flightNumber" placeholder="MU5123" required />
                  </label>
                  <label>
                    {translate('manager.departureAirport')}
                    <input name="departureAirport" placeholder="PVG" required />
                  </label>
                  <label>
                    {translate('manager.arrivalAirport')}
                    <input name="arrivalAirport" placeholder="HND" required />
                  </label>
                  <label>
                    {translate('manager.departureTime')}
                    <input name="departureTime" type="datetime-local" required />
                  </label>
                  <label>
                    {translate('manager.arrivalTime')}
                    <input name="arrivalTime" type="datetime-local" required />
                  </label>
                  <label>
                    {translate('manager.economySeatCount')}
                    <input name="economySeatCount" type="number" min={1} defaultValue={20} required />
                  </label>
                  <label>
                    {translate('manager.economyPrice')}
                    <input name="economyPrice" type="number" min={1} defaultValue={880} required />
                  </label>
                  <label>
                    {translate('manager.businessSeatCount')}
                    <input name="businessSeatCount" type="number" min={1} defaultValue={6} required />
                  </label>
                  <label>
                    {translate('manager.businessPrice')}
                    <input name="businessPrice" type="number" min={1} defaultValue={1880} required />
                  </label>
                  <label>
                    {translate('manager.currency')}
                    <select name="currency" defaultValue="CNY">
                      <option value="CNY">CNY</option>
                      <option value="USD">USD</option>
                      <option value="EUR">EUR</option>
                    </select>
                  </label>
                </div>
                <button type="submit" disabled={isBusy}>
                  {translate('manager.createFlight')}
                </button>
              </form>

              <div className="list-surface">
                <h3>{translate('manager.createFlight')}</h3>
                {managedFlights.length === 0 ? (
                  <p className="empty-state">{translate('manager.empty')}</p>
                ) : (
                  <ul className="entity-list">
                    {managedFlights.map(flight => (
                      <li key={flight.flightId}>
                        <div>
                          <strong>{`${flight.airlineName} ${flight.flightNumber}`}</strong>
                          <p>{`${flight.departureAirport} -> ${flight.arrivalAirport}`}</p>
                          <p>{`${formatIsoDateTime(flight.departureTime, '-')} -> ${formatIsoDateTime(flight.arrivalTime, '-')}`}</p>
                          <p>{mapBackendStatusToProductLabel(flight.status, currentLanguage)}</p>
                        </div>
                        <span className="tag-chip">{flight.cabinInventories.map(cabin => localizeCabinClass(cabin.cabinClass, currentLanguage)).join(' | ')}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </>
          ) : null}

          {managerSession.managerType === 'Hotel' ? (
            <form
              className="stack-form panel-card"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onCreateManagerRoomType({
                  managerId: managerSession.managerId,
                  roomTypeName: String(formData.get('roomTypeName') ?? ''),
                  capacity: Number(formData.get('capacity') ?? 2),
                  bedType: String(formData.get('bedType') ?? 'queen'),
                  nightlyPrice: String(formData.get('nightlyPrice') ?? '699'),
                  currency: String(formData.get('currency') ?? 'CNY'),
                  availableRooms: Number(formData.get('availableRooms') ?? 5),
                  inventoryStartDate: String(formData.get('inventoryStartDate') ?? ''),
                  inventoryEndDate: String(formData.get('inventoryEndDate') ?? ''),
                })
                event.currentTarget.reset()
              }}
            >
              <h3>{translate('manager.createRoomType')}</h3>
              <div className="three-column-grid">
                <label>
                  {translate('manager.roomTypeName')}
                  <input name="roomTypeName" placeholder={translate('manager.roomTypeName')} required />
                </label>
                <label>
                  {translate('manager.capacity')}
                  <input name="capacity" type="number" min={1} defaultValue={2} required />
                </label>
                <label>
                  {translate('manager.bedType')}
                  <select name="bedType" defaultValue="queen">
                    <option value="single">Single</option>
                    <option value="double">Double</option>
                    <option value="twin">Twin</option>
                    <option value="queen">Queen</option>
                    <option value="king">King</option>
                  </select>
                </label>
                <label>
                  {translate('manager.nightlyPrice')}
                  <input name="nightlyPrice" type="number" min={1} defaultValue={699} required />
                </label>
                <label>
                  {translate('manager.currency')}
                  <select name="currency" defaultValue="CNY">
                    <option value="CNY">CNY</option>
                    <option value="USD">USD</option>
                    <option value="EUR">EUR</option>
                  </select>
                </label>
                <label>
                  {translate('manager.availableRooms')}
                  <input name="availableRooms" type="number" min={1} defaultValue={5} required />
                </label>
                <label>
                  {translate('manager.inventoryStartDate')}
                  <input name="inventoryStartDate" type="date" defaultValue="2026-04-01" required />
                </label>
                <label>
                  {translate('manager.inventoryEndDate')}
                  <input name="inventoryEndDate" type="date" defaultValue="2026-04-30" required />
                </label>
              </div>
              <button type="submit" disabled={isBusy}>
                {translate('manager.createRoomType')}
              </button>
            </form>
          ) : null}

          <div className="action-cluster">
            <select value={taskFilter} disabled={isBusy} onChange={event => setTaskFilter(event.target.value as typeof taskFilter)}>
              <option value="pending">{translate('manager.filter.pending')}</option>
              <option value="confirmed">{translate('manager.filter.confirmed')}</option>
              <option value="rejected">{translate('manager.filter.rejected')}</option>
              <option value="all">{translate('manager.filter.all')}</option>
            </select>
            <select value={resourceFilter} disabled={isBusy} onChange={event => setResourceFilter(event.target.value as typeof resourceFilter)}>
              <option value="all">{translate('manager.resourceFilter.all')}</option>
              <option value="flight">{translate('manager.resourceFilter.flight')}</option>
              <option value="hotel">{translate('manager.resourceFilter.hotel')}</option>
              <option value="train">{translate('manager.resourceFilter.train')}</option>
              <option value="attraction">{translate('manager.resourceFilter.attraction')}</option>
            </select>
            <button
              type="button"
              className="secondary-button"
              disabled={isBusy}
              onClick={() => void onReloadTasks({ status: taskFilter, resourceType: resourceFilter })}
            >
              {translate('manager.refresh')}
            </button>
            <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onReloadRefundTasks()}>
              {translate('manager.refundTasks')}
            </button>
          </div>

          <div className="list-surface">
            {visiblePendingTaskIds.length > 0 ? (
              <div className="panel-card stack-form">
                <div className="panel-heading">
                  <strong>{translate('manager.batchActions')}</strong>
                  <label className="checkbox-row">
                    <input
                      type="checkbox"
                      checked={selectedOrderItemIds.length > 0 && selectedOrderItemIds.length === visiblePendingTaskIds.length}
                      onChange={event => toggleSelectAllPending(event.target.checked)}
                    />
                    {translate('manager.selectAllPending')}
                  </label>
                </div>
                <p className="detail-label">{translate('manager.selectedCount').replace('{count}', String(selectedOrderItemIds.length))}</p>
                <div className="three-column-grid">
                  <label>
                    {translate('manager.note')}
                    <input value={bulkNote} placeholder={translate('manager.notePlaceholder')} onChange={event => setBulkNote(event.target.value)} />
                  </label>
                  <label>
                    {translate('manager.rejectReason')}
                    <input
                      value={bulkReason}
                      placeholder={translate('manager.rejectReasonPlaceholder')}
                      onChange={event => setBulkReason(event.target.value)}
                    />
                  </label>
                </div>
                <div className="compact-action-block">
                  <button
                    type="button"
                    disabled={isBusy || selectedOrderItemIds.length === 0}
                    onClick={() => void onBatchConfirmTasks({ orderItemIds: selectedOrderItemIds, note: bulkNote })}
                  >
                    {translate('manager.batchConfirm')}
                  </button>
                  <button
                    type="button"
                    className="secondary-button"
                    disabled={isBusy || selectedOrderItemIds.length === 0}
                    onClick={() => void onBatchRejectTasks({ orderItemIds: selectedOrderItemIds, reason: bulkReason })}
                  >
                    {translate('manager.batchReject')}
                  </button>
                </div>
              </div>
            ) : null}
            {managerTasks.length === 0 ? (
              <p className="empty-state">{translate('manager.empty')}</p>
            ) : (
              <ul className="entity-list">
                {managerTasks.map(task => (
                  <li key={task.orderItemId}>
                    <div>
                      {task.supplierReviewStatus === 'PendingSupplierConfirmation' ? (
                        <label className="checkbox-row">
                          <input
                            type="checkbox"
                            checked={selectedOrderItemIds.includes(task.orderItemId)}
                            onChange={() => toggleTaskSelection(task.orderItemId)}
                          />
                          {translate('manager.selectTask')}
                        </label>
                      ) : null}
                      <strong>{task.summaryLabel}</strong>
                      <p>{localizeManagerTaskType(task.taskType, currentLanguage)}</p>
                      <p>{task.detailLabel}</p>
                      <p>{localizeSupplierReviewStatus(task.supplierReviewStatus, currentLanguage)}</p>
                      <p>{`${translate('manager.requestedAt')}: ${formatIsoDateTime(task.requestedAt, '-')}`}</p>
                      {task.reviewedAt ? <p>{`${translate('manager.reviewedAt')}: ${formatIsoDateTime(task.reviewedAt, '-')}`}</p> : null}
                      {task.reviewedBy ? <p>{`${translate('manager.reviewedBy')}: ${task.reviewedBy}`}</p> : null}
                      {task.reviewNote ? <p>{`${translate('manager.reviewNote')}: ${task.reviewNote}`}</p> : null}
                    </div>
                    <div className="manager-task-actions">
                      <label>
                        {translate('manager.note')}
                        <input
                          value={draftNotesByTaskId[task.orderItemId] ?? ''}
                          placeholder={translate('manager.notePlaceholder')}
                          disabled={isBusy || task.supplierReviewStatus !== 'PendingSupplierConfirmation'}
                          onChange={event =>
                            setDraftNotesByTaskId(currentDrafts => ({
                              ...currentDrafts,
                              [task.orderItemId]: event.target.value,
                            }))
                          }
                        />
                      </label>
                      <label>
                        {translate('manager.rejectReason')}
                        <input
                          value={draftReasonsByTaskId[task.orderItemId] ?? ''}
                          placeholder={translate('manager.rejectReasonPlaceholder')}
                          disabled={isBusy || task.supplierReviewStatus !== 'PendingSupplierConfirmation'}
                          onChange={event =>
                            setDraftReasonsByTaskId(currentDrafts => ({
                              ...currentDrafts,
                              [task.orderItemId]: event.target.value,
                            }))
                          }
                        />
                      </label>
                      <div className="compact-action-block">
                        <button
                          type="button"
                          disabled={isBusy || task.supplierReviewStatus !== 'PendingSupplierConfirmation'}
                          onClick={() => void onConfirmTask({ orderItemId: task.orderItemId, note: draftNotesByTaskId[task.orderItemId] ?? '' })}
                        >
                          {translate('manager.confirm')}
                        </button>
                        <button
                          type="button"
                          className="secondary-button"
                          disabled={isBusy || task.supplierReviewStatus !== 'PendingSupplierConfirmation'}
                          onClick={() => void onRejectTask({ orderItemId: task.orderItemId, reason: draftReasonsByTaskId[task.orderItemId] ?? '' })}
                        >
                          {translate('manager.reject')}
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            )}

            {managerRefundTasks.length > 0 ? (
              <>
                <h3>{translate('manager.refundTasks')}</h3>
                <ul className="entity-list">
                  {managerRefundTasks.map(task => (
                    <li key={task.refundId}>
                      <div>
                        <strong>{task.summaryLabel}</strong>
                        <p>{`${task.refundAmount} ${task.refundCurrency}`}</p>
                        <p>{task.refundReason}</p>
                        <p>{formatIsoDateTime(task.requestedAt, '-')}</p>
                      </div>
                      <div className="compact-action-block">
                        <button type="button" disabled={isBusy} onClick={() => void onApproveRefundTask({ orderId: task.orderId })}>
                          {translate('manager.approveRefund')}
                        </button>
                        <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onRejectRefundTask({ orderId: task.orderId })}>
                          {translate('manager.rejectRefund')}
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              </>
            ) : null}
          </div>
        </>
      )}
    </section>
  )
}
