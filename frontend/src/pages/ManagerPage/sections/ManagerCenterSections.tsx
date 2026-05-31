import { useEffect, useState } from 'react'

import { handleOrderCancellationRequest, markFeedbackThreadRead, sendFeedbackMessage, useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { FeedbackConversationWorkspace } from '@/pages/shared/feedback/FeedbackConversationWorkspace'
import { AdvertisementReviewWorkspace } from '@/pages/ManagerPage/components/advertising/AdvertisementReviewWorkspace'
import type { BlogPostSummaryResponse } from '@/microservices/content/objects/BlogPostSummaryResponse'
import type { AttractionAdminSessionResponse, CurrentManagerSessionResponse, FlightPlannerResponse, ManagerRefundTaskResponse, ManagerTaskResponse, TrainAdminSessionResponse } from '@/lib/mvp-types/index'

export type ManagerCenterSectionKey =
  | 'workspace'
  | 'feedback'
  | 'profile'
  | 'advertising'
  | 'blogAudit'
  | 'advertisingReview'
  | 'siteAdminFeedback'

type SupplierFeedbackSectionProps = {
  title: string
  currentManagerSession: CurrentManagerSessionResponse | null
  managedFlightPlannerResponses: FlightPlannerResponse[]
  managerTaskResponses: ManagerTaskResponse[]
  managerRefundTaskResponses: ManagerRefundTaskResponse[]
  currentTrainAdminSession: TrainAdminSessionResponse | null
  currentAttractionAdminSession: AttractionAdminSessionResponse | null
  translate: (translationKey: string) => string
}

type SiteAdminPanelProps = {
  section: 'blogAudit' | 'advertisingReview' | 'siteAdminFeedback'
  advertisingModule: 'flight' | 'hotel' | 'train' | 'attraction'
  currentManagerSession: CurrentManagerSessionResponse | null
  translate: (translationKey: string) => string
}

export function SupplierFeedbackSection({
  title,
  currentManagerSession,
  managedFlightPlannerResponses,
  managerTaskResponses,
  managerRefundTaskResponses,
  currentTrainAdminSession,
  currentAttractionAdminSession,
  translate,
}: SupplierFeedbackSectionProps) {
  const loadManagerThreads = useFeedbackChatStore(state => state.loadManagerThreads)
  const managerThreads = useFeedbackChatStore(state => state.managerThreads)
  void managedFlightPlannerResponses
  void managerTaskResponses
  void managerRefundTaskResponses
  void currentTrainAdminSession
  void currentAttractionAdminSession

  useEffect(() => {
    if (!currentManagerSession) {
      return
    }

    void loadManagerThreads()
  }, [currentManagerSession?.managerId, loadManagerThreads])

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('manager.userFeedback')}</p>
            <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{title}</h2>
          </div>
        </div>
      </section>

      <FeedbackConversationWorkspace
        audience="Manager"
        audienceDisplayName={currentManagerSession?.displayName ?? ''}
        emptyTitle={translate('feedback.managerTitle')}
        emptyDescription={translate('feedback.managerEmpty')}
        threads={managerThreads}
        translate={translate}
        unreadCountSelector={thread => thread.unreadByManager}
        onMarkRead={markFeedbackThreadRead}
        onSendMessage={(threadId, body) =>
          sendFeedbackMessage({
            threadId,
            senderRole: 'Manager',
            senderDisplayName: currentManagerSession?.displayName ?? translate('manager.centerTitle'),
            body,
          })
        }
        onHandleCancellationRequest={(threadId, messageId, status, managerNote) =>
          handleOrderCancellationRequest({
            threadId,
            messageId,
            status,
            managerNote,
            handledBy: currentManagerSession?.displayName ?? translate('manager.centerTitle'),
            handlerRole: 'Manager',
          })
        }
      />

    </section>
  )
}

export function SiteAdminPanel({ section, advertisingModule, currentManagerSession, translate }: SiteAdminPanelProps) {
  const sectionCopyMap: Record<'blogAudit' | 'advertisingReview' | 'siteAdminFeedback', { title: string; description: string }> = {
    blogAudit: {
      title: translate('manager.siteAdmin.blogAudit'),
      description: translate('manager.siteAdmin.blogAuditDescription'),
    },
    advertisingReview: {
      title: translate('advertising.reviewTitle'),
      description: translate('advertising.reviewDescription'),
    },
    siteAdminFeedback: {
      title: translate('feedback.title'),
      description: translate('manager.siteAdmin.userCoordinationDescription'),
    },
  }

  const sectionCopy = sectionCopyMap[section]
  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('manager.siteAdmin.title')}</p>
            <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{sectionCopy.title}</h2>
          </div>
        </div>
        <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{sectionCopy.description}</p>
      </section>

      {section === 'blogAudit' ? (
        <SiteAdminBlogAuditWorkspace translate={translate} />
      ) : section === 'advertisingReview' ? (
        <AdvertisementReviewWorkspace businessModule={advertisingModule} translate={translate} />
      ) : (
        <SiteAdminFeedbackWorkspace currentManagerSession={currentManagerSession} translate={translate} />
      )}
    </section>
  )
}

