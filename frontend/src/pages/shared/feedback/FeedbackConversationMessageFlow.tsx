import { useEffect, useRef } from 'react'

import type { FeedbackAudience } from '@/microservices/feedback/objects/FeedbackAudience'
import type { FeedbackMessageResponse } from '@/microservices/feedback/objects/FeedbackMessageResponse'
import type { FeedbackThread } from '@/microservices/feedback/objects/FeedbackThread'
import type { OrderCancellationRequestStatus } from '@/microservices/feedback/objects/OrderCancellationRequestPayload'
import { FeedbackConversationAvatar } from './FeedbackConversationAvatar'
import { formatCenterTime, isOwnMessage, localizeCancellationStatus, shouldShowTimeMarker } from './FeedbackConversationWorkspace.utils'
import type { ChatIdentity } from './FeedbackConversationWorkspace.types'

export function FeedbackConversationMessageFlow({
  activeThread,
  audience,
  audienceDisplayName,
  audienceAvatarUrl,
  activeIdentity,
  complaintMode,
  selectedComplaintMessageIds,
  onToggleComplaintMessageSelection,
  managerNotes,
  onManagerNoteChange,
  onHandleCancellationRequest,
  onOpenComplaintManagerThread,
}: {
  activeThread: FeedbackThread
  audience: FeedbackAudience
  audienceDisplayName: string
  audienceAvatarUrl?: string | null
  activeIdentity: ChatIdentity
  complaintMode: boolean
  selectedComplaintMessageIds: string[]
  onToggleComplaintMessageSelection: (messageId: string) => void
  managerNotes: Record<string, string>
  onManagerNoteChange: (messageId: string, note: string) => void
  onHandleCancellationRequest?: (
    threadId: string,
    messageId: string,
    status: Exclude<OrderCancellationRequestStatus, 'pending'>,
    managerNote: string,
  ) => void | Promise<unknown>
  onOpenComplaintManagerThread?: (complaintMessageId: string) => Promise<FeedbackThread | void> | FeedbackThread | void
}) {
  const bottomRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: 'end' })
  }, [activeThread.threadId, activeThread.messages.length])

  return (
    <div className="min-h-0 overflow-y-auto bg-slate-50 px-6 py-5">
      <div className="grid gap-4">
        {activeThread.messages.map((message, index) => {
          const ownMessage = isOwnMessage(message.senderRole, audience)
          const senderName = ownMessage && audience === 'User' ? audienceDisplayName : message.senderDisplayName
          const counterpartyAvatar =
            activeThread.kind === 'ManagerEscalation' && message.senderRole === 'SiteAdmin'
              ? { imageUrl: activeThread.siteAdminActorLogoAssetPath ?? null, fallback: '管', useBackendAsset: true }
              : activeThread.kind === 'ManagerEscalation' && message.senderRole === 'Manager'
                ? { imageUrl: activeThread.managerActorLogoAssetPath ?? null, fallback: senderName.slice(0, 1) || '管', useBackendAsset: true }
                : audience === 'Manager'
                  ? { imageUrl: null, fallback: senderName.slice(0, 1) || '客', useBackendAsset: false }
                  : { imageUrl: activeIdentity.logoPath, fallback: activeIdentity.fallback, useBackendAsset: false }
          const avatar = ownMessage
            ? { imageUrl: audienceAvatarUrl, fallback: audienceDisplayName.slice(0, 1) || '我', useBackendAsset: true }
            : counterpartyAvatar
          const canSelectForComplaint = complaintMode && message.messageType !== 'system'
          const isSelectedForComplaint = selectedComplaintMessageIds.includes(message.messageId)

          return (
            <ConversationMessageItem
              key={message.messageId}
              message={message}
              index={index}
              ownMessage={ownMessage}
              senderName={senderName}
              avatar={avatar}
              canSelectForComplaint={canSelectForComplaint}
              isSelectedForComplaint={isSelectedForComplaint}
              activeThread={activeThread}
              audience={audience}
              managerNote={managerNotes[message.messageId] ?? ''}
              onToggleComplaintMessageSelection={onToggleComplaintMessageSelection}
              onManagerNoteChange={note => onManagerNoteChange(message.messageId, note)}
              onHandleCancellationRequest={onHandleCancellationRequest}
              onOpenComplaintManagerThread={onOpenComplaintManagerThread}
            />
          )
        })}
        <div ref={bottomRef} />
      </div>
    </div>
  )
}

