import { useEffect, useMemo, useRef, useState } from 'react'

import type { FeedbackMessageResponse } from '@/microservices/content/objects/FeedbackMessageResponse'
import { FeedbackConversationActions } from './FeedbackConversationActions'
import { FeedbackConversationEmptyState } from './FeedbackConversationEmptyState'
import { FeedbackConversationHeader } from './FeedbackConversationHeader'
import { FeedbackConversationMessageFlow } from './FeedbackConversationMessageFlow'
import { FeedbackConversationThreadList } from './FeedbackConversationThreadList'
import { FeedbackComplaintPreviewModal } from './FeedbackComplaintPreviewModal'
import type { FeedbackConversationWorkspaceProps } from './FeedbackConversationWorkspace.types'
import { getThreadIdentity, managerTypeToOrderCategory } from './FeedbackConversationWorkspace.utils'

export function FeedbackConversationWorkspace({
  audience,
  audienceDisplayName,
  audienceAvatarUrl,
  emptyTitle,
  emptyDescription,
  threads,
  fullScreen = false,
  preferredThreadId,
  cancellationOrders = [],
  supportIdentityOverrides = {},
  translate,
  unreadCountSelector,
  onThreadChange,
  onMarkRead,
  onSendMessage,
  onCreateCancellationRequest,
  onHandleCancellationRequest,
  onEscalate,
  onCreateComplaint,
  onOpenComplaintManagerThread,
}: FeedbackConversationWorkspaceProps) {
  const [activeThreadId, setActiveThreadId] = useState<string | null>(threads[0]?.threadId ?? null)
  const [draftMessage, setDraftMessage] = useState('')
  const [complaintMode, setComplaintMode] = useState(false)
  const [selectedComplaintMessageIds, setSelectedComplaintMessageIds] = useState<string[]>([])
  const [complaintExplanation, setComplaintExplanation] = useState('')
  const [complaintPreviewMessage, setComplaintPreviewMessage] = useState<FeedbackMessageResponse | null>(null)
  const [showCancellationForm, setShowCancellationForm] = useState(false)
  const [cancellationOrderId, setCancellationOrderId] = useState('')
  const [managerNotes, setManagerNotes] = useState<Record<string, string>>({})
  const lastNotifiedThreadIdRef = useRef<string | null>(null)
  void fullScreen

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

  const activeThread = useMemo(() => threads.find(thread => thread.threadId === activeThreadId) ?? null, [activeThreadId, threads])
  const cancellationOrderTitleById = useMemo(() => new Map(cancellationOrders.map(order => [order.orderId, order.title || order.orderId])), [cancellationOrders])
  const activeIdentity = activeThread ? getThreadIdentity(activeThread, audience, translate, supportIdentityOverrides, cancellationOrderTitleById) : null
  const visibleCancellationOrders = useMemo(() => {
    if (!activeThread) return cancellationOrders
    const threadCategory = managerTypeToOrderCategory(activeThread.managerType)
    const categoryOrders = threadCategory ? cancellationOrders.filter(order => order.category === threadCategory) : cancellationOrders
    if (activeThread.orderId) {
      const matchedOrders = categoryOrders.filter(order => order.orderId === activeThread.orderId)
      if (matchedOrders.length > 0) return matchedOrders
    }
    if (threadCategory) return categoryOrders
    const resourceTitle = activeThread.resourceSummaryTitle.trim()
    if (resourceTitle.length === 0) return categoryOrders
    return categoryOrders.filter(order => order.title.includes(resourceTitle) || resourceTitle.includes(order.title))
  }, [activeThread, cancellationOrders])

  useEffect(() => {
    const nextThreadId = activeThread?.threadId ?? null
    if (lastNotifiedThreadIdRef.current === nextThreadId) {
      return
    }
    lastNotifiedThreadIdRef.current = nextThreadId
    onThreadChange?.(activeThread)
  }, [activeThread, onThreadChange])

  useEffect(() => {
    if (!activeThread) return
    void onMarkRead(activeThread.threadId, audience)
  }, [activeThread?.threadId, audience, onMarkRead])

  useEffect(() => {
    if (cancellationOrderId && !visibleCancellationOrders.some(order => order.orderId === cancellationOrderId)) {
      setCancellationOrderId('')
    }
  }, [cancellationOrderId, visibleCancellationOrders])

  function resetComposerState() {
    setDraftMessage('')
    setShowCancellationForm(false)
    setCancellationOrderId('')
  }

  function resetComplaintState() {
    setComplaintMode(false)
    setSelectedComplaintMessageIds([])
    setComplaintExplanation('')
    setComplaintPreviewMessage(null)
  }

  function handleThreadSelect(threadId: string) {
    setActiveThreadId(threadId)
    resetComposerState()
    resetComplaintState()
  }

  function toggleComplaintMode() {
    setComplaintMode(value => !value)
    setSelectedComplaintMessageIds([])
    setComplaintExplanation('')
    setShowCancellationForm(false)
  }

  if (threads.length === 0) {
    return <FeedbackConversationEmptyState title={emptyTitle} description={emptyDescription} />
  }

  return (
    <section className="mx-auto grid min-h-[calc(100vh-8rem)] w-full max-w-6xl overflow-hidden border border-slate-200 bg-white text-slate-950 shadow-sm shadow-slate-200/70">
      <div className="grid min-h-0 grid-cols-[19rem_minmax(0,1fr)]">
        <FeedbackConversationThreadList
          threads={threads}
          activeThreadId={activeThreadId}
          audience={audience}
          translate={translate}
          supportIdentityOverrides={supportIdentityOverrides}
          cancellationOrderTitleById={cancellationOrderTitleById}
          unreadCountSelector={unreadCountSelector}
          onThreadSelect={handleThreadSelect}
        />

        {activeThread && activeIdentity ? (
          <div className="grid min-h-0 grid-rows-[4.5rem_minmax(0,1fr)_auto] bg-white">
            <FeedbackConversationHeader
              audience={audience}
              activeThread={activeThread}
              activeIdentity={activeIdentity}
              complaintMode={complaintMode}
              translate={translate}
              onEscalate={onEscalate}
              onToggleComplaintMode={toggleComplaintMode}
            />

            <FeedbackConversationMessageFlow
              activeThread={activeThread}
              audience={audience}
              audienceDisplayName={audienceDisplayName}
              audienceAvatarUrl={audienceAvatarUrl}
              activeIdentity={activeIdentity}
              complaintMode={complaintMode}
              selectedComplaintMessageIds={selectedComplaintMessageIds}
              onToggleComplaintMessageSelection={messageId => {
                setSelectedComplaintMessageIds(previous =>
                  previous.includes(messageId) ? previous.filter(nextMessageId => nextMessageId !== messageId) : [...previous, messageId],
                )
              }}
              managerNotes={managerNotes}
              onManagerNoteChange={(messageId, note) => setManagerNotes(previous => ({ ...previous, [messageId]: note }))}
              onHandleCancellationRequest={onHandleCancellationRequest}
              onOpenComplaintManagerThread={onOpenComplaintManagerThread}
            />

            <FeedbackConversationActions
              activeThread={activeThread}
              complaintMode={complaintMode}
              draftMessage={draftMessage}
              onDraftMessageChange={setDraftMessage}
              showCancellationForm={showCancellationForm}
              onToggleCancellationForm={() => {
                setShowCancellationForm(value => !value)
              }}
              cancellationOrderId={cancellationOrderId}
              onCancellationOrderIdChange={setCancellationOrderId}
              visibleCancellationOrders={visibleCancellationOrders}
              onCreateCancellationRequest={onCreateCancellationRequest}
              onSendMessage={onSendMessage}
              onSendDraft={body => {
                void onSendMessage(activeThread.threadId, body)
                setDraftMessage('')
              }}
              selectedComplaintMessageCount={selectedComplaintMessageIds.length}
              complaintExplanation={complaintExplanation}
              onComplaintExplanationChange={setComplaintExplanation}
              onSubmitComplaint={async () => {
                if (!onCreateComplaint) return
                if (selectedComplaintMessageIds.length === 0 || complaintExplanation.trim().length === 0) return
                const nextThread = await onCreateComplaint({
                  sourceThreadId: activeThread.threadId,
                  selectedMessageIds: selectedComplaintMessageIds,
                  userExplanation: complaintExplanation,
                  userDisplayName: audienceDisplayName,
                })
                if (nextThread && typeof nextThread === 'object' && 'threadId' in nextThread) {
                  setActiveThreadId(nextThread.threadId)
                }
                resetComplaintState()
              }}
              onCloseComplaintMode={resetComplaintState}
              onDraftReset={resetComposerState}
            />
          </div>
        ) : null}
      </div>

      {complaintPreviewMessage?.complaintPayload ? (
        <FeedbackComplaintPreviewModal message={complaintPreviewMessage} onClose={() => setComplaintPreviewMessage(null)} />
      ) : null}
    </section>
  )
}