function SiteAdminFeedbackWorkspace({ currentManagerSession, translate }: { currentManagerSession: CurrentManagerSessionResponse | null; translate: (translationKey: string) => string }) {
  const loadSiteAdminThreads = useFeedbackChatStore(state => state.loadSiteAdminThreads)
  const siteAdminUserThreads = useFeedbackChatStore(state => state.siteAdminUserThreads)

  useEffect(() => {
    void loadSiteAdminThreads('user')
  }, [loadSiteAdminThreads])

  return (
    <FeedbackConversationWorkspace
      audience="SiteAdmin"
      audienceDisplayName={currentManagerSession?.displayName ?? translate('manager.siteAdmin.title')}
      emptyTitle={translate('feedback.title')}
      emptyDescription={translate('feedback.siteAdminEmpty')}
      threads={siteAdminUserThreads}
      translate={translate}
      unreadCountSelector={thread => thread.unreadBySiteAdmin}
      onMarkRead={markFeedbackThreadRead}
      onSendMessage={(threadId, body) =>
        sendFeedbackMessage({
          threadId,
          senderRole: 'SiteAdmin',
          senderDisplayName: currentManagerSession?.displayName ?? translate('manager.siteAdmin.title'),
          body,
        })
      }
    />
  )
}

function SiteAdminBlogAuditWorkspace({ translate }: { translate: (translationKey: string) => string }) {
  const [pendingPosts, setPendingPosts] = useState<BlogPostSummaryResponse[]>([])
  const [reviewedPosts, setReviewedPosts] = useState<BlogPostSummaryResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  async function loadModerationQueues() {
    setIsLoading(true)
    setErrorMessage(null)

    try {
      const [pendingResponse, reviewedResponse] = await Promise.all([
        travelMvpApiClient.listBlogModerationPosts('pending'),
        travelMvpApiClient.listBlogModerationPosts('reviewed'),
      ])

      setPendingPosts(pendingResponse.posts)
      setReviewedPosts(reviewedResponse.posts)
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : translate('error.friendly.default'))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadModerationQueues()
  }, [])

  async function handleDecision(postId: string, decision: 'approve' | 'reject') {
    try {
      if (decision === 'approve') {
        await travelMvpApiClient.approveBlogPost(postId)
      } else {
        await travelMvpApiClient.rejectBlogPost(postId)
      }
      await loadModerationQueues()
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : translate('error.friendly.default'))
    }
  }

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('manager.siteAdmin.blogAudit')}</p>
            <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('manager.siteAdmin.blogAuditPending')}</h3>
          </div>
        </div>

        {isLoading ? (
          <p className="text-sm leading-6 text-slate-500">{translate('search.loading')}</p>
        ) : pendingPosts.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('manager.siteAdmin.blogAuditEmptyPending')}</p>
        ) : (
          <div className="grid gap-3">
            {pendingPosts.map(post => (
              <article key={post.postId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-3">
                <strong>{post.title}</strong>
                <span>{post.authorDisplayName}</span>
                <span>{post.summary}</span>
                <span>{post.status}</span>
                <div className="button-row">
                  <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => void handleDecision(post.postId, 'reject')}>
                    {translate('manager.siteAdmin.rejectBlog')}
                  </button>
                  <button type="button" className="inline-flex min-h-11 items-center justify-center border border-black bg-black px-4 py-2 text-sm font-semibold text-white shadow-none transition hover:bg-white hover:text-black disabled:cursor-not-allowed disabled:opacity-55" onClick={() => void handleDecision(post.postId, 'approve')}>
                    {translate('manager.siteAdmin.approveBlog')}
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('manager.siteAdmin.blogAudit')}</p>
            <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('manager.siteAdmin.blogAuditReviewed')}</h3>
          </div>
        </div>

        {isLoading ? (
          <p className="text-sm leading-6 text-slate-500">{translate('search.loading')}</p>
        ) : reviewedPosts.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('manager.siteAdmin.blogAuditEmptyReviewed')}</p>
        ) : (
          <div className="grid gap-3">
            {reviewedPosts.map(post => (
              <article key={post.postId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-3">
                <strong>{post.title}</strong>
                <span>{post.authorDisplayName}</span>
                <span>{post.summary}</span>
                <span>{post.status}</span>
              </article>
            ))}
          </div>
        )}
      </section>

      {errorMessage ? <p className="text-sm leading-6 text-slate-500">{errorMessage}</p> : null}
    </section>
  )
}
