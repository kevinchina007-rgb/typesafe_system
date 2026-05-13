import { useEffect, useMemo, useState } from 'react'

import type { FeedbackAudience } from '@/microservices/content/objects/FeedbackAudience'
import type { FeedbackManagerType } from '@/microservices/content/objects/FeedbackManagerType'
import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'

type FeedbackConversationWorkspaceProps = {
  audience: FeedbackAudience
  audienceDisplayName: string
  emptyTitle: string
  emptyDescription: string
  threads: FeedbackThread[]
  fullScreen?: boolean
  preferredThreadId?: string | null
  translate: (translationKey: string) => string
  unreadCountSelector: (thread: FeedbackThread) => number
  onThreadChange?: (thread: FeedbackThread | null) => void
  onMarkRead: (threadId: string, audience: FeedbackAudience) => Promise<unknown> | void
  onSendMessage: (threadId: string, body: string) => Promise<unknown> | void
  onEscalate?: (thread: FeedbackThread) => Promise<unknown> | void
}

function localizeManagerType(managerType: FeedbackManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'Airline') {
    return translate('manager.type.airline')
  }
  if (managerType === 'Hotel') {
    return translate('manager.type.hotel')
  }
  if (managerType === 'Train') {
    return translate('manager.type.train')
  }
  if (managerType === 'Attraction') {
    return translate('manager.type.attraction')
  }
  return translate('manager.type.siteAdmin')
}

export function FeedbackConversationWorkspace({
  audience,
  audienceDisplayName,
  emptyTitle,
  emptyDescription,
  threads,
  fullScreen = false,
  preferredThreadId,
  translate,
  unreadCountSelector,
  onThreadChange,
  onMarkRead,
  onSendMessage,
  onEscalate,
}: FeedbackConversationWorkspaceProps) {
  const [activeThreadId, setActiveThreadId] = useState<string | null>(threads[0]?.threadId ?? null)
  const [draftMessage, setDraftMessage] = useState('')

  useEffect(() => {
    if (!threads.some(thread => thread.threadId === activeThreadId)) {
      setActiveThreadId(threads[0]?.threadId ?? null)
    }
  }, [activeThreadId, threads])

  useEffect(() => {
    if (preferredThreadId && threads.some(thread => thread.threadId === preferredThreadId)) {
      setActiveThreadId(preferredThreadId)
    }
  }, [preferredThreadId, threads])

  const activeThread = useMemo(
    () => threads.find(thread => thread.threadId === activeThreadId) ?? null,
    [activeThreadId, threads]
  )

  useEffect(() => {
    onThreadChange?.(activeThread)
  }, [activeThread, onThreadChange])

  useEffect(() => {
    if (!activeThread) {
      return
    }
    void onMarkRead(activeThread.threadId, audience)
  }, [activeThread?.threadId, audience, onMarkRead])

  if (threads.length === 0) {
    return (
      <section className={fullScreen ? 'page-card feedback-page-card feedback-page-card--fullscreen' : 'page-card feedback-page-card'}>
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('feedback.title')}</p>
            <h2 className="section-title">{emptyTitle}</h2>
          </div>
        </div>
        <p className="empty-state">{emptyDescription}</p>
      </section>
    )
  }

  return (
    <section className={fullScreen ? 'page-card feedback-page-card feedback-page-card--fullscreen' : 'page-card feedback-page-card'}>
      <div className={fullScreen ? 'feedback-workspace feedback-workspace--fullscreen' : 'feedback-workspace'}>
        <aside className="feedback-thread-list">
          {threads.map(thread => {
            const unreadCount = unreadCountSelector(thread)
            return (
              <button
                key={thread.threadId}
                type="button"
                className={thread.threadId === activeThreadId ? 'feedback-thread-card is-active' : 'feedback-thread-card'}
                onClick={() => {
                  setActiveThreadId(thread.threadId)
                  setDraftMessage('')
                }}
              >
                <div className="feedback-thread-card-head">
                  <strong>{thread.title}</strong>
                  {unreadCount > 0 ? <span className="feedback-unread-badge">{unreadCount}</span> : null}
                </div>
                <span>{thread.subtitle}</span>
                <span>{localizeManagerType(thread.managerType, translate)}</span>
              </button>
            )
          })}
        </aside>

        {activeThread ? (
          <div className="feedback-thread-panel">
            <div className="section-header">
              <div>
                <p className="eyebrow-label">{translate('feedback.thread')}</p>
                <h2 className="section-title">{activeThread.title}</h2>
              </div>
            </div>

            <div className="manager-feedback-stats">
              <article className="stat-card">
                <span className="detail-label">{translate('feedback.counterparty')}</span>
                <strong className="stat-card-value">{localizeManagerType(activeThread.managerType, translate)}</strong>
              </article>
              <article className="stat-card">
                <span className="detail-label">{translate('feedback.owner')}</span>
                <strong className="stat-card-value">{activeThread.ownerUserDisplayName}</strong>
              </article>
              <article className="stat-card">
                <span className="detail-label">{translate('feedback.updatedAt')}</span>
                <strong className="stat-card-value">{new Date(activeThread.updatedAt).toLocaleString()}</strong>
              </article>
            </div>

            <div className="feedback-message-list">
              {activeThread.messages.map(message => (
                <article
                  key={message.messageId}
                  className={
                    message.senderDisplayName === audienceDisplayName
                      ? 'feedback-message-card is-self'
                      : 'feedback-message-card'
                  }
                >
                  <strong>{message.senderDisplayName}</strong>
                  <p>{message.body}</p>
                  <span>{new Date(message.sentAt).toLocaleString()}</span>
                </article>
              ))}
            </div>

            <form
              className="stack-form feedback-composer"
              onSubmit={event => {
                event.preventDefault()
                if (draftMessage.trim().length === 0) {
                  return
                }
                onSendMessage(activeThread.threadId, draftMessage)
                setDraftMessage('')
              }}
            >
              <label>
                {translate('feedback.message')}
                <textarea
                  rows={4}
                  value={draftMessage}
                  onChange={event => setDraftMessage(event.target.value)}
                  placeholder={translate('feedback.messagePlaceholder')}
                />
              </label>
              <div className="action-row">
                {onEscalate && activeThread.kind === 'ServiceReview' ? (
                  <button type="button" className="secondary-button" onClick={() => onEscalate(activeThread)}>
                    {translate('feedback.escalate')}
                  </button>
                ) : null}
                <button type="submit">{translate('feedback.send')}</button>
              </div>
            </form>
          </div>
        ) : null}
      </div>
    </section>
  )
}
