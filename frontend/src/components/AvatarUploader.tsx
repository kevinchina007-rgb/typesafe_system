import { useRef, useState } from 'react'

import type { UserResponse } from '../lib/mvp-types'
import { BackendAssetImage } from './BackendAssetImage'

type AvatarUploaderProps = {
  account: UserResponse
  isBusy: boolean
  translate: (translationKey: string) => string
  onUploadAvatar: (avatarFile: File) => Promise<void>
  onValidationError: (message: string) => void
}

const maximumAvatarBytes = 2 * 1024 * 1024
const allowedAvatarMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg'])

export function AvatarUploader({
  account,
  isBusy,
  translate,
  onUploadAvatar,
  onValidationError,
}: AvatarUploaderProps) {
  const [selectedAvatarFile, setSelectedAvatarFile] = useState<File | null>(null)
  const hiddenFileInputRef = useRef<HTMLInputElement | null>(null)

  const avatarFallbackLabel = account.nickname.trim().slice(0, 1).toUpperCase() || 'U'

  function handleSelectedFile(nextAvatarFile: File | null) {
    if (!nextAvatarFile) {
      setSelectedAvatarFile(null)
      return
    }

    if (!allowedAvatarMimeTypes.has(nextAvatarFile.type)) {
      onValidationError(translate('error.avatarType'))
      return
    }

    if (nextAvatarFile.size > maximumAvatarBytes) {
      onValidationError(translate('error.avatarTooLarge'))
      return
    }

    setSelectedAvatarFile(nextAvatarFile)
  }

  return (
    <div className="avatar-panel">
      <div className="avatar-preview-shell">
        {account.avatarUrl ? (
          <BackendAssetImage className="avatar-preview-image" assetUrl={account.avatarUrl} alt={translate('account.avatar')} />
        ) : (
          <div className="avatar-preview-fallback" aria-label={translate('account.avatar')}>
            {avatarFallbackLabel}
          </div>
        )}
      </div>

      <div className="avatar-panel-copy">
        <span className="detail-label">{translate('account.avatar')}</span>
        <strong>
          {selectedAvatarFile?.name ??
            (account.avatarUrl ? translate('account.avatarUploadedState') : translate('account.avatarEmpty'))}
        </strong>
        <p className="hero-copy">{translate('account.avatarHint')}</p>
      </div>

      <div className="action-cluster">
        <input
          ref={hiddenFileInputRef}
          type="file"
          accept=".png,.jpg,.jpeg,image/png,image/jpeg"
          className="hidden-file-input"
          onChange={event => handleSelectedFile(event.target.files?.[0] ?? null)}
        />
        <button
          type="button"
          className="secondary-button"
          disabled={isBusy}
          onClick={() => hiddenFileInputRef.current?.click()}
        >
          {translate('account.avatarSelect')}
        </button>
        <button
          type="button"
          disabled={isBusy || !selectedAvatarFile}
          onClick={async () => {
            if (!selectedAvatarFile) {
              onValidationError(translate('error.avatarMissing'))
              return
            }
            await onUploadAvatar(selectedAvatarFile)
            setSelectedAvatarFile(null)
            if (hiddenFileInputRef.current) {
              hiddenFileInputRef.current.value = ''
            }
          }}
        >
          {translate('account.avatarUpload')}
        </button>
      </div>
    </div>
  )
}
