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
}

export function TopNavButton({ badgeCount, icon, isActive, label, targetViewKey, onSelect }: TopNavButtonProps) {
  return (
    <button
      type="button"
      className={`top-nav-button ${isActive ? 'is-active' : ''}`}
      onClick={() => onSelect(targetViewKey)}
    >
      <span className="top-nav-button-inner">
        <Icon icon={icon} size={18} />
        <span className="top-nav-button-label">{label}</span>
      </span>
      {badgeCount && badgeCount > 0 ? <span className="top-nav-button-badge">{badgeCount}</span> : null}
    </button>
  )
}
