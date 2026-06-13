import { useEffect } from 'react'

import type { ChatMessageItem } from '@/pages/TourGroupsPage/components/TourGroupChatPanel.types'
import { ChatAttachmentPreview } from '@/pages/TourGroupsPage/components/ChatAttachmentPreview'
import { quickReactions } from '@/pages/TourGroupsPage/components/TourGroupChatPanel.utils'

type ChatMessageListProps = {
  translate: (translationKey: string) => string
  messages: ChatMessageItem[]
  targetMessageId: string | null
  onTargetMessageHandled: (messageId: string) => void
  onReplyTarget: (message: ChatMessageItem) => void
  onEditStart: (message: ChatMessageItem) => void
  onDeleteMessage: (messageId: string) => Promise<void>
  onRecallMessage: (messageId: string) => Promise<void>
  onAddReaction: (messageId: string, reactionType: string) => Promise<void>
  onRemoveReaction: (messageId: string, reactionType: string) => Promise<void>
}

export function ChatMessageList({
  translate,
  messages,
  targetMessageId,
  onTargetMessageHandled,
  onReplyTarget,
  onEditStart,
  onDeleteMessage,
  onRecallMessage,
  onAddReaction,
  onRemoveReaction,
}: ChatMessageListProps) {
  useEffect(() => {
    if (!targetMessageId || messages.every(message => message.messageId !== targetMessageId)) {
      return
    }

    const element = document.getElementById(`tour-group-message-${targetMessageId}`)
    if (!element) {
      return
    }

    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    element.classList.add('border-sky-400', 'bg-sky-50')
    const timeoutId = window.setTimeout(() => {
      element.classList.remove('border-sky-400', 'bg-sky-50')
      onTargetMessageHandled(targetMessageId)
    }, 2200)

    return () => window.clearTimeout(timeoutId)
  }, [messages, onTargetMessageHandled, targetMessageId])

  return (
    <div className="grid max-h-[32rem] gap-3 overflow-auto border border-slate-200 bg-white p-4">
      {messages.map(message => (
        <article
          key={message.messageId}
          id={`tour-group-message-${message.messageId}`}
          className={message.isMine ? 'grid gap-2 border border-slate-200 bg-white p-3 justify-self-end bg-sky-50' : 'grid gap-2 border border-slate-200 bg-white p-3'}
        >
          <div className="text-xs text-slate-500">
            <strong>{message.senderDisplayName}</strong>
            <span>{new Date(message.createdAt).toLocaleString('zh-CN')}</span>
          </div>
          {message.replyToPreview ? (
            <p className="text-sm font-medium text-slate-500">
              {translate('tourGroups.replyingTo')}: {message.replyToPreview}
            </p>
          ) : null}
          {message.content ? <p className="text-sm leading-6 text-slate-700">{message.content}</p> : null}
          <ChatAttachmentPreview attachments={message.attachments} />
          <div className="flex flex-wrap items-center gap-2">
            <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => onReplyTarget(message)}>
              {translate('tourGroups.replyMessage')}
            </button>
            {message.canEdit ? (
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => onEditStart(message)}>
                {translate('tourGroups.editMessage')}
              </button>
            ) : null}
            {message.canDelete ? (
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={async () => void onDeleteMessage(message.messageId)}>
                {translate('tourGroups.deleteMessage')}
              </button>
            ) : null}
            {message.canRecall ? (
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={async () => void onRecallMessage(message.messageId)}>
                {translate('tourGroups.recallMessage')}
              </button>
            ) : null}
          </div>
          {message.canReact ? (
            <div className="flex flex-wrap gap-2">
              {quickReactions.map(reactionType => {
                const existingReaction = message.reactions.find(reaction => reaction.reactionType === reactionType)
                return (
                  <button
                    key={reactionType}
                    type="button"
                    className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                    onClick={async () =>
                      existingReaction?.reactedByCurrentUser
                      ? void onRemoveReaction(message.messageId, reactionType)
                      : void onAddReaction(message.messageId, reactionType)
                    }
                  >
                    {reactionType} {existingReaction?.count ?? 0}
                  </button>
                )
              })}
            </div>
          ) : null}
        </article>
      ))}
    </div>
  )
}
