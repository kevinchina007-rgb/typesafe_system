import type { useAppPageController } from '../hooks'

export type AppPageController = ReturnType<typeof useAppPageController>

export type AppPageRegionKey = 'loading' | 'shell' | 'notice'

export type AppPageRegion = {
  key: AppPageRegionKey
  title: string
  description: string
}

export const APP_PAGE_REGIONS: AppPageRegion[] = [
  {
    key: 'loading',
    title: '工作台恢复区',
    description: '恢复当前用户、管理者与后端健康状态。',
  },
  {
    key: 'shell',
    title: '页面壳层',
    description: '承载顶部导航和当前功能页面。',
  },
  {
    key: 'notice',
    title: '通知层',
    description: '承载全局提示与错误信息。',
  },
]
