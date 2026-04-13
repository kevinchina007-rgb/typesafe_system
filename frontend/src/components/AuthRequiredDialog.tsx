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
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="modal-card auth-required-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={event => event.stopPropagation()}
      >
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('authRequired.eyebrow')}</p>
            <h3>{title}</h3>
          </div>
          <button type="button" className="secondary-button modal-close-button" onClick={onClose}>
            ×
          </button>
        </div>

        <p className="hero-copy">{description}</p>

        <div className="action-row">
          <button type="button" onClick={onConfirm}>
            {translate('authRequired.goToLogin')}
          </button>
          <button type="button" className="secondary-button" onClick={onClose}>
            {translate('tourGroups.cancel')}
          </button>
        </div>
      </div>
    </div>
  )
}
