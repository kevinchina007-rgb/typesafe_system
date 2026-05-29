type AuthRequiredDialogProps = {
  isOpen: boolean
  title: string
  description: string
  translate: (translationKey: string) => string
  onClose: () => void
  onConfirm: () => void
}

export function AuthRequiredDialog({
  isOpen,
  title,
  description,
  translate,
  onClose,
  onConfirm,
}: AuthRequiredDialogProps) {
  if (!isOpen) {
    return null
  }

  return (
    <div className="fixed inset-0 z-[70] grid place-items-center bg-slate-950/35 px-4" role="presentation" onClick={onClose}>
      <div
        className="w-full max-w-lg border border-slate-200 bg-white p-8 text-slate-950 shadow-2xl shadow-slate-950/20"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={event => event.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-6">
          <div>
            <p className="text-sm font-semibold text-slate-500">{translate('authRequired.eyebrow')}</p>
            <div className="mt-2 flex items-center gap-3">
              <h3 className="text-3xl font-bold text-slate-950">{title}</h3>
              <img
                src="/images/fly-bara-login.png"
                alt=""
                className="h-16 w-16 object-contain"
                aria-hidden="true"
              />
            </div>
          </div>
          <button
            type="button"
            className="inline-flex h-10 w-10 items-center justify-center border border-slate-300 bg-white text-xl font-semibold text-slate-900 transition hover:border-black hover:bg-black hover:text-white"
            onClick={onClose}
            aria-label={translate('tourGroups.cancel')}
          >
            x
          </button>
        </div>

        <p className="mt-6 max-w-prose text-base leading-7 text-slate-700">{description}</p>

        <div className="mt-8 flex flex-wrap gap-3">
          <button
            type="button"
            className="inline-flex min-h-12 items-center justify-center border border-pink-500 bg-pink-500 px-6 py-3 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600"
            onClick={onConfirm}
          >
            {translate('authRequired.goToLogin')}
          </button>
          <button
            type="button"
            className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-900 transition hover:border-black hover:bg-black hover:text-white"
            onClick={onClose}
          >
            {translate('tourGroups.cancel')}
          </button>
        </div>
      </div>
    </div>
  )
}
