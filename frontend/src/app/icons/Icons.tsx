// 本文件定义应用图标组件集合，供页面壳层和业务页面统一复用。

import type { ReactNode, SVGProps } from 'react'

export type AppIcon =
  | 'dashboard'
  | 'flight'
  | 'hotel'
  | 'train'
  | 'attraction'
  | 'traveler'
  | 'group'
  | 'blog'
  | 'review'
  | 'account'
  | 'planner'
  | 'orders'
  | 'operations'

type IconProps = {
  icon: AppIcon
  size?: number
  className?: string
} & SVGProps<SVGSVGElement>

// 基础 SVG 容器，统一图标的尺寸、描边和公共属性。
function BaseIcon({ children, size = 20, className, ...props }: Omit<IconProps, 'icon'> & { children: ReactNode }) {
  return (
    <svg
      viewBox="0 0 24 24"
      width={size}
      height={size}
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden="true"
      {...props}
    >
      {children}
    </svg>
  )
}

// 根据业务枚举值渲染对应的图标图形。
export function Icon({ icon, size = 20, className, ...props }: IconProps) {
  switch (icon) {
    case 'dashboard':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <rect x="3" y="4" width="18" height="14" rx="3" />
          <path d="M7 18v2h10v-2" />
          <path d="M8 13V9" />
          <path d="M12 13V7" />
          <path d="M16 13v-3" />
        </BaseIcon>
      )
    case 'flight':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <path d="M21 6 10.8 10.2" />
          <path d="m10.8 10.2-2.9 7.8-1.8-1.7 1.2-5.1-4-2.2 1.3-1.1 4.7 1L17.4 3a2.6 2.6 0 1 1 2.2 3Z" />
        </BaseIcon>
      )
    case 'hotel':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <path d="M5 19V7a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v12" />
          <path d="M3 19h18" />
          <path d="M8 10h3" />
          <path d="M8 13h3" />
          <path d="M14 10h2" />
          <path d="M14 13h2" />
        </BaseIcon>
      )
    case 'train':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <rect x="5" y="4" width="14" height="12" rx="3" />
          <path d="M8 19h8" />
          <path d="m9 16-2 3" />
          <path d="m15 16 2 3" />
          <path d="M8 8h8" />
          <circle cx="9" cy="12" r="1" />
          <circle cx="15" cy="12" r="1" />
        </BaseIcon>
      )
    case 'attraction':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <path d="M4 19h16" />
          <path d="M6 19V9l6-4 6 4v10" />
          <path d="M9 19v-4h6v4" />
          <path d="M8 10h8" />
        </BaseIcon>
      )
    case 'traveler':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <circle cx="12" cy="8" r="3.2" />
          <path d="M5 19a7 7 0 0 1 14 0" />
        </BaseIcon>
      )
    case 'group':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <circle cx="9" cy="9" r="2.6" />
          <circle cx="16.5" cy="8.5" r="2.2" />
          <path d="M4.5 19a5.5 5.5 0 0 1 9 0" />
          <path d="M14 19a4.5 4.5 0 0 1 6 0" />
        </BaseIcon>
      )
    case 'blog':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <path d="M6 4h8l4 4v12H6z" />
          <path d="M14 4v4h4" />
          <path d="M9 12h6" />
          <path d="M9 16h4" />
        </BaseIcon>
      )
    case 'review':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <path d="M6 7h12a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2h-7l-4 3v-3H6a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2Z" />
          <path d="M8.5 11.5h7" />
          <path d="M8.5 14.5h4.5" />
        </BaseIcon>
      )
    case 'account':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <path d="M12 21s7-3.6 7-10V6l-7-3-7 3v5c0 6.4 7 10 7 10Z" />
          <circle cx="12" cy="10" r="2.5" />
          <path d="M8.6 16a4.2 4.2 0 0 1 6.8 0" />
        </BaseIcon>
      )
    case 'planner':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <path d="M5 18a7 7 0 1 1 9.7-6.4" />
          <path d="m13 10 2 2 5-5" />
          <path d="M12 5v3" />
        </BaseIcon>
      )
    case 'orders':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <rect x="5" y="4" width="14" height="16" rx="2.5" />
          <path d="M9 8h6" />
          <path d="M9 12h6" />
          <path d="M9 16h4" />
        </BaseIcon>
      )
    case 'operations':
      return (
        <BaseIcon size={size} className={className} {...props}>
          <circle cx="12" cy="12" r="3" />
          <path d="M19 12h2" />
          <path d="M3 12h2" />
          <path d="M12 3v2" />
          <path d="M12 19v2" />
          <path d="m17.2 6.8 1.4-1.4" />
          <path d="m5.4 18.6 1.4-1.4" />
          <path d="m17.2 17.2 1.4 1.4" />
          <path d="m5.4 5.4 1.4 1.4" />
        </BaseIcon>
      )
    default:
      return null
  }
}
