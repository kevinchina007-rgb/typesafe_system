import type { FeedbackMessageResponse } from '@/microservices/feedback/objects/FeedbackMessageResponse'
import { formatCenterTime } from './FeedbackConversationWorkspace.utils'

export function FeedbackComplaintPreviewModal({
  message,
  onClose,
}: {
  message: FeedbackMessageResponse
  onClose: () => void
}) {
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/45 p-6" role="dialog" aria-modal="true">
      <section className="grid max-h-[80vh] w-full max-w-2xl grid-rows-[auto_minmax(0,1fr)_auto] overflow-hidden bg-white text-slate-950 shadow-2xl">
        <header className="flex items-center justify-between border-b border-slate-200 p-4">
          <div>
            <p className="m-0 text-sm font-bold text-slate-500">投诉消息</p>
            <h3 className="m-0 text-xl font-bold">{message.complaintPayload?.targetDisplayName}</h3>
          </div>
          <button type="button" className="h-10 w-10 bg-black text-lg font-bold text-white" onClick={onClose}>
            ×
          </button>
        </header>
        <div className="min-h-0 overflow-y-auto bg-slate-50 p-5">
          <div className="mb-4 grid gap-2 border border-slate-200 bg-white p-4">
            <strong>用户说明</strong>
            <p className="m-0 whitespace-pre-wrap text-sm leading-6 text-slate-600">{message.complaintPayload?.userExplanation}</p>
          </div>
          <div className="grid gap-4">
            {message.complaintPayload?.selectedMessages.map((snapshot, index, snapshots) => (
              <div key={`${snapshot.messageId}-${index}`} className="grid gap-3">
                {index === 0 || formatCenterTime(snapshot.createdAt) !== formatCenterTime(snapshots[index - 1]?.createdAt ?? '') ? (
                  <div className="justify-self-center bg-slate-200 px-3 py-1 text-xs font-semibold text-slate-500">{formatCenterTime(snapshot.createdAt)}</div>
                ) : null}
                <div className={snapshot.senderRole === 'User' ? 'grid justify-items-end gap-1' : 'grid justify-items-start gap-1'}>
                  <span className="text-xs font-bold text-slate-500">{snapshot.senderDisplayName}</span>
                  <article className={snapshot.senderRole === 'User' ? 'max-w-xl bg-sky-100 p-3 text-slate-950' : 'max-w-xl bg-white p-3 text-slate-950 shadow-sm shadow-slate-200/60'}>
                    <p className="m-0 whitespace-pre-wrap text-base leading-7">{snapshot.content}</p>
                  </article>
                </div>
              </div>
            ))}
          </div>
        </div>
        <footer className="border-t border-slate-200 p-4 text-sm text-slate-500">以上为用户选择提交给网站管理员的聊天记录。</footer>
      </section>
    </div>
  )
}

