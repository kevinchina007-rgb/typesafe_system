import { useEffect } from 'react'

import { FeedbackReviewComposerPanel } from '../../components/feedback/FeedbackReviewComposerPanel'
import { FeedbackConversationWorkspace } from '../../components/feedback/FeedbackConversationWorkspace'
import {
  createReviewFeedbackThread,
  setPendingFeedbackReviewDraft,
  setActiveFeedbackMiniThread,
  markFeedbackThreadRead,
  sendFeedbackMessage,
  useFeedbackChatStore,
} from '../../app/stores/feedback-chat-store'
import { travelMvpApiClient } from '../../lib/api-client'
import type { AppLanguage, UserResponse } from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'

type CustomerFeedbackPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

export function CustomerFeedbackPage({ currentLanguage, signedInUser, translate, onShowNotice }: CustomerFeedbackPageProps) {
  const threads = useFeedbackChatStore(state => state.userThreads)
  const activeThread = useFeedbackChatStore(state => state.activeMiniThread)
  const pendingReviewDraft = useFeedbackChatStore(state => state.pendingReviewDraft)
  const loadUserThreads = useFeedbackChatStore(state => state.loadUserThreads)
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

  useEffect(() => {
    if (!signedInUser) {
      return
    }

    void loadUserThreads()
  }, [loadUserThreads, signedInUser?.userId])

  if (!signedInUser) {
    return (
      <section className="page-card">
        <p className="empty-state">{translate('feedback.userGuest')}</p>
      </section>
    )
  }

  return (
    <>
      {pendingReviewDraft ? (
        <FeedbackReviewComposerPanel
          isBusy={isBusy}
          eligibility={pendingReviewDraft.eligibility}
          title={pendingReviewDraft.title}
          translate={translate}
          onUploadImage={imageFile =>
            runPageActionWithResult(
              () => travelMvpApiClient.uploadReviewImage(signedInUser.userId, imageFile),
              translate('content.imagesUpload'),
              translate('notice.actionSuccess'),
            )
          }
          onSubmit={async payload => {
            await runPageAction(async () => {
              const review = await travelMvpApiClient.createReview({
                userId: signedInUser.userId,
                orderId: pendingReviewDraft.orderId,
                orderItemId: pendingReviewDraft.orderItemId,
                rating: payload.rating,
                title: payload.title,
                content: payload.content,
                images: payload.images,
              })
              const thread = await createReviewFeedbackThread(review.reviewId)
              setActiveFeedbackMiniThread(thread)
              setPendingFeedbackReviewDraft(null)
              await loadUserThreads()
            }, translate('reviews.submit'), translate('notice.actionSuccess'))
          }}
          onCancel={() => setPendingFeedbackReviewDraft(null)}
        />
      ) : null}

      <FeedbackConversationWorkspace
        audience="User"
        audienceDisplayName={signedInUser.nickname}
        emptyTitle={translate('feedback.userTitle')}
        emptyDescription={translate('feedback.userEmpty')}
        fullScreen
        preferredThreadId={activeThread?.threadId ?? null}
        threads={threads}
        translate={translate}
        unreadCountSelector={thread => thread.unreadByUser}
        onMarkRead={markFeedbackThreadRead}
        onSendMessage={(threadId, body) =>
          sendFeedbackMessage({
            threadId,
            senderRole: 'User',
            senderDisplayName: signedInUser.nickname,
            body,
          })
        }
        onThreadChange={thread => setActiveFeedbackMiniThread(thread)}
      />
    </>
  )
}
