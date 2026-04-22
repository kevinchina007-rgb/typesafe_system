import { useEffect, useState } from 'react'

import { markFeedbackThreadRead, sendFeedbackMessage, useFeedbackChatStore } from '../../../app/stores/feedback-chat-store'
import { travelMvpApiClient } from '../../../lib/api-client'
import { FeedbackConversationWorkspace } from '../../../components/feedback/FeedbackConversationWorkspace'
import { AdvertisementReviewWorkspace } from '../../../components/advertising/sections/AdvertisementReviewWorkspace'
import type {
  BlogPostSummaryResponse,
} from '../../../lib/api-dtos/content'
import type {
  AttractionAdminSessionResponse,
  CurrentManagerSessionResponse,
  FlightResponse,
  ManagerRefundTaskResponse,
  ManagerTaskResponse,
  TrainAdminSessionResponse,
} from '../../../lib/mvp-types'

export type ManagerCenterSectionKey =
  | 'workspace'
  | 'feedback'
  | 'advertising'
  | 'blogAudit'
  | 'advertisingReview'

type SupplierFeedbackSectionProps = {
  title: string
  currentManagerSession: CurrentManagerSessionResponse | null
  managedFlightResponses: FlightResponse[]
  managerTaskResponses: ManagerTaskResponse[]
  managerRefundTaskResponses: ManagerRefundTaskResponse[]
  currentTrainAdminSession: TrainAdminSessionResponse | null
  currentAttractionAdminSession: AttractionAdminSessionResponse | null
  translate: (translationKey: string) => string
}

type SiteAdminPanelProps = {
  section: 'blogAudit' | 'advertisingReview'
  currentManagerSession: CurrentManagerSessionResponse | null
  translate: (translationKey: string) => string
}

