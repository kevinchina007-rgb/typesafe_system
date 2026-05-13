import { useState } from 'react'

import { formatIsoDateTime, localizeBedType, localizeCabinClass, localizeManagerTaskType, localizeSupplierReviewStatus, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { normalizeDateTimeInput } from '@/pages/ManagerPage/components/managers/manager-panel-shared'

type ManagerPanelWorkspaceProps = Pick<
  ManagerPanelProps,
  | 'currentLanguage'
  | 'isBusy'
  | 'managerSession'
  | 'managedFlights'
  | 'managedHotels'
  | 'managerTasks'
  | 'managerRefundTasks'
  | 'translate'
  | 'onReloadTasks'
  | 'onReloadRefundTasks'
  | 'onCreateManagerFlight'
  | 'onCreateManagerRoomType'
  | 'onConfirmTask'
  | 'onRejectTask'
  | 'onBatchConfirmTasks'
  | 'onBatchRejectTasks'
  | 'onApproveRefundTask'
  | 'onRejectRefundTask'
>

export function ManagerPanelWorkspace({
  currentLanguage,
  isBusy,
  managerSession,
  managedFlights,
  managedHotels,
  managerTasks,
  managerRefundTasks,
  translate,
  onReloadTasks,
  onReloadRefundTasks,
  onCreateManagerFlight,
  onCreateManagerRoomType,
  onConfirmTask,
  onRejectTask,
  onBatchConfirmTasks,
  onBatchRejectTasks,
  onApproveRefundTask,
  onRejectRefundTask,
}: ManagerPanelWorkspaceProps) {
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
    <>
      {managerSession ? (
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
      ) : null}

      {managerSession?.managerType === 'Airline' ? (
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
              <label>{translate('manager.flightNumber')}<input name="flightNumber" placeholder="MU5123" required /></label>
              <label>{translate('manager.departureAirport')}<input name="departureAirport" placeholder="PVG" required /></label>
              <label>{translate('manager.arrivalAirport')}<input name="arrivalAirport" placeholder="HND" required /></label>
              <label>{translate('manager.departureTime')}<input name="departureTime" type="datetime-local" required /></label>
              <label>{translate('manager.arrivalTime')}<input name="arrivalTime" type="datetime-local" required /></label>
              <label>{translate('manager.economySeatCount')}<input name="economySeatCount" type="number" min={1} defaultValue={20} required /></label>
              <label>{translate('manager.economyPrice')}<input name="economyPrice" type="number" min={1} defaultValue={880} required /></label>
              <label>{translate('manager.businessSeatCount')}<input name="businessSeatCount" type="number" min={1} defaultValue={6} required /></label>
              <label>{translate('manager.businessPrice')}<input name="businessPrice" type="number" min={1} defaultValue={1880} required /></label>
              <label>
                {translate('manager.currency')}
                <select name="currency" defaultValue="CNY">
                  <option value="CNY">CNY</option>
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                </select>
              </label>
            </div>
            <button type="submit" disabled={isBusy}>{translate('manager.createFlight')}</button>
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

      {managerSession?.managerType === 'Hotel' ? (
        <>
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
              <label>{translate('manager.roomTypeName')}<input name="roomTypeName" placeholder={translate('manager.roomTypeName')} required /></label>
              <label>{translate('manager.capacity')}<input name="capacity" type="number" min={1} defaultValue={2} required /></label>
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
              <label>{translate('manager.nightlyPrice')}<input name="nightlyPrice" type="number" min={1} defaultValue={699} required /></label>
              <label>
                {translate('manager.currency')}
                <select name="currency" defaultValue="CNY">
                  <option value="CNY">CNY</option>
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                </select>
              </label>
              <label>{translate('manager.availableRooms')}<input name="availableRooms" type="number" min={1} defaultValue={5} required /></label>
              <label>{translate('manager.inventoryStartDate')}<input name="inventoryStartDate" type="date" defaultValue="2026-04-01" required /></label>
              <label>{translate('manager.inventoryEndDate')}<input name="inventoryEndDate" type="date" defaultValue="2026-04-30" required /></label>
            </div>
            <button type="submit" disabled={isBusy}>{translate('manager.createRoomType')}</button>
          </form>

          <div className="list-surface">
            <h3>{translate('manager.hotelName')}</h3>
            {managedHotels.length === 0 ? (
              <p className="empty-state">{translate('manager.empty')}</p>
            ) : (
              <ul className="entity-list">
                {managedHotels.map(hotel => (
                  <li key={hotel.hotelId}>
                    <div>
                      <strong>{hotel.hotelName}</strong>
                      <p>{`${translate('manager.hotelLocation')}: ${hotel.location}`}</p>
                      <p>{`${translate('manager.status')}: ${hotel.status}`}</p>
                      <p>{`${translate('manager.enteredAt')}: ${formatIsoDateTime(hotel.createdAt, '-')}`}</p>
                      {hotel.roomTypes.length === 0 ? (
                        <p>{translate('manager.empty')}</p>
                      ) : (
                        <div className="manager-roomtype-list">
                          {hotel.roomTypes.map(roomType => (
                            <div key={roomType.roomTypeId} className="tag-chip">
                              {`${roomType.roomTypeName} \u00B7 ${localizeBedType(roomType.bedType, currentLanguage)} \u00B7 ${roomType.capacity} \u00B7 ${roomType.basePrice} ${roomType.currency}`}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
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
        <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onReloadTasks({ status: taskFilter, resourceType: resourceFilter })}>
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
              <label>{translate('manager.note')}<input value={bulkNote} placeholder={translate('manager.notePlaceholder')} onChange={event => setBulkNote(event.target.value)} /></label>
              <label>{translate('manager.rejectReason')}<input value={bulkReason} placeholder={translate('manager.rejectReasonPlaceholder')} onChange={event => setBulkReason(event.target.value)} /></label>
            </div>
            <div className="compact-action-block">
              <button type="button" disabled={isBusy || selectedOrderItemIds.length === 0} onClick={() => void onBatchConfirmTasks({ orderItemIds: selectedOrderItemIds, note: bulkNote })}>
                {translate('manager.batchConfirm')}
              </button>
              <button type="button" className="secondary-button" disabled={isBusy || selectedOrderItemIds.length === 0} onClick={() => void onBatchRejectTasks({ orderItemIds: selectedOrderItemIds, reason: bulkReason })}>
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
                      <input type="checkbox" checked={selectedOrderItemIds.includes(task.orderItemId)} onChange={() => toggleTaskSelection(task.orderItemId)} />
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
                      onChange={event => setDraftNotesByTaskId(currentDrafts => ({ ...currentDrafts, [task.orderItemId]: event.target.value }))}
                    />
                  </label>
                  <label>
                    {translate('manager.rejectReason')}
                    <input
                      value={draftReasonsByTaskId[task.orderItemId] ?? ''}
                      placeholder={translate('manager.rejectReasonPlaceholder')}
                      disabled={isBusy || task.supplierReviewStatus !== 'PendingSupplierConfirmation'}
                      onChange={event => setDraftReasonsByTaskId(currentDrafts => ({ ...currentDrafts, [task.orderItemId]: event.target.value }))}
                    />
                  </label>
                  <div className="compact-action-block">
                    <button type="button" disabled={isBusy || task.supplierReviewStatus !== 'PendingSupplierConfirmation'} onClick={() => void onConfirmTask({ orderItemId: task.orderItemId, note: draftNotesByTaskId[task.orderItemId] ?? '' })}>
                      {translate('manager.confirm')}
                    </button>
                    <button type="button" className="secondary-button" disabled={isBusy || task.supplierReviewStatus !== 'PendingSupplierConfirmation'} onClick={() => void onRejectTask({ orderItemId: task.orderItemId, reason: draftReasonsByTaskId[task.orderItemId] ?? '' })}>
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
  )
}
