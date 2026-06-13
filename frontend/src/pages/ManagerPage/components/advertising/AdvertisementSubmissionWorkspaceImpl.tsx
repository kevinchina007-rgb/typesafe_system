import { AdvertisementDraftSection } from './AdvertisementSubmissionWorkspaceSupport'
import { AdvertisementSubmissionWorkspaceCreatePanel } from './AdvertisementSubmissionWorkspaceCreatePanel'
import { AdvertisementSubmissionWorkspaceDraftDialog } from './AdvertisementSubmissionWorkspaceDraftDialog'
import { useAdvertisementSubmissionWorkspaceController } from './useAdvertisementSubmissionWorkspaceController'
import type { AdvertisementSubmissionWorkspaceProps } from './AdvertisementSubmissionWorkspaceSupport'

export function AdvertisementSubmissionWorkspace({
  defaultPlacement,
  defaultTargetResourceType,
  resourceOptions,
  translate,
  onOpenResource,
  onShowNotice,
}: AdvertisementSubmissionWorkspaceProps) {
  const {
    controller,
    workspaceTab,
    setWorkspaceTab,
    draftName,
    draftNameInput,
    setDraftNameInput,
    isDraftNameDialogOpen,
    setIsDraftNameDialogOpen,
    draftAdvertisements,
    openAdvertisementDraft,
    withdrawAdvertisement,
    submitDraftAdvertisement,
    confirmNewDraft,
    onOpenResource: openResource,
  } = useAdvertisementSubmissionWorkspaceController({
    defaultPlacement,
    defaultTargetResourceType,
    resourceOptions,
    translate,
    onOpenResource,
    onShowNotice,
  })

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.submitEyebrow')}</p>
          <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.submitTitle')}</h2>
          {workspaceTab === 'create' && draftName ? <p className="m-0 mt-2 text-sm font-semibold text-slate-500">当前草稿：{draftName}</p> : null}
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            className={`inline-flex min-h-11 items-center justify-center border px-4 py-2 text-sm font-semibold transition ${workspaceTab === 'create' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-300 bg-white text-slate-950'}`}
            onClick={() => setWorkspaceTab('create')}
          >
            创建
          </button>
          <button
            type="button"
            className={`inline-flex min-h-11 items-center justify-center border px-4 py-2 text-sm font-semibold transition ${workspaceTab === 'drafts' ? 'border-slate-950 bg-slate-950 text-white' : 'border-slate-300 bg-white text-slate-950'}`}
            onClick={() => setWorkspaceTab('drafts')}
          >
            查看草稿
          </button>
        </div>

        {workspaceTab === 'create' ? <AdvertisementSubmissionWorkspaceCreatePanel controller={controller} /> : null}
      </section>

      {isDraftNameDialogOpen ? (
        <AdvertisementSubmissionWorkspaceDraftDialog
          draftNameInput={draftNameInput}
          confirmNewDraft={confirmNewDraft}
          setDraftNameInput={setDraftNameInput}
          setIsDraftNameDialogOpen={setIsDraftNameDialogOpen}
        />
      ) : null}

      {workspaceTab === 'drafts' ? (
        <AdvertisementDraftSection
          advertisements={draftAdvertisements}
          translate={translate}
          onEdit={openAdvertisementDraft}
          onOpenResource={openResource}
          onWithdraw={withdrawAdvertisement}
          onSubmitReview={submitDraftAdvertisement}
        />
      ) : null}
    </section>
  )
}
