// 本文件定义 ReviewsPage 页面的页面壳层，负责页面头部、内容区和操作入口布局。

import { CommunityHero } from '@/pages/shared/community/CommunityHero'
import { ReviewComposerDialog } from '@/pages/shared/content/ReviewComposerDialog'
import type { ReviewsPageController } from '../objects'
import { ReviewListSection } from './ReviewListSection'
import { getReviewTabLabel } from '../functions'

type ReviewsPageShellProps = {
  controller: ReviewsPageController
}

export function ReviewsPageShell({ controller }: ReviewsPageShellProps) {
  return (
    <>
      {/* 页面区域总览：hero / list / dialog */}
      <section className="sr-only">
        <span>hero</span>
        <span>list</span>
        <span>dialog</span>
      </section>

      <CommunityHero
        eyebrow={controller.translate('community.reviewEyebrow')}
        title={controller.translate('reviews.title')}
        searchValue={controller.searchDraft}
        searchButtonLabel={controller.translate('search.confirm')}
        tabs={controller.resourceTypes.map(resourceType => ({
          key: resourceType,
          label: getReviewTabLabel(resourceType, controller.currentLanguage, controller.translate),
        }))}
        activeTab={controller.activeResourceType}
        secondaryActionLabel={controller.translate('reviews.refresh')}
        isBusy={controller.isBusy}
        onSearchChange={controller.setSearchDraft}
        onSearchSubmit={() => {
          controller.setSearchText(controller.searchDraft)
        }}
        onSelectTab={controller.setActiveResourceType}
        onSecondaryAction={() => void controller.refreshReviews()}
      />

      {!controller.signedInUser ? <p className="text-sm leading-6 text-slate-500">{controller.translate('reviews.guest')}</p> : null}

      <ReviewListSection
        currentLanguage={controller.currentLanguage}
        isBusy={controller.isBusy}
        reviews={controller.visibleReviews}
        signedInUser={controller.signedInUser}
        translate={controller.translate}
        onEditReview={controller.setEditingReview}
        onDeleteReview={reviewId => {
          void controller.handleDeleteReview(reviewId).then(controller.refreshReviews)
        }}
      />

      <ReviewComposerDialog
        isOpen={controller.editingReview !== null}
        isBusy={controller.isBusy}
        eligibility={null}
        mode="edit"
        initialValue={
          controller.editingReview
            ? {
                rating: controller.editingReview.rating,
                title: controller.editingReview.title,
                content: controller.editingReview.content,
                images: controller.editingReview.images,
              }
            : null
        }
        title={controller.editingReview?.resourceSummaryTitle ?? ''}
        translate={controller.translate}
        onClose={() => controller.setEditingReview(null)}
        onUploadImage={controller.handleUploadImage}
        onSubmit={controller.handleSubmitReviewEdit}
      />
    </>
  )
}

