import type { useAppPageController } from '../hooks'

// App 页面控制器的类型别名。
export type AppPageController = ReturnType<typeof useAppPageController>

// App 页面区域的 key。
export type AppPageRegionKey = 'loading' | 'shell' | 'notice'

// App 页面区域的展示信息。
export type AppPageRegion = {
  key: AppPageRegionKey
  title: string
  description: string
}

// App 页面区域定义列表。
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
