import { useRef, useState } from 'react'
import { Camera, X } from 'lucide-react'

import type { UserResponse } from '@/lib/mvp-types/index'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

type AvatarUploaderProps = {
  account: UserResponse
  isBusy: boolean
  translate: (translationKey: string) => string
  onUploadAvatar: (avatarFile: File) => Promise<void>
  onUseDefaultAvatar: (avatarUrl: string) => Promise<void>
  onValidationError: (message: string) => void
}

// 头像上传允许的最大字节数。
const maximumAvatarBytes = 2 * 1024 * 1024
// 头像上传允许的 MIME 类型。
const allowedAvatarMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg'])

// 默认头像候选项列表。
const defaultAvatarOptions = Array.from({ length: 8 }, (_, index) => {
  const avatarIndex = index + 1
  return {
    id: `bara-avatar-${avatarIndex}`,
    src: `/images/avatar-defaults/bara-avatar-${avatarIndex}.png`,
  }
})

// 次要按钮样式。
const secondaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'

// 主要按钮样式。
const primaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55'

// 头像上传和默认头像选择弹窗。
export function AvatarUploader({
  account,
  isBusy,
  translate,
  onUploadAvatar,
  onUseDefaultAvatar,
  onValidationError,
}: AvatarUploaderProps) {
  const [selectedAvatarFile, setSelectedAvatarFile] = useState<File | null>(null)
  const [isAvatarDialogOpen, setIsAvatarDialogOpen] = useState(false)
  const hiddenFileInputRef = useRef<HTMLInputElement | null>(null)

  // 头像缺省展示用的首字母。
  const avatarFallbackLabel = account.nickname.trim().slice(0, 1).toUpperCase() || 'U'

  // 清空当前选择的本地文件。
  function resetSelectedFile() {
    setSelectedAvatarFile(null)
    if (hiddenFileInputRef.current) {
      hiddenFileInputRef.current.value = ''
    }
  }

  // 关闭头像弹窗并恢复初始状态。
  function closeAvatarDialog() {
    resetSelectedFile()
    setIsAvatarDialogOpen(false)
  }

  // 处理用户选择的头像文件。
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

  // 上传某个默认头像。
  async function uploadDefaultAvatar(src: string) {
    await onUseDefaultAvatar(src)
    closeAvatarDialog()
  }

  // 上传当前选择的本地头像文件。
  async function uploadSelectedAvatar() {
    if (!selectedAvatarFile) {
      onValidationError(translate('error.avatarMissing'))
      return
    }
    await onUploadAvatar(selectedAvatarFile)
    closeAvatarDialog()
  }

  const avatarPreview = account.avatarUrl ? (
    <BackendAssetImage
      className="h-full w-full bg-white object-contain"
      assetUrl={account.avatarUrl}
      alt={translate('account.avatar')}
    />
  ) : (
    <span className="grid h-full w-full place-items-center bg-sky-100 text-3xl font-bold text-slate-950">
      {avatarFallbackLabel}
    </span>
  )

  return (
    <div className="grid gap-4">
      <button
        type="button"
        className="group relative h-24 w-24 overflow-hidden border-2 border-slate-200 bg-white p-0 text-left transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-pink-500 disabled:cursor-not-allowed disabled:opacity-60"
        disabled={isBusy}
        onClick={() => setIsAvatarDialogOpen(true)}
        aria-label={translate('account.changeAvatar')}
        title={translate('account.changeAvatar')}
      >
        {avatarPreview}
        <span className="absolute inset-0 grid place-items-center bg-black/0 transition group-hover:bg-black/35 group-focus-visible:bg-black/35">
          <span className="grid h-9 w-11 place-items-center border-2 border-white bg-black/25 opacity-0 transition group-hover:opacity-100 group-focus-visible:opacity-100">
            <Camera className="h-5 w-5 text-white" strokeWidth={2.4} />
          </span>
        </span>
      </button>

      {isAvatarDialogOpen ? (
        <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 px-4">
          <div className="w-full max-w-2xl border border-slate-200 bg-white p-6 shadow-2xl">
            <div className="mb-5 flex justify-end">
              <button
                type="button"
                className="grid h-11 w-11 place-items-center border border-slate-300 bg-white text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
                onClick={closeAvatarDialog}
                aria-label={translate('account.closeAvatarDialog')}
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="grid gap-6">
              <section className="grid gap-3">
                <h3 className="text-lg font-bold text-slate-950">{translate('account.defaultAvatars')}</h3>
                <div className="grid grid-cols-4 gap-3 sm:grid-cols-8">
                  {defaultAvatarOptions.map(option => (
                    <button
                      key={option.id}
                      type="button"
                      className="grid h-16 w-16 place-items-center border border-slate-200 bg-white p-1 transition hover:border-pink-500 disabled:cursor-not-allowed disabled:opacity-55"
                      disabled={isBusy}
                      onClick={() => void uploadDefaultAvatar(option.src)}
                      title={translate('account.useDefaultAvatar')}
                    >
                      <img
                        src={option.src}
                        alt={translate('account.useDefaultAvatar')}
                        className="h-full w-full object-contain"
                      />
                    </button>
                  ))}
                </div>
              </section>

              <section className="grid gap-3 border-t border-slate-200 pt-5">
                <h3 className="text-lg font-bold text-slate-950">{translate('account.localAvatar')}</h3>
                <input
                  ref={hiddenFileInputRef}
                  type="file"
                  accept=".png,.jpg,.jpeg,image/png,image/jpeg"
                  className="hidden"
                  onChange={event => handleSelectedFile(event.target.files?.[0] ?? null)}
                />
                <div className="flex flex-wrap items-center gap-3">
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
                    onClick={() => void uploadSelectedAvatar()}
                  >
                    {translate('account.avatarUpload')}
                  </button>
                </div>
                <span className="text-sm font-semibold text-slate-600">
                  {selectedAvatarFile?.name ?? translate('account.avatarEmpty')}
                </span>
              </section>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}
