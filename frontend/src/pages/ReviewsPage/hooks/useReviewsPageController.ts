// 本文件定义 ReviewsPage 页面的状态控制逻辑，负责条件维护、请求触发和动作调度。

import { useEffect, useMemo, useState } from 'react'

import type { ReviewPlannerResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { getReviewsPageResourceTypes, getVisibleReviews } from '../functions'
import type { ReviewsPageController, ReviewsPageProps } from '../objects'

export function useReviewsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: ReviewsPageProps): ReviewsPageController {
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)
  const [reviews, setReviews] = useState<ReviewPlannerResponse[]>([])
  const [activeResourceType, setActiveResourceType] = useState('All')
  const [searchText, setSearchText] = useState('')
  const [searchDraft, setSearchDraft] = useState('')
  const [editingReview, setEditingReview] = useState<ReviewPlannerResponse | null>(null)

  function requireSignedInUser() {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUser
  }

  const refreshReviews = async () => {
    if (!signedInUser) {
      setReviews([])
      return
    }
    const nextSignedInUser = requireSignedInUser()
    const response = await travelMvpApiClient.listMyReviews({ userId: nextSignedInUser.userId })
    setReviews(response.reviews)
  }

  const visibleReviews = useMemo(() => getVisibleReviews(reviews, activeResourceType, searchText), [activeResourceType, reviews, searchText])
  const resourceTypes = useMemo(() => getReviewsPageResourceTypes(reviews), [reviews])

  useEffect(() => {
    void refreshReviews()
  }, [signedInUser?.userId])

  return {
    currentLanguage,
    isBusy,
    signedInUser,
    reviews,
    activeResourceType,
    searchDraft,
    searchText,
    editingReview,
    visibleReviews,
    resourceTypes,
    translate,
    setSearchDraft,
    setSearchText,
    setActiveResourceType,
    setEditingReview,
    refreshReviews,
    handleUploadImage: async imageFile => {
      const nextSignedInUser = requireSignedInUser()
      return runPageActionWithResult(
        () => travelMvpApiClient.uploadReviewImage(nextSignedInUser.userId, imageFile),
        translate('content.imagesUpload'),
        translate('notice.actionSuccess'),
      )
    },
    handleUpdateReview: async (reviewId, payload) => {
      const nextSignedInUser = requireSignedInUser()
      return runPageActionWithResult(
        async () =>
          travelMvpApiClient.updateReview({
            userId: nextSignedInUser.userId,
            reviewId,
            rating: payload.rating,
            title: payload.title,
            content: payload.content,
            images: payload.images,
          }),
        translate('reviews.save'),
        translate('notice.actionSuccess'),
      )
    },
    handleDeleteReview: async reviewId => {
      const nextSignedInUser = requireSignedInUser()
      await runPageAction(async () => {
        await travelMvpApiClient.deleteReview({ userId: nextSignedInUser.userId, reviewId })
      }, translate('reviews.delete'), translate('notice.actionSuccess'))
    },
    handleSubmitReviewEdit: async payload => {
      if (!editingReview) {
        return
      }
      await travelMvpApiClient.updateReview({
        userId: requireSignedInUser().userId,
        reviewId: editingReview.reviewId,
        rating: payload.rating,
        title: payload.title,
        content: payload.content,
        images: payload.images,
      })
      await refreshReviews()
      setEditingReview(null)
    },
  }
}
