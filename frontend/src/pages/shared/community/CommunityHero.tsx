type CommunityTab = {
  key: string
  label: string
}

type CommunityHeroProps = {
  eyebrow: string
  title: string
  searchValue: string
  searchPlaceholder: string
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
  searchPlaceholder,
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
    <section className="page-card community-hero">
      <div className="community-hero-head">
        <div>
          <p className="eyebrow-label">{eyebrow}</p>
          <h1 className="community-hero-title">{title}</h1>
        </div>
        <div className="action-row community-hero-actions">
          {secondaryActionLabel && onSecondaryAction ? (
            <button type="button" className="secondary-button" disabled={isBusy} onClick={onSecondaryAction}>
              {secondaryActionLabel}
            </button>
          ) : null}
          {primaryActionLabel && onPrimaryAction ? (
            <button type="button" disabled={isBusy} onClick={onPrimaryAction}>
              {primaryActionLabel}
            </button>
          ) : null}
        </div>
      </div>

      <div className="community-search-shell">
        <input
          value={searchValue}
          onChange={event => onSearchChange(event.target.value)}
          placeholder={searchPlaceholder}
          className="community-search-input"
        />
        <button type="button" disabled={isBusy} onClick={onSearchSubmit}>
          {searchButtonLabel}
        </button>
      </div>

      <div className="community-filter-tabs" role="tablist" aria-label={title}>
        {tabs.map(tab => (
          <button
            key={tab.key}
            type="button"
            className={tab.key === activeTab ? 'community-filter-tab is-active' : 'community-filter-tab secondary-button'}
            onClick={() => onSelectTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>
    </section>
  )
}
