import type { UserResponse } from '@/lib/mvp-types/index'
import type { AccountProfileDraft } from '../objects'

// 头像上传允许的最大大小。
export const maximumAvatarBytes = 2 * 1024 * 1024
// 头像上传允许的图片类型。
export const allowedAvatarMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg'])

// 默认头像候选列表。
export const defaultAvatarOptions = Array.from({ length: 8 }, (_, index) => {
  const avatarIndex = index + 1
  return {
    id: `bara-avatar-${avatarIndex}`,
    src: `/images/avatar-defaults/bara-avatar-${avatarIndex}.png`,
  }
})

// 账号页主卡片样式。
export const cardClassName = 'grid gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60'
// 账号入口卡片样式。
export const accountEntryCardClassName = 'mx-auto grid w-full gap-4 self-start border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60 md:w-1/2 md:max-w-3xl'
// 账号页头像区样式。
export const heroCardClassName = 'grid gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60'
// 主要操作按钮样式。
export const primaryButtonClassName = 'inline-flex min-h-11 items-center justify-center !border !border-black ![background:#000] px-4 py-2 text-sm font-semibold !text-white !shadow-none ![transform:none] transition hover:![background:#000] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
// 次要操作按钮样式。
export const secondaryButtonClassName = 'inline-flex min-h-11 items-center justify-center !border !border-slate-300 ![background:transparent] px-4 py-2 text-sm font-semibold !text-slate-950 !shadow-none ![transform:none] transition hover:!border-black hover:![background:#000] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
// 登录注册的强调按钮样式。
export const authPrimaryButtonClassName = 'inline-flex min-h-11 items-center justify-center !border !border-pink-500 ![background:#ec4899] px-4 py-2 text-sm font-semibold !text-white !shadow-none ![transform:none] transition hover:![background:#db2777] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
// 表单标签样式。
export const labelClassName = 'grid gap-2 text-sm font-medium text-slate-700'
// 表单输入框样式。
export const inputClassName = 'min-h-11 border border-slate-300 bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-black focus:ring-2 focus:ring-slate-200'

// 获取账号显示时用的头像首字母。
export function getAvatarFallbackLabel(account?: UserResponse | null) {
  return account?.nickname?.trim().slice(0, 1).toUpperCase() || 'U'
}

// 根据当前用户生成资料编辑草稿。
export function createProfileDraft(account?: UserResponse | null): AccountProfileDraft {
  return {
    nickname: account?.nickname ?? '',
    phone: account?.phone ?? '',
  }
}
