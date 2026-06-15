import type { FeedbackAudience } from '@/microservices/feedback/objects/FeedbackAudience'
import type { FeedbackMessageResponse } from '@/microservices/feedback/objects/FeedbackMessageResponse'
import type { FeedbackThread } from '@/microservices/feedback/objects/FeedbackThread'
import type { OrderCancellationRequestStatus } from '@/microservices/feedback/objects/OrderCancellationRequestPayload'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

export type CancellationOrderOption = {
  orderId: string
  title: string
  detailLabel: string
  category: OrderCategory
}

export type SupportIdentityOverride = {
  name: string
  logoPath: string | null
}

export type ChatIdentity = {
  name: string
  logoPath: string | null
  fallback: string
}

export type FeedbackConversationWorkspaceProps = {
  audience: FeedbackAudience
  audienceDisplayName: string
  audienceAvatarUrl?: string | null
  emptyTitle: string
  emptyDescription: string
  threads: FeedbackThread[]
  fullScreen?: boolean
  preferredThreadId?: string | null
  cancellationOrders?: CancellationOrderOption[]
  supportIdentityOverrides?: Record<string, SupportIdentityOverride>
  translate: (translationKey: string) => string
  unreadCountSelector: (thread: FeedbackThread) => number
  onThreadChange?: (thread: FeedbackThread | null) => void
  onMarkRead: (threadId: string, audience: FeedbackAudience) => Promise<unknown> | void
  onSendMessage: (threadId: string, body: string) => Promise<unknown> | void
  onCreateCancellationRequest?: (threadId: string, orderId: string, reason: string) => Promise<unknown> | void
  onHandleCancellationRequest?: (
    threadId: string,
    messageId: string,
    status: Exclude<OrderCancellationRequestStatus, 'pending'>,
    managerNote: string,
  ) => Promise<unknown> | void
  onEscalate?: (thread: FeedbackThread) => Promise<unknown> | void
  onCreateComplaint?: (params: {
    sourceThreadId: string
    selectedMessageIds: string[]
    userExplanation: string
    userDisplayName: string
  }) => Promise<FeedbackThread | void> | FeedbackThread | void
  onOpenComplaintManagerThread?: (complaintMessageId: string) => Promise<FeedbackThread | void> | FeedbackThread | void
}

export type FeedbackConversationRouteThread = {
  thread: FeedbackThread
  unreadCount: number
  identity: ChatIdentity
  preview: string
  timeLabel: string
  isActive: boolean
}

export type FeedbackMessageComposerState = {
  draftMessage: string
  setDraftMessage: (value: string) => void
  showCancellationForm: boolean
  setShowCancellationForm: (value: boolean | ((current: boolean) => boolean)) => void
  cancellationOrderId: string
  setCancellationOrderId: (value: string) => void
}

export type FeedbackComplaintState = {
  complaintMode: boolean
  setComplaintMode: (value: boolean | ((current: boolean) => boolean)) => void
  selectedComplaintMessageIds: string[]
  setSelectedComplaintMessageIds: (value: string[] | ((current: string[]) => string[])) => void
  complaintExplanation: string
  setComplaintExplanation: (value: string) => void
  complaintPreviewMessage: FeedbackMessageResponse | null
  setComplaintPreviewMessage: (value: FeedbackMessageResponse | null) => void
  managerNotes: Record<string, string>
  setManagerNotes: (value: Record<string, string> | ((current: Record<string, string>) => Record<string, string>)) => void
}
