import { AdvertisementSubmissionWorkspaceCanvasEditor } from './AdvertisementSubmissionWorkspaceCanvasEditor'
import { AdvertisementSubmissionWorkspaceFlightSearchPanel } from './AdvertisementSubmissionWorkspaceFlightSearchPanel'
import { AdvertisementSubmissionWorkspaceSubmissionBar } from './AdvertisementSubmissionWorkspaceSubmissionBar'

export type AdvertisementSubmissionWorkspaceCreatePanelProps = {
  controller: Record<string, unknown>
}

export function AdvertisementSubmissionWorkspaceCreatePanel({ controller }: AdvertisementSubmissionWorkspaceCreatePanelProps) {
  const c = controller as any
  return (
    <div className="grid gap-5">
      <section className="grid gap-4 border border-slate-200 bg-slate-50 p-4">
        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
            onClick={c.openNewDraftDialog}
          >
            新建草稿
          </button>
        </div>
      </section>

      <AdvertisementSubmissionWorkspaceCanvasEditor controller={c} />

      <section className="grid gap-4 border border-slate-200 bg-slate-50 p-4">
        <div className="flex items-center justify-between gap-3">
          <div className="grid gap-1">
            <strong>资源绑定</strong>
            <span className="text-sm leading-6 text-slate-500">这里控制当前广告是否绑定某个业务资源。</span>
          </div>
          <label className="inline-flex items-center gap-2 text-sm font-semibold text-slate-700">
            <input type="checkbox" checked={c.hyperlinkEnabled} onChange={event => c.setHyperlinkEnabled(event.target.checked)} />
            启用资源绑定
          </label>
        </div>

        <AdvertisementSubmissionWorkspaceFlightSearchPanel
          defaultTargetResourceType={c.defaultTargetResourceType}
          flightSearchResults={c.flightSearchResults}
          hasSearchedFlights={c.hasSearchedFlights}
          hyperlinkEnabled={c.hyperlinkEnabled}
          resourceOptions={c.resourceOptions}
          searchDraft={c.searchDraft}
          targetResourceId={c.targetResourceId}
          translate={c.translate}
          updateSearchDraftField={c.updateSearchDraftField}
          runFlightSearch={c.runFlightSearch}
          setTargetResourceId={c.setTargetResourceId}
        />
      </section>

      <AdvertisementSubmissionWorkspaceSubmissionBar controller={c} />
    </div>
  )
}
