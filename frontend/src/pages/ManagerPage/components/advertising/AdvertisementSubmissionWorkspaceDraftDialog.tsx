export type AdvertisementSubmissionWorkspaceDraftDialogProps = {
  draftNameInput: string
  confirmNewDraft: () => void
  setDraftNameInput: (value: string) => void
  setIsDraftNameDialogOpen: (value: boolean) => void
}

export function AdvertisementSubmissionWorkspaceDraftDialog({
  draftNameInput,
  confirmNewDraft,
  setDraftNameInput,
  setIsDraftNameDialogOpen,
}: AdvertisementSubmissionWorkspaceDraftDialogProps) {
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/40 px-4">
      <section className="grid w-full max-w-md gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl">
        <div className="grid gap-1">
          <strong className="text-xl">新建草稿名称</strong>
          <span className="text-sm leading-6 text-slate-500">这个名称只显示在草稿列表里，也可以作为广告的本地标题。</span>
        </div>
        <label className="grid gap-2">
          <span className="text-sm font-semibold text-slate-700">草稿名称</span>
          <input
            autoFocus
            value={draftNameInput}
            onChange={event => setDraftNameInput(event.target.value)}
            onKeyDown={event => {
              if (event.key === 'Enter') {
                confirmNewDraft()
              }
            }}
            maxLength={120}
            placeholder="例如：沪航暑期特惠"
          />
        </label>
        <div className="flex flex-wrap justify-end gap-3">
          <button type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" onClick={() => setIsDraftNameDialogOpen(false)}>取消</button>
          <button type="button" className="inline-flex min-h-10 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600" onClick={confirmNewDraft}>开始创建</button>
        </div>
      </section>
    </div>
  )
}
