import { useEffect, useState } from 'react'

import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'

type MiniFeedbackChatDialogProps = {
  isOpen: boolean
  thread: FeedbackThread | null
  senderDisplayName: string
  translate: (translationKey: string) => string
  onClose: () => void
  onSendMessage: (threadId: string, body: string) => void
}

export function MiniFeedbackChatDialog({
  isOpen,
  thread,
  senderDisplayName,
  translate,
  onClose,
  onSendMessage,
}: MiniFeedbackChatDialogProps) {
  const [draftMessage, setDraftMessage] = useState('')

  useEffect(() => {
    if (!isOpen) {
      setDraftMessage('')
    }
  }, [isOpen])

  if (!isOpen || !thread) {
    return null
  }

  return (
    <div className="dialog-backdrop">
      <section className="dialog-card feedback-mini-dialog">
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('feedback.quickChat')}</p>
            <h3>{thread.title}</h3>
          </div>
          <button type="button" className="secondary-button" onClick={onClose}>
            {translate('tourGroups.cancel')}
          </button>
        </div>

        <div className="feedback-message-list feedback-message-list-compact">
          {thread.messages.slice(-3).map(message => (
            <article
              key={message.messageId}
              className={message.senderDisplayName === senderDisplayName ? 'feedback-message-card is-self' : 'feedback-message-card'}
            >
              <strong>{message.senderDisplayName}</strong>
              <p>{message.body}</p>
            </article>
          ))}
        </div>

        <form
          className="stack-form feedback-composer"
          onSubmit={event => {
            event.preventDefault()
            if (!thread || draftMessage.trim().length === 0) {
              return
            }
            onSendMessage(thread.threadId, draftMessage)
            setDraftMessage('')
          }}
        >
          <label>
            {translate('feedback.message')}
            <textarea
              rows={3}
              value={draftMessage}
              onChange={event => setDraftMessage(event.target.value)}
              placeholder={translate('feedback.quickChatPlaceholder')}
            />
          </label>
          <div className="action-row">
            <button type="submit">{translate('feedback.send')}</button>
          </div>
        </form>
      </section>
    </div>
  )
}
