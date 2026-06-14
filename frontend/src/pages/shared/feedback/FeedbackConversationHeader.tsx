import type { FeedbackAudience } from '@/microservices/content/objects/FeedbackAudience'
import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'
import { FeedbackConversationAvatar } from './FeedbackConversationAvatar'
import type { ChatIdentity } from './FeedbackConversationWorkspace.types'

export function FeedbackConversationHeader({
  audience,
  activeThread,
  activeIdentity,
  complaintMode,
  translate,
  onEscalate,
  onToggleComplaintMode,
}: {
  audience: FeedbackAudience
  activeThread: FeedbackThread
  activeIdentity: ChatIdentity
  complaintMode: boolean
  translate: (translationKey: string) => string
  onEscalate?: (thread: FeedbackThread) => void | Promise<unknown>
  onToggleComplaintMode: () => void
}) {
  const subtitle = activeThread.managerType === 'Hotel'
    ? activeThread.resourceSummaryTitle.replace(/\s+路\s+/g, ' · ').replace(/\s*·\s*/g, ' · ').trim() || activeThread.subtitle
    : activeThread.resourceSummaryTitle || activeThread.subtitle

  return (
    <header className="flex items-center justify-between border-b border-slate-200 px-6">
      <div className="flex min-w-0 items-center gap-3">
        <FeedbackConversationAvatar imageUrl={activeIdentity.logoPath} fallback={activeIdentity.fallback} alt={activeIdentity.name} />
        <div className="min-w-0">
          <h3 className="m-0 truncate text-xl font-bold text-slate-950">{activeIdentity.name}</h3>
          <p className="m-0 truncate text-sm text-slate-500">{subtitle}</p>
        </div>
      </div>
      {onEscalate && activeThread.kind === 'ServiceReview' ? (
        <button type="button" className="min-h-10 border border-slate-300 bg-white px-4 text-sm font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white" onClick={() => onEscalate(activeThread)}>
          {translate('feedback.escalate')}
        </button>
      ) : null}
      {audience === 'User' && activeThread.kind === 'ServiceReview' && activeThread.managerType !== 'SiteAdmin' ? (
        <button
          type="button"
          className={complaintMode ? 'min-h-10 border border-black bg-black px-4 text-sm font-bold text-white' : 'min-h-10 border border-slate-300 bg-white px-4 text-sm font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white'}
          onClick={onToggleComplaintMode}
        >
          {complaintMode ? '退出投诉' : '投诉'}
        </button>
      ) : null}
    </header>
  )
}
