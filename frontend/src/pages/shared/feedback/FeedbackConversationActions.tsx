import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'
import { FeedbackConversationInputArea } from './FeedbackConversationInputArea'

export function FeedbackConversationActions({
  activeThread,
  complaintMode,
  draftMessage,
  onDraftMessageChange,
  showCancellationForm,
  onToggleCancellationForm,
  cancellationOrderId,
  onCancellationOrderIdChange,
  visibleCancellationOrders,
  onCreateCancellationRequest,
  onSendMessage,
  onSendDraft,
  selectedComplaintMessageCount,
  complaintExplanation,
  onComplaintExplanationChange,
  onSubmitComplaint,
  onCloseComplaintMode,
  onDraftReset,
}: {
  activeThread: FeedbackThread
  complaintMode: boolean
  draftMessage: string
  onDraftMessageChange: (value: string) => void
  showCancellationForm: boolean
  onToggleCancellationForm: () => void
  cancellationOrderId: string
  onCancellationOrderIdChange: (value: string) => void
  visibleCancellationOrders: Array<{ orderId: string; title: string }>
  onCreateCancellationRequest?: (threadId: string, orderId: string, reason: string) => void | Promise<unknown>
  onSendMessage: (threadId: string, body: string) => void | Promise<unknown>
  onSendDraft: (body: string) => void
  selectedComplaintMessageCount: number
  complaintExplanation: string
  onComplaintExplanationChange: (value: string) => void
  onSubmitComplaint: () => void
  onCloseComplaintMode: () => void
  onDraftReset: () => void
}) {
  return (
    <footer className="border-t border-slate-200 bg-white p-4">
      {complaintMode ? (
        <form
          className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
          onSubmit={event => {
            event.preventDefault()
            onSubmitComplaint()
          }}
        >
          <div className="flex flex-wrap items-center justify-between gap-3">
            <strong className="text-base text-slate-950">已选择 {selectedComplaintMessageCount} 条消息</strong>
            <button
              type="button"
              className="min-h-9 border border-slate-300 bg-white px-3 text-sm font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white"
              onClick={onCloseComplaintMode}
            >
              退出
            </button>
          </div>
          <textarea
            className="min-h-24 resize-none border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
            rows={3}
            value={complaintExplanation}
            onChange={event => onComplaintExplanationChange(event.target.value)}
            placeholder="请说明你要投诉的问题。"
            required
          />
          <button
            className="justify-self-end min-h-10 border border-pink-500 bg-pink-500 px-6 py-2 text-sm font-bold text-white hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-50"
            type="submit"
            disabled={selectedComplaintMessageCount === 0 || complaintExplanation.trim().length === 0}
          >
            提交给网站管理员
          </button>
        </form>
      ) : null}

      {!complaintMode && onCreateCancellationRequest ? (
        <div className="grid gap-3">
          <button
            type="button"
            className="w-fit min-h-10 border border-pink-500 bg-white px-4 py-2 text-sm font-bold text-pink-600 hover:bg-pink-500 hover:text-white"
            onClick={onToggleCancellationForm}
          >
            申请取消订单
          </button>
          {showCancellationForm ? (
            <form
              className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
              onSubmit={event => {
                event.preventDefault()
                const trimmedMessage = draftMessage.trim()
                if (trimmedMessage.length === 0) return
                if (cancellationOrderId) {
                  void onCreateCancellationRequest(activeThread.threadId, cancellationOrderId, trimmedMessage)
                } else {
                  void onSendMessage(activeThread.threadId, trimmedMessage)
                }
                onDraftReset()
              }}
            >
              <select className="min-h-12 border border-slate-300 bg-white px-3 text-base" value={cancellationOrderId} onChange={event => onCancellationOrderIdChange(event.target.value)}>
                <option value="">请选择订单</option>
                {visibleCancellationOrders.map(order => (
                  <option key={order.orderId} value={order.orderId}>
                    {order.title || order.orderId}
                  </option>
                ))}
              </select>
              <textarea
                className="min-h-28 border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
                rows={4}
                value={draftMessage}
                onChange={event => onDraftMessageChange(event.target.value)}
                placeholder={cancellationOrderId ? '请填写取消原因。' : '不选择订单时，这里会作为普通消息发送。'}
              />
              <button type="submit" className="w-fit min-h-11 border border-pink-500 bg-pink-500 px-5 py-2 font-bold text-white">
                {cancellationOrderId ? '提交取消请求' : '发送'}
              </button>
            </form>
          ) : null}
        </div>
      ) : null}

      {!showCancellationForm && !complaintMode ? (
        <FeedbackConversationInputArea
          draftMessage={draftMessage}
          onDraftMessageChange={onDraftMessageChange}
          onSubmit={() => {
            if (draftMessage.trim().length === 0) return
            onSendDraft(draftMessage)
          }}
          sendLabel="发送消息"
        />
      ) : null}
    </footer>
  )
}
