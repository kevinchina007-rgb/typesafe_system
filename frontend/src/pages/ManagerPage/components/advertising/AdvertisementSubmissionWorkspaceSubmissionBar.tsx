export type AdvertisementSubmissionWorkspaceSubmissionBarProps = {
  controller: Record<string, unknown>
}

export function AdvertisementSubmissionWorkspaceSubmissionBar({ controller }: AdvertisementSubmissionWorkspaceSubmissionBarProps) {
  const c = controller as any
  return (
            <div className="flex flex-wrap gap-3">
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white disabled:opacity-50" disabled={c.isLoading || c.isSubmitting || c.creative.elements.length === 0} onClick={() => {
                if (!c.draftName.trim()) {
                  c.setIsDraftNameDialogOpen(true)
                  return
                }
                void c.saveDraft(false)
              }}>保存草稿</button>
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-5 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600 disabled:opacity-50" disabled={c.isLoading || c.isSubmitting || c.creative.elements.length === 0} onClick={() => {
                if (!c.draftName.trim()) {
                  c.setIsDraftNameDialogOpen(true)
                  return
                }
                void c.saveDraft(true)
              }}>{c.isSubmitting ? c.translate('advertising.submitting') : '提交审核'}</button>
            </div>
  )
}
