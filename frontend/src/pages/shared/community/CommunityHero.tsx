type CommunityTab = {
  key: string
  label: string
}

type CommunityHeroProps = {
  eyebrow: string
  title: string
  searchValue: string
  searchButtonLabel: string
  tabs: CommunityTab[]
  activeTab: string
  primaryActionLabel?: string
  secondaryActionLabel?: string
  isBusy?: boolean
  onSearchChange: (value: string) => void
  onSearchSubmit: () => void
  onSelectTab: (tabKey: string) => void
  onPrimaryAction?: () => void
  onSecondaryAction?: () => void
}

export function CommunityHero({
  eyebrow,
  title,
  searchValue,
  searchButtonLabel,
  tabs,
  activeTab,
  primaryActionLabel,
  secondaryActionLabel,
  isBusy = false,
  onSearchChange,
  onSearchSubmit,
  onSelectTab,
  onPrimaryAction,
  onSecondaryAction,
}: CommunityHeroProps) {
  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{eyebrow}</p>
          <h1 className="m-0 text-3xl font-bold text-slate-950">{title}</h1>
        </div>
        <div className="flex flex-wrap items-center gap-3 flex flex-wrap items-center gap-3">
          {secondaryActionLabel && onSecondaryAction ? (
            <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onSecondaryAction}>
              {secondaryActionLabel}
            </button>
          ) : null}
          {primaryActionLabel && onPrimaryAction ? (
            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={onPrimaryAction}>
              {primaryActionLabel}
            </button>
          ) : null}
        </div>
      </div>

      <div className="grid gap-2">
        <input
          value={searchValue}
          onChange={event => onSearchChange(event.target.value)}
          className="min-h-11 border border-slate-200 bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-black focus:ring-2 focus:ring-slate-200"
        />
        <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={onSearchSubmit}>
          {searchButtonLabel}
        </button>
      </div>

      <div className="flex flex-wrap gap-3" role="tablist" aria-label={title}>
        {tabs.map(tab => (
          <button
            key={tab.key}
            type="button"
            className={tab.key === activeTab ? 'inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 border-black bg-black text-white' : 'inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'}
            onClick={() => onSelectTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>
    </section>
  )
}
