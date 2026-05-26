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
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6">
      <section className="grid max-h-[90vh] w-full max-w-3xl gap-4 overflow-auto border border-slate-200 bg-white p-6 text-slate-950 shadow-2xl shadow-slate-950/20 fixed bottom-6 right-6 z-40 grid w-96 max-w-[calc(100vw-3rem)] gap-3 border border-slate-200 bg-white p-4 shadow-2xl">
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('feedback.quickChat')}</p>
            <h3>{thread.title}</h3>
          </div>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={onClose}>
            {translate('tourGroups.cancel')}
          </button>
        </div>

        <div className="grid gap-3 max-h-80 overflow-auto">
          {thread.messages.slice(-3).map(message => (
            <article
              key={message.messageId}
              className={message.senderDisplayName === senderDisplayName ? 'grid gap-2 border border-slate-200 bg-white p-3 justify-self-end bg-sky-50' : 'grid gap-2 border border-slate-200 bg-white p-3'}
            >
              <strong>{message.senderDisplayName}</strong>
              <p>{message.messageType === 'orderCancellationRequest' ? '取消订单请求' : message.content}</p>
            </article>
          ))}
        </div>

        <form
          className="grid gap-4 grid gap-3"
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
            />
          </label>
          <div className="flex flex-wrap items-center gap-3">
            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit">{translate('feedback.send')}</button>
          </div>
        </form>
      </section>
    </div>
  )
}