export function SupplierFeedbackSection({
  title,
  currentManagerSession,
  managedFlightResponses,
  managerTaskResponses,
  managerRefundTaskResponses,
  currentTrainAdminSession,
  currentAttractionAdminSession,
  translate,
}: SupplierFeedbackSectionProps) {
  const loadManagerThreads = useFeedbackChatStore(state => state.loadManagerThreads)
  const managerThreads = useFeedbackChatStore(state => state.managerThreads)
  const trainCount = currentTrainAdminSession?.managedTrains.length ?? 0
  const attractionCount = currentAttractionAdminSession?.managedAttractions.length ?? 0

  useEffect(() => {
    if (!currentManagerSession) {
      return
    }

    void loadManagerThreads()
  }, [currentManagerSession?.managerId, loadManagerThreads])

  return (
    <section className="page-stack">
      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('manager.userFeedback')}</p>
            <h2 className="section-title">{title}</h2>
          </div>
        </div>

        <div className="manager-feedback-stats">
          <article className="stat-card">
            <span className="detail-label">{translate('manager.feedback.pendingDecisions')}</span>
            <strong className="stat-card-value">{managerTaskResponses.length}</strong>
          </article>
          <article className="stat-card">
            <span className="detail-label">{translate('manager.feedback.refundQueue')}</span>
            <strong className="stat-card-value">{managerRefundTaskResponses.length}</strong>
          </article>
          <article className="stat-card">
            <span className="detail-label">{translate('manager.feedback.flightInventory')}</span>
            <strong className="stat-card-value">{managedFlightResponses.length}</strong>
          </article>
          <article className="stat-card">
            <span className="detail-label">{translate('manager.feedback.resourceCount')}</span>
            <strong className="stat-card-value">{trainCount + attractionCount}</strong>
          </article>
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
      />

      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('manager.feedback.pendingDecisions')}</p>
            <h3 className="section-title">{translate('manager.feedback.pendingTitle')}</h3>
          </div>
        </div>

        {managerTaskResponses.length === 0 ? (
          <p className="empty-state">{translate('manager.feedback.empty')}</p>
        ) : (
          <div className="manager-feedback-list">
            {managerTaskResponses.map(task => (
              <article key={task.orderItemId} className="panel-card manager-feedback-card">
                <strong>{task.summaryLabel}</strong>
                <span>{task.detailLabel}</span>
                <span>{task.requestedAt}</span>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('manager.feedback.refundQueue')}</p>
            <h3 className="section-title">{translate('manager.feedback.refundTitle')}</h3>
          </div>
        </div>

        {managerRefundTaskResponses.length === 0 ? (
          <p className="empty-state">{translate('manager.feedback.emptyRefunds')}</p>
        ) : (
          <div className="manager-feedback-list">
            {managerRefundTaskResponses.map(task => (
              <article key={task.refundId} className="panel-card manager-feedback-card">
                <strong>{task.summaryLabel}</strong>
                <span>{task.refundReason}</span>
                <span>{`${task.refundAmount} ${task.refundCurrency}`}</span>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  )
}

export function SiteAdminPanel({ section, currentManagerSession, translate }: SiteAdminPanelProps) {
  void currentManagerSession

  const sectionCopyMap: Record<'blogAudit' | 'advertisingReview', { title: string; description: string }> = {
    blogAudit: {
      title: translate('manager.siteAdmin.blogAudit'),
      description: translate('manager.siteAdmin.blogAuditDescription'),
    },
    advertisingReview: {
      title: translate('advertising.reviewTitle'),
      description: translate('advertising.reviewDescription'),
    },
  }

  const sectionCopy = sectionCopyMap[section]
  return (
    <section className="page-stack">
      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('manager.siteAdmin.title')}</p>
            <h2 className="section-title">{sectionCopy.title}</h2>
          </div>
        </div>
        <p className="hero-copy">{sectionCopy.description}</p>
      </section>

      {section === 'blogAudit' ? (
        <SiteAdminBlogAuditWorkspace translate={translate} />
      ) : (
        <AdvertisementReviewWorkspace translate={translate} />
      )}
    </section>
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
    <section className="page-stack">
      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('manager.siteAdmin.blogAudit')}</p>
            <h3 className="section-title">{translate('manager.siteAdmin.blogAuditPending')}</h3>
          </div>
        </div>

        {isLoading ? (
          <p className="empty-state">{translate('search.loading')}</p>
        ) : pendingPosts.length === 0 ? (
          <p className="empty-state">{translate('manager.siteAdmin.blogAuditEmptyPending')}</p>
        ) : (
          <div className="manager-feedback-list">
            {pendingPosts.map(post => (
              <article key={post.postId} className="panel-card manager-feedback-card">
                <strong>{post.title}</strong>
                <span>{post.authorDisplayName}</span>
                <span>{post.summary}</span>
                <span>{post.status}</span>
                <div className="button-row">
                  <button type="button" className="secondary-button" onClick={() => void handleDecision(post.postId, 'reject')}>
                    {translate('manager.siteAdmin.rejectBlog')}
                  </button>
                  <button type="button" className="primary-button" onClick={() => void handleDecision(post.postId, 'approve')}>
                    {translate('manager.siteAdmin.approveBlog')}
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('manager.siteAdmin.blogAudit')}</p>
            <h3 className="section-title">{translate('manager.siteAdmin.blogAuditReviewed')}</h3>
          </div>
        </div>

        {isLoading ? (
          <p className="empty-state">{translate('search.loading')}</p>
        ) : reviewedPosts.length === 0 ? (
          <p className="empty-state">{translate('manager.siteAdmin.blogAuditEmptyReviewed')}</p>
        ) : (
          <div className="manager-feedback-list">
            {reviewedPosts.map(post => (
              <article key={post.postId} className="panel-card manager-feedback-card">
                <strong>{post.title}</strong>
                <span>{post.authorDisplayName}</span>
                <span>{post.summary}</span>
                <span>{post.status}</span>
              </article>
            ))}
          </div>
        )}
      </section>

      {errorMessage ? <p className="empty-state">{errorMessage}</p> : null}
    </section>
  )
}
