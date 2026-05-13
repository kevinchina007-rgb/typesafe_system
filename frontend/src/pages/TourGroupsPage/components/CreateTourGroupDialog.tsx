import { useEffect, useState } from 'react'

type CreateTourGroupDialogProps = {
  isOpen: boolean
  isBusy: boolean
  translate: (translationKey: string) => string
  onClose: () => void
  onCreateGroup: (payload: {
    title: string
    description: string
    destination: string
    startDate: string
    endDate: string
    capacity: number
  }) => Promise<void>
}

export function CreateTourGroupDialog({
  isOpen,
  isBusy,
  translate,
  onClose,
  onCreateGroup,
}: CreateTourGroupDialogProps) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [destination, setDestination] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [capacity, setCapacity] = useState(4)

  useEffect(() => {
    if (!isOpen) {
      setTitle('')
      setDescription('')
      setDestination('')
      setStartDate('')
      setEndDate('')
      setCapacity(4)
    }
  }, [isOpen])

  if (!isOpen) {
    return null
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal-card" role="dialog" aria-modal="true" aria-label={translate('tourGroups.createGroup')}>
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('nav.tourGroups')}</p>
            <h3>{translate('tourGroups.createGroup')}</h3>
          </div>
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onClose}>
            {translate('payment.close')}
          </button>
        </div>

        <p className="hero-copy">{translate('tourGroups.createDialogHint')}</p>

        <form
          className="stack-form"
          onSubmit={async event => {
            event.preventDefault()
            await onCreateGroup({
              title,
              description,
              destination,
              startDate,
              endDate,
              capacity,
            })
          }}
        >
          <div className="three-column-grid">
            <label>
              {translate('tourGroups.groupTitle')}
              <input value={title} onChange={event => setTitle(event.target.value)} placeholder={translate('tourGroups.groupTitlePlaceholder')} required />
            </label>
            <label>
              {translate('tourGroups.destination')}
              <input value={destination} onChange={event => setDestination(event.target.value)} placeholder={translate('tourGroups.destinationPlaceholder')} required />
            </label>
            <label>
              {translate('tourGroups.capacity')}
              <input type="number" min={1} value={capacity} onChange={event => setCapacity(Number(event.target.value || 1))} required />
            </label>
            <label>
              {translate('tourGroups.startDate')}
              <input type="date" value={startDate} onChange={event => setStartDate(event.target.value)} required />
            </label>
            <label>
              {translate('tourGroups.endDate')}
              <input type="date" value={endDate} onChange={event => setEndDate(event.target.value)} required />
            </label>
            <label>
              {translate('tourGroups.descriptionLabel')}
              <input value={description} onChange={event => setDescription(event.target.value)} placeholder={translate('tourGroups.descriptionPlaceholder')} required />
            </label>
          </div>

          <div className="action-cluster">
            <button type="submit" disabled={isBusy}>
              {translate('tourGroups.createGroup')}
            </button>
            <button type="button" className="secondary-button" disabled={isBusy} onClick={onClose}>
              {translate('tourGroups.cancel')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
