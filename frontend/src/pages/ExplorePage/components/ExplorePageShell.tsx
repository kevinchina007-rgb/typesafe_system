import type { ExplorePageController, ExplorePageProps } from '../objects'
import { EXPLORE_SEARCH_TYPES } from '../objects'
import { targetViewFromResult } from '../functions'

type ExplorePageShellProps = {
  controller: ExplorePageController
  pageProps: ExplorePageProps
}

export function ExplorePageShell({ controller, pageProps }: ExplorePageShellProps) {
  const { translate, onOpenView } = pageProps
  const { groupedResults, isLoadingSuggestions, isSearching, results, searchDraft, searchType, suggestions } = controller

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <section className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('nav.explore')}</p>
            <h2>{translate('explore.title')}</h2>
          </div>
        </div>

        <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('explore.description')}</p>

        <section className="grid gap-3">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">搜索类型区</p>
          <div className="flex flex-wrap items-center gap-3">
            {EXPLORE_SEARCH_TYPES.map(tabValue => (
              <button
                key={tabValue}
                type="button"
                className={searchType === tabValue ? '' : 'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'}
                onClick={() => controller.setSearchType(tabValue)}
              >
                {translate(`search.type.${tabValue}`)}
              </button>
            ))}
          </div>
        </section>

        <section className="grid gap-3">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">搜索输入区</p>
          <label className="grid gap-2 text-sm font-semibold text-slate-700">
            <span>{translate('search.global.label')}</span>
            <input
              value={searchDraft}
              onChange={event => controller.setSearchDraft(event.target.value)}
            />
          </label>
          <button
            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
            type="button"
            disabled={isSearching}
            onClick={controller.runSearch}
          >
            {translate('search.confirm')}
          </button>
        </section>
      </section>

      <section className="grid gap-4">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">建议区</p>
        {suggestions.length > 0 || isLoadingSuggestions ? (
          <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <p className="text-sm font-bold text-slate-500">{translate('search.suggestions')}</p>
            {isLoadingSuggestions && suggestions.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('search.loading')}</p> : null}
            <ul className="grid gap-3">
              {suggestions.map(suggestion => (
                <li key={`${suggestion.resourceType}:${suggestion.value}`}>
                  <button
                    type="button"
                    className="inline-flex items-center justify-center text-sm font-bold text-sky-600 underline-offset-4 hover:underline"
                    onClick={() => controller.applySuggestion(suggestion)}
                  >
                    <strong>{suggestion.title}</strong>
                  </button>
                  <p>{suggestion.subtitle}</p>
                </li>
              ))}
            </ul>
          </section>
        ) : null}
      </section>

      <section className="grid gap-4">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">结果区</p>
        {searchDraft.trim().length === 0 ? (
          <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <p className="text-sm leading-6 text-slate-500">{translate('search.emptyHint')}</p>
          </section>
        ) : null}

        {searchDraft.trim().length > 0 && results.length === 0 && !isSearching ? (
          <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <p className="text-sm leading-6 text-slate-500">{translate('search.emptyResults')}</p>
          </section>
        ) : null}

        {Object.entries(groupedResults).map(([resourceType, groupedItems]) => (
          <section key={resourceType} className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <div className="text-lg font-bold text-slate-950">
              <div>
                <p className="text-sm font-bold text-slate-500">{translate(`search.type.${resourceType}`)}</p>
                <h3>{translate('search.resultGroupTitle')}</h3>
              </div>
            </div>

            <ul className="grid gap-3">
              {groupedItems.map(result => (
                <li key={`${result.resourceType}:${result.resourceId}`}>
                  <div className="search-result-card">
                    <div>
                      <strong>{result.title}</strong>
                      <p>{result.summary}</p>
                      <p>{result.metaLabel}</p>
                    </div>
                    <div className="flex flex-wrap items-center gap-3">
                      <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" onClick={() => onOpenView(targetViewFromResult(result.resourceType))}>
                        {translate('search.openResult')}
                      </button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </section>
        ))}
      </section>
    </section>
  )
}
