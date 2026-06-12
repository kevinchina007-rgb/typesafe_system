// 本文件定义顶部导航按钮，负责在壳层导航区触发页面跳转。

import type { AppViewKey } from '@/lib/mvp-types/index'
import type { AppIcon } from '@/app/icons/Icons'
import { Icon } from '@/app/icons/Icons'

type TopNavButtonProps = {
  badgeCount?: number
  icon: AppIcon
  isActive: boolean
  label: string
  targetViewKey: AppViewKey
  onSelect: (viewKey: AppViewKey) => void
  isOverlay?: boolean
}

export function TopNavButton({ badgeCount, icon, isActive, label, targetViewKey, onSelect, isOverlay = false }: TopNavButtonProps) {
  const buttonClassName = isOverlay
    ? `relative inline-flex h-11 min-w-28 items-center justify-center gap-2 !rounded-none !border-0 ![background:transparent] ![box-shadow:none] ![transform:none] px-4 !text-white outline-none ring-0 transition-colors focus:outline-none focus:ring-0 focus-visible:outline-none focus-visible:ring-0 hover:![background:rgba(255,255,255,0.16)] hover:![box-shadow:none] hover:![transform:none] ${
        isActive ? '' : ''
      }`
    : `relative inline-flex h-11 min-w-28 items-center justify-center gap-2 !rounded-none !border !border-transparent ![background:transparent] ![box-shadow:none] ![transform:none] px-4 !text-slate-950 outline-none ring-0 transition-colors focus:outline-none focus:ring-0 focus-visible:outline-none focus-visible:ring-0 hover:!border-black hover:![background:#000] hover:!text-white hover:![box-shadow:none] hover:![transform:none] ${
        isActive ? 'font-semibold' : ''
      }`

  return (
    <button
      type="button"
      className={buttonClassName}
      onClick={() => onSelect(targetViewKey)}
    >
      <span className="inline-flex min-w-0 items-center justify-center gap-2">
        <Icon icon={icon} size={18} />
        <span className="overflow-hidden text-ellipsis whitespace-nowrap">{label}</span>
      </span>
      {badgeCount && badgeCount > 0 ? (
        <span className="absolute -right-1 -top-1 grid h-5 min-w-5 place-items-center bg-rose-500 px-1 text-xs text-white">
          {badgeCount}
        </span>
      ) : null}
    </button>
  )
}
