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
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6" role="presentation">
      <div className="grid max-h-[90vh] w-full max-w-3xl gap-4 overflow-auto border border-slate-200 bg-white p-6 text-slate-950 shadow-2xl shadow-slate-950/20" role="dialog" aria-modal="true" aria-label={translate('tourGroups.createGroup')}>
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('nav.tourGroups')}</p>
            <h3>{translate('tourGroups.createGroup')}</h3>
          </div>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onClose}>
            {translate('payment.close')}
          </button>
        </div>

        <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.createDialogHint')}</p>

        <form
          className="grid gap-4"
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
          <div className="grid gap-4 md:grid-cols-3">
            <label>
              {translate('tourGroups.groupTitle')}
              <input value={title} onChange={event => setTitle(event.target.value)} required />
            </label>
            <label>
              {translate('tourGroups.destination')}
              <input value={destination} onChange={event => setDestination(event.target.value)} required />
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
              <input value={description} onChange={event => setDescription(event.target.value)} required />
            </label>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
              {translate('tourGroups.createGroup')}
            </button>
            <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onClose}>
              {translate('tourGroups.cancel')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
