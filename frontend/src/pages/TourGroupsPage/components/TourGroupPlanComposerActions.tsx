export function TourGroupPlanComposerActions({
  isBusy,
  translate,
}: {
  isBusy: boolean
  translate: (translationKey: string) => string
}) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <button
        className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
        type="submit"
        disabled={isBusy}
      >
        {translate('tourGroups.searchOptions')}
      </button>
    </div>
  )
}
