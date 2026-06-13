import type { TopNavBrandProps } from '@/app/shell/TopNavBar.types'

export function TopNavBrand({ onLogoClick, isOverlay = false }: TopNavBrandProps) {
  return (
    <button
      type="button"
      className="grid min-w-0 border-0 bg-transparent p-0 text-left"
      onClick={onLogoClick}
      aria-label="网站管理者登录"
    >
      <img
        className={isOverlay ? 'h-18 w-auto max-w-[24rem] object-contain object-left md:h-20 md:max-w-[26rem]' : 'h-18 w-auto max-w-[24rem] object-contain object-left md:h-20 md:max-w-[26rem]'}
        src="/images/fly-pig-logo.png"
        alt="flybara"
      />
    </button>
  )
}
