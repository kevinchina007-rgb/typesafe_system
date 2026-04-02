import { MyReviewsPanel } from '../../components/MyReviewsPanel'
import { travelMvpApiClient } from '../../lib/api-client'
import type { AppLanguage, UserResponse } from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'

type ReviewsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

export function ReviewsPage({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: ReviewsPageProps) {
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

  function requireSignedInUser() {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUser
  }

  return (
    <MyReviewsPanel
      currentLanguage={currentLanguage}
      isBusy={isBusy}
      signedInUser={signedInUser}
      translate={translate}
      onListMyReviews={async () => {
        const nextSignedInUser = requireSignedInUser()
        const response = await travelMvpApiClient.listMyReviews(nextSignedInUser.userId)
        return response.reviews
      }}
      onUploadImage={async imageFile => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () => travelMvpApiClient.uploadReviewImage(nextSignedInUser.userId, imageFile),
          translate('content.imagesUpload'),
          translate('notice.actionSuccess'),
        )
      }}
      onUpdateReview={async (reviewId, payload) => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          async () => {
            return travelMvpApiClient.updateReview(reviewId, {
              userId: nextSignedInUser.userId,
              rating: payload.rating,
              title: payload.title,
              content: payload.content,
              images: payload.images,
            })
          },
          translate('reviews.save'),
          translate('notice.actionSuccess'),
        )
      }}
      onDeleteReview={async reviewId => {
        const nextSignedInUser = requireSignedInUser()
        await runPageAction(async () => {
          await travelMvpApiClient.deleteReview(reviewId, { userId: nextSignedInUser.userId })
        }, translate('reviews.delete'), translate('notice.actionSuccess'))
      }}
    />
  )
}
