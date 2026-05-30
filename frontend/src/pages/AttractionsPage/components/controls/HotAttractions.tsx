import type { HotAttractionsProps } from '../../objects'

export function HotAttractions({ items, translate, onSelect }: HotAttractionsProps) {
  return (
    <div className="grid gap-2">
      <span className="text-sm font-medium text-slate-500">{translate('attractions.hotCities')}</span>
      <div className="flex flex-wrap items-center gap-3">
        {items.map(item => (
          <button key={item} type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => onSelect(item)}>
            {item}
          </button>
        ))}
      </div>
    </div>
  )
}

