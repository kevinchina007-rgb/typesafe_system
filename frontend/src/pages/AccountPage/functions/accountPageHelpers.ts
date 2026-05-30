import type { UserResponse } from '@/lib/mvp-types/index'
import type { AccountProfileDraft } from '../objects'

export const maximumAvatarBytes = 2 * 1024 * 1024
export const allowedAvatarMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg'])

export const defaultAvatarOptions = Array.from({ length: 8 }, (_, index) => {
  const avatarIndex = index + 1
  return {
    id: `bara-avatar-${avatarIndex}`,
    src: `/images/avatar-defaults/bara-avatar-${avatarIndex}.png`,
  }
})

export const cardClassName = 'grid gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60'
export const accountEntryCardClassName = 'mx-auto grid w-full gap-4 self-start border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60 md:w-1/2 md:max-w-3xl'
export const heroCardClassName = 'grid gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60'
export const primaryButtonClassName = 'inline-flex min-h-11 items-center justify-center !border !border-black ![background:#000] px-4 py-2 text-sm font-semibold !text-white !shadow-none ![transform:none] transition hover:![background:#000] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
export const secondaryButtonClassName = 'inline-flex min-h-11 items-center justify-center !border !border-slate-300 ![background:transparent] px-4 py-2 text-sm font-semibold !text-slate-950 !shadow-none ![transform:none] transition hover:!border-black hover:![background:#000] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
export const authPrimaryButtonClassName = 'inline-flex min-h-11 items-center justify-center !border !border-pink-500 ![background:#ec4899] px-4 py-2 text-sm font-semibold !text-white !shadow-none ![transform:none] transition hover:![background:#db2777] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
export const labelClassName = 'grid gap-2 text-sm font-medium text-slate-700'
export const inputClassName = 'min-h-11 border border-slate-300 bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-black focus:ring-2 focus:ring-slate-200'

export function getAvatarFallbackLabel(account?: UserResponse | null) {
  return account?.nickname?.trim().slice(0, 1).toUpperCase() || 'U'
}

export function createProfileDraft(account?: UserResponse | null): AccountProfileDraft {
  return {
    nickname: account?.nickname ?? '',
    phone: account?.phone ?? '',
  }
}
