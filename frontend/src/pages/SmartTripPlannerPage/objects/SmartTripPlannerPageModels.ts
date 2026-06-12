// 本文件定义 SmartTripPlannerPage 页面数据模型和 JSON 编解码。

import type { AppViewKey } from '@/lib/mvp-types/index'

export type SmartTripPlannerPageProps = {
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type SmartTripPlannerPageRegion = 'hero' | 'search' | 'empty'

export const SMART_TRIP_PLANNER_PAGE_REGIONS: SmartTripPlannerPageRegion[] = ['hero', 'search', 'empty']

