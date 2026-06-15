// 本文件定义共享页面组件或工具，负责页面间复用逻辑。

import type { AppLanguage, ResourceReviewSummaryPlannerResponse } from '@/lib/mvp-types/index'
import { summarizeReviewAggregate } from '@/lib/presenters/content-presenter'

// 资源评价汇总组件的输入参数。
type ResourceReviewSummaryProps = {
  currentLanguage: AppLanguage
  summary: ResourceReviewSummaryPlannerResponse | null
  translate: (translationKey: string) => string
}

// 评价汇总组件，没有数据时显示空态，有数据时显示统计结果。
export function ResourceReviewSummary({
  currentLanguage,
  summary,
  translate,
}: ResourceReviewSummaryProps) {
  if (!summary) {
    return <p className="text-sm font-medium text-slate-500">{translate('reviews.summaryEmpty')}</p>
  }

  return <p className="text-sm font-medium text-slate-500">{summarizeReviewAggregate(summary, currentLanguage)}</p>
}
