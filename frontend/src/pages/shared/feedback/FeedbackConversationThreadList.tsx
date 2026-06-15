import type { FeedbackAudience } from '@/microservices/feedback/objects/FeedbackAudience'
import type { FeedbackThread } from '@/microservices/feedback/objects/FeedbackThread'
import { FeedbackConversationAvatar } from './FeedbackConversationAvatar'
import { formatListTime, getLastMessage, getThreadPreview, getThreadIdentity } from './FeedbackConversationWorkspace.utils'
import type { SupportIdentityOverride } from './FeedbackConversationWorkspace.types'

export function FeedbackConversationThreadList({
  threads,
  activeThreadId,
  audience,
  translate,
  supportIdentityOverrides,
  cancellationOrderTitleById,
  unreadCountSelector,
  onThreadSelect,
}: {
  threads: FeedbackThread[]
  activeThreadId: string | null
  audience: FeedbackAudience
  translate: (translationKey: string) => string
  supportIdentityOverrides: Record<string, SupportIdentityOverride>
  cancellationOrderTitleById: Map<string, string>
  unreadCountSelector: (thread: FeedbackThread) => number
  onThreadSelect: (threadId: string) => void
}) {
  return (
    <aside className="min-h-0 border-r border-slate-300 bg-slate-50">
      <div className="border-b border-slate-200 p-4">
        <h2 className="m-0 text-2xl font-bold text-slate-950">鐎广垺婀囬崣宥夘洯</h2>
      </div>

      <div className="grid">
        {threads.map(thread => {
          const unreadCount = unreadCountSelector(thread)
          const identity = getThreadIdentity(thread, audience, translate, supportIdentityOverrides, cancellationOrderTitleById)
          const lastMessage = getLastMessage(thread)
          const isActive = thread.threadId === activeThreadId
          return (
            <button
              key={thread.threadId}
              type="button"
              className={`grid grid-cols-[2.75rem_minmax(0,1fr)_3.5rem] items-center gap-3 border-b border-slate-200 p-4 text-left transition ${
                isActive ? 'bg-white' : 'bg-slate-50 hover:bg-white'
              }`}
              onClick={() => onThreadSelect(thread.threadId)}
            >
              <FeedbackConversationAvatar imageUrl={identity.logoPath} fallback={identity.fallback} alt={identity.name} />
              <span className="grid min-w-0 gap-1">
                <span className="truncate text-base font-bold text-slate-950">{identity.name}</span>
                <span className="truncate text-sm text-slate-500">{getThreadPreview(thread)}</span>
              </span>
              <span className="grid justify-items-end gap-2">
                <span className="text-xs text-slate-400">{formatListTime(lastMessage?.createdAt ?? thread.updatedAt)}</span>
                {unreadCount > 0 ? <span className="min-w-5 bg-pink-500 px-1.5 py-0.5 text-center text-xs font-bold text-white">{unreadCount}</span> : null}
              </span>
            </button>
          )
        })}
      </div>
    </aside>
  )
}