function ConversationMessageItem({
  message,
  index,
  ownMessage,
  senderName,
  avatar,
  canSelectForComplaint,
  isSelectedForComplaint,
  activeThread,
  audience,
  managerNote,
  onToggleComplaintMessageSelection,
  onManagerNoteChange,
  onHandleCancellationRequest,
  onOpenComplaintManagerThread,
}: {
  message: FeedbackMessageResponse
  index: number
  ownMessage: boolean
  senderName: string
  avatar: { imageUrl: string | null | undefined; fallback: string; useBackendAsset: boolean }
  canSelectForComplaint: boolean
  isSelectedForComplaint: boolean
  activeThread: FeedbackThread
  audience: FeedbackAudience
  managerNote: string
  onToggleComplaintMessageSelection: (messageId: string) => void
  onManagerNoteChange: (note: string) => void
  onHandleCancellationRequest?: (
    threadId: string,
    messageId: string,
    status: Exclude<OrderCancellationRequestStatus, 'pending'>,
    managerNote: string,
  ) => void | Promise<unknown>
  onOpenComplaintManagerThread?: (complaintMessageId: string) => Promise<FeedbackThread | void> | FeedbackThread | void
}) {
  return (
    <div key={message.messageId} className={canSelectForComplaint ? 'grid grid-cols-[2rem_minmax(0,1fr)] items-start gap-3' : 'grid gap-3'}>
      {canSelectForComplaint ? (
        <button
          type="button"
          aria-label={isSelectedForComplaint ? '取消选择这条消息' : '选择这条消息'}
          className={isSelectedForComplaint ? 'mt-8 flex h-6 w-6 items-center justify-center rounded-full bg-sky-600 text-sm font-bold text-white' : 'mt-8 h-6 w-6 rounded-full border-2 border-slate-300 bg-white'}
          onClick={() => onToggleComplaintMessageSelection(message.messageId)}
        >
          {isSelectedForComplaint ? '✓' : ''}
        </button>
      ) : null}
      <div className="grid gap-3">
        {shouldShowTimeMarker(activeThread.messages, index) ? (
          <div className="justify-self-center bg-slate-200 px-3 py-1 text-xs font-semibold text-slate-500">{formatCenterTime(message.createdAt)}</div>
        ) : null}

        {message.messageType === 'system' ? (
          <div className="justify-self-center bg-white px-4 py-2 text-sm font-semibold text-slate-500 shadow-sm shadow-slate-200/50">{message.content}</div>
        ) : null}

        {message.messageType === 'orderCancellationRequest' && message.payload ? (
          <div className={ownMessage ? 'grid grid-cols-[minmax(0,1fr)_2.75rem] gap-3 justify-self-end' : 'grid grid-cols-[2.75rem_minmax(0,1fr)] gap-3 justify-self-start'}>
            {!ownMessage ? <FeedbackConversationAvatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
            <article className="grid max-w-xl gap-3 border border-pink-200 bg-white p-4 shadow-sm shadow-pink-100">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="m-0 text-xs font-bold text-pink-600">{senderName}</p>
                  <h4 className="m-0 text-lg font-bold text-slate-950">取消订单请求</h4>
                </div>
                <span className="border border-pink-200 bg-pink-50 px-3 py-1 text-sm font-bold text-pink-700">{localizeCancellationStatus(message.payload.status)}</span>
              </div>
              <div className="grid gap-1 text-sm leading-6 text-slate-600">
                <span>订单编号：{message.payload.orderId}</span>
                <span>订单名称：{message.payload.orderTitle ?? '订单'}</span>
                <span>取消原因：{message.payload.reason}</span>
                {message.payload.requestedRefundAmount !== null ? <span>预计可退：{message.payload.requestedRefundAmount}</span> : null}
                {message.payload.managerNote ? <span>客服备注：{message.payload.managerNote}</span> : null}
                {message.payload.status === 'pending' && audience === 'User' ? <strong className="text-pink-700">等待客服处理</strong> : null}
              </div>
              {audience !== 'User' && message.payload.status === 'pending' && onHandleCancellationRequest ? (
                <div className="grid gap-3">
                  <textarea
                    rows={3}
                    value={managerNote}
                    onChange={event => onManagerNoteChange(event.target.value)}
                    className="min-h-20 border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
                  />
                  <div className="flex flex-wrap gap-3">
                    {(['approved', 'rejected', 'needMoreInfo'] as const).map(nextStatus => (
                      <button
                        key={nextStatus}
                        type="button"
                        className={nextStatus === 'approved' ? 'min-h-10 border border-pink-500 bg-pink-500 px-4 py-2 font-bold text-white' : 'min-h-10 border border-slate-300 bg-white px-4 py-2 font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white'}
                        onClick={() => void onHandleCancellationRequest(activeThread.threadId, message.messageId, nextStatus, managerNote)}
                      >
                        {nextStatus === 'approved' ? '同意取消' : nextStatus === 'rejected' ? '拒绝取消' : '需要补充信息'}
                      </button>
                    ))}
                  </div>
                </div>
              ) : null}
            </article>
            {ownMessage ? <FeedbackConversationAvatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
          </div>
        ) : null}

        {message.messageType === 'complaintCard' && message.complaintPayload ? (
          <div className={ownMessage ? 'grid grid-cols-[minmax(0,1fr)_2.75rem] gap-3 justify-self-end' : 'grid grid-cols-[2.75rem_minmax(0,1fr)] gap-3 justify-self-start'}>
            {!ownMessage ? <FeedbackConversationAvatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
            <article className="grid w-[26rem] max-w-full gap-3 border border-slate-200 bg-white p-4 text-left shadow-sm shadow-slate-200/70">
              <button type="button" className="grid gap-2 text-left" onClick={() => void onOpenComplaintManagerThread?.(message.messageId)}>
                <span className="text-xs font-bold text-slate-500">投诉记录</span>
                <strong className="text-lg leading-tight text-slate-950">投诉对象：{message.complaintPayload.targetDisplayName}</strong>
                <span className="line-clamp-2 text-sm leading-6 text-slate-600">用户说明：{message.complaintPayload.userExplanation}</span>
                <span className="line-clamp-2 text-sm leading-6 text-slate-500">摘要：{message.complaintPayload.summary}</span>
                <span className="text-sm font-bold text-pink-600">查看{message.complaintPayload.selectedMessages.length}条投诉消息</span>
              </button>
              {audience === 'SiteAdmin' && onOpenComplaintManagerThread ? (
                <button
                  type="button"
                  className="min-h-10 border border-black bg-black px-4 py-2 text-sm font-bold text-white hover:bg-white hover:text-black"
                  onClick={() => void onOpenComplaintManagerThread(message.messageId)}
                >
                  与被投诉者对话
                </button>
              ) : null}
            </article>
            {ownMessage ? <FeedbackConversationAvatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
          </div>
        ) : null}

        {message.messageType === 'text' ? (
          <div className={ownMessage ? 'grid grid-cols-[minmax(0,1fr)_2.75rem] gap-3 justify-self-end' : 'grid grid-cols-[2.75rem_minmax(0,1fr)] gap-3 justify-self-start'}>
            {!ownMessage ? <FeedbackConversationAvatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
            <div className={ownMessage ? 'grid justify-items-end gap-1' : 'grid justify-items-start gap-1'}>
              <span className="text-xs font-bold text-slate-500">{senderName}</span>
              <article className={ownMessage ? 'max-w-xl bg-sky-100 p-3 text-slate-950' : 'max-w-xl bg-white p-3 text-slate-950 shadow-sm shadow-slate-200/60'}>
                <p className="m-0 whitespace-pre-wrap text-base leading-7">{message.content}</p>
              </article>
            </div>
            {ownMessage ? <FeedbackConversationAvatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
          </div>
        ) : null}
      </div>
    </div>
  )
}
