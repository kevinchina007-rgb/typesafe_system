import type { UserResponse } from '@/lib/mvp-types/index'
import { AvatarUploader } from '../../AvatarUploader'
import type { AccountPageController } from '../../objects'
import { cardClassName, heroCardClassName, inputClassName, labelClassName, primaryButtonClassName, secondaryButtonClassName } from '../../functions'
import { DetailField } from '../shared/DetailField'
import { SectionHeader } from '../shared/SectionHeader'

type AccountSignedInSectionProps = {
  account: UserResponse
  controller: AccountPageController
  translate: (translationKey: string) => string
}

// 已登录状态下的账号信息区域。
export function AccountSignedInSection({ account, controller, translate }: AccountSignedInSectionProps) {
  const {
    isBusy,
    isProfileEditing,
    profileDraft,
    setIsProfileEditing,
    setProfileDraft,
    onUploadAvatar,
    onUseDefaultAvatar,
    onAvatarValidationError,
    onUpdateProfile,
    onValidationError,
    isChangePasswordOpen,
    setIsChangePasswordOpen,
    onChangePassword,
    onLogoutCurrentSession,
    onLogout,
  } = controller

  return (
    <section className="mx-auto grid w-full gap-4 lg:w-3/4">
      <section className={heroCardClassName}>
        <div className="grid gap-6 lg:grid-cols-[7rem_minmax(0,1fr)]">
          <AvatarUploader
            account={account}
            isBusy={isBusy}
            translate={translate}
            onUploadAvatar={onUploadAvatar}
            onUseDefaultAvatar={onUseDefaultAvatar}
            onValidationError={onAvatarValidationError}
          />

          <div className="grid gap-4">
            <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
              <div className="grid gap-4 md:grid-cols-2">
                <DetailField label={translate('account.nickname')}>{account.nickname}</DetailField>
                <DetailField label={translate('account.email')}>{account.email}</DetailField>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={() => setIsProfileEditing(editing => !editing)}>
                  {translate('account.editProfile')}
                </button>
                <button className={primaryButtonClassName} type="button" disabled={isBusy} onClick={onLogoutCurrentSession}>
                  {translate('account.logoutCurrentSession')}
                </button>
              </div>
            </div>

            {isProfileEditing ? (
              <form
                className="grid gap-4 border border-slate-200 bg-slate-50 p-4"
                onSubmit={async event => {
                  event.preventDefault()
                  if (!profileDraft.nickname.trim()) {
                    onValidationError('昵称不能为空。')
                    return
                  }
                  await onUpdateProfile({
                    nickname: profileDraft.nickname.trim(),
                    phone: account.phone,
                  })
                  setIsProfileEditing(false)
                }}
              >
                <label className={labelClassName}>
                  {translate('account.nickname')}
                  <input
                    className={inputClassName}
                    value={profileDraft.nickname}
                    onChange={event => setProfileDraft(current => ({ ...current, nickname: event.target.value }))}
                  />
                </label>
                <div className="flex flex-wrap gap-3">
                  <button className={primaryButtonClassName} type="submit" disabled={isBusy}>
                    {translate('account.saveProfile')}
                  </button>
                  <button
                    className={secondaryButtonClassName}
                    type="button"
                    disabled={isBusy}
                    onClick={() => {
                      setProfileDraft({ nickname: account.nickname ?? '', phone: account.phone ?? '' })
                      setIsProfileEditing(false)
                    }}
                  >
                    {translate('account.cancelEdit')}
                  </button>
                </div>
              </form>
            ) : null}
          </div>
        </div>
      </section>

      <section className={cardClassName}>
        <SectionHeader
          eyebrow={translate('account.security')}
          title={translate('account.accountSecurity')}
          actions={
            <>
              <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={() => setIsChangePasswordOpen(open => !open)}>
                {translate(isChangePasswordOpen ? 'account.hideChangePassword' : 'account.showChangePassword')}
              </button>
              <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={onLogout}>
                {translate('account.logout')}
              </button>
            </>
          }
        />

        {isChangePasswordOpen ? (
          <form
            className="grid gap-4"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              const currentPassword = String(formData.get('currentPassword') ?? '')
              const newPassword = String(formData.get('newPassword') ?? '')
              const confirmPassword = String(formData.get('confirmPassword') ?? '')
              if (newPassword !== confirmPassword) {
                    onValidationError('昵称不能为空。')
                return
              }
              await onChangePassword({ currentPassword, newPassword })
              event.currentTarget.reset()
              setIsChangePasswordOpen(false)
            }}
          >
            <label className={labelClassName}>
              {translate('account.currentPassword')}
              <input className={inputClassName} name="currentPassword" type="password" required />
            </label>
            <label className={labelClassName}>
              {translate('account.newPassword')}
              <input className={inputClassName} name="newPassword" type="password" required />
            </label>
            <label className={labelClassName}>
              {translate('account.confirmPassword')}
              <input className={inputClassName} name="confirmPassword" type="password" required />
            </label>
            <button className={primaryButtonClassName} type="submit" disabled={isBusy}>
              {translate('account.changePassword')}
            </button>
          </form>
        ) : null}
      </section>
    </section>
  )
}
