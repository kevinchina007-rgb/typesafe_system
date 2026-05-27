type HotDestinationsProps = {
  destinations: string[]
  translate: (translationKey: string) => string
  onSelectDestination: (value: string) => void
}

export function HotDestinations({ destinations, translate, onSelectDestination }: HotDestinationsProps) {
  return (
    <div className="grid gap-3">
      <div className="flex flex-wrap items-center gap-3">
        <span className="text-sm font-black uppercase tracking-[0.12em] text-slate-500">{translate('hotels.hotDestinations')}</span>
        <span className="inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">推荐</span>
      </div>
      <div className="flex flex-wrap items-center gap-3">
        {destinations.map(destination => (
          <button
            key={destination}
            type="button"
            className="inline-flex min-h-10 items-center justify-center border border-fuchsia-200 bg-gradient-to-r from-white to-fuchsia-50 px-4 py-2 text-sm font-bold text-fuchsia-700 shadow-sm shadow-fuchsia-100/60 transition hover:border-fuchsia-400 hover:from-fuchsia-500 hover:to-pink-500 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
            onClick={() => onSelectDestination(destination)}
          >
            {destination}
          </button>
        ))}
      </div>
    </div>
  )
}
