type HotDestinationsProps = {
  destinations: string[]
  translate: (translationKey: string) => string
  onSelectDestination: (value: string) => void
}

export function HotDestinations({ destinations, translate, onSelectDestination }: HotDestinationsProps) {
  return (
    <div className="grid gap-2">
      <span className="text-sm font-medium text-slate-500">{translate('hotels.hotDestinations')}</span>
      <div className="flex flex-wrap items-center gap-3">
        {destinations.map(destination => (
          <button key={destination} type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => onSelectDestination(destination)}>
            {destination}
          </button>
        ))}
      </div>
    </div>
  )
}
