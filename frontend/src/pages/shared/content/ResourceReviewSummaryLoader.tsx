// 本文件定义共享页面组件或工具，负责页面间复用逻辑。

import { useEffect, useState } from 'react'

import type { AppLanguage, ResourceReviewSummaryPlannerResponse, ReviewPlannerResponse } from '@/lib/mvp-types/index'
import { ResourceReviewSummary } from '@/pages/shared/content/ResourceReviewSummary'
import { ReviewListDialog } from '@/pages/shared/content/ReviewListDialog'

// 资源评价汇总加载器的输入参数。
type ResourceReviewSummaryLoaderProps = {
  currentLanguage: AppLanguage
  isBusy?: boolean
  isEnabled: boolean
  resourceType: string
  resourceId: string
  title: string
  translate: (translationKey: string) => string
  onLoadSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryPlannerResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewPlannerResponse[]>
}

// 资源评价汇总加载器，负责先加载摘要，再按需打开评论列表。
export function ResourceReviewSummaryLoader({
  currentLanguage,
  isBusy = false,
  isEnabled,
  resourceType,
  resourceId,
  title,
  translate,
  onLoadSummary,
  onLoadReviews,
}: ResourceReviewSummaryLoaderProps) {
  const [summary, setSummary] = useState<ResourceReviewSummaryPlannerResponse | null>(null)
  const [reviews, setReviews] = useState<ReviewPlannerResponse[]>([])
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  // 资源启用时自动加载评价摘要。
  useEffect(() => {
    if (!isEnabled) {
      setSummary(null)
      return
    }
    void onLoadSummary({ resourceType, resourceId }).then(setSummary).catch(() => setSummary(null))
  }, [isEnabled, onLoadSummary, resourceId, resourceType])

  if (!isEnabled) {
    return null
  }

  return (
    <>
      {/* 上方区域负责展示摘要并提供“查看评价”入口。 */}
      <div className="flex flex-wrap items-center gap-3">
        <ResourceReviewSummary currentLanguage={currentLanguage} summary={summary} translate={translate} />
        <button
          type="button"
          className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
          disabled={isBusy}
          onClick={() => {
            void onLoadReviews({ resourceType, resourceId }).then(nextReviews => {
              setReviews(nextReviews)
              setIsDialogOpen(true)
            })
          }}
        >
          {translate('reviews.view')}
        </button>
      </div>

      {/* 明细对话框使用单独弹窗承载评论列表。 */}
      <ReviewListDialog
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        isOpen={isDialogOpen}
        title={title}
        reviews={reviews}
        translate={translate}
        onClose={() => setIsDialogOpen(false)}
      />
    </>
  )
}
