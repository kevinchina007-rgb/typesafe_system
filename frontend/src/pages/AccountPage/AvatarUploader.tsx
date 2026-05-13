import { useRef, useState } from 'react'

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { UserResponse } from '@/lib/mvp-types/index'

type AvatarUploaderProps = {
  account: UserResponse
  isBusy: boolean
  translate: (translationKey: string) => string
  onUploadAvatar: (avatarFile: File) => Promise<void>
  onValidationError: (message: string) => void
}

const maximumAvatarBytes = 2 * 1024 * 1024
const allowedAvatarMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg'])

const secondaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center rounded-xl border border-slate-600/70 bg-white/5 px-4 py-2 text-sm font-semibold text-slate-100 transition hover:border-cyan-300/50 hover:bg-cyan-300/10 disabled:cursor-not-allowed disabled:opacity-55'

const primaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center rounded-xl border border-cyan-300/40 bg-cyan-300 px-4 py-2 text-sm font-semibold text-slate-950 shadow-lg shadow-cyan-950/20 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:opacity-55'

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
    <div className="grid items-center gap-4 rounded-2xl border border-white/10 bg-white/5 p-4 sm:grid-cols-[auto_1fr]">
      <div className="h-22 w-22">
        {account.avatarUrl ? (
          <BackendAssetImage
            className="h-22 w-22 rounded-full border-2 border-slate-500/50 object-cover"
            assetUrl={account.avatarUrl}
            alt={translate('account.avatar')}
          />
        ) : (
          <div
            className="grid h-22 w-22 place-items-center rounded-full bg-gradient-to-br from-cyan-300 to-blue-500 text-3xl font-bold text-slate-950"
            aria-label={translate('account.avatar')}
          >
            {avatarFallbackLabel}
          </div>
        )}
      </div>

      <div className="grid gap-3">
        <div>
          <span className="mb-1 block text-sm text-slate-400">{translate('account.avatar')}</span>
          <strong className="text-slate-50">
            {selectedAvatarFile?.name ??
              (account.avatarUrl ? translate('account.avatarUploadedState') : translate('account.avatarEmpty'))}
          </strong>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <input
            ref={hiddenFileInputRef}
            type="file"
            accept=".png,.jpg,.jpeg,image/png,image/jpeg"
            className="hidden"
            onChange={event => handleSelectedFile(event.target.files?.[0] ?? null)}
          />
          <button
            type="button"
            className={secondaryButtonClassName}
            disabled={isBusy}
            onClick={() => hiddenFileInputRef.current?.click()}
          >
            {translate('account.avatarSelect')}
          </button>
          <button
            type="button"
            className={primaryButtonClassName}
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
    </div>
  )
}
