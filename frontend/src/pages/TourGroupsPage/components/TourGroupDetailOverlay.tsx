import { X } from 'lucide-react'

import type { AppLanguage, AppViewKey, AttractionResponse, FlightPlannerResponse, GroupPlanItemResponse, HotelPlannerResponse, TourGroupDetailsResponse, TrainResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { TourGroupDetail } from '@/pages/TourGroupsPage/components/TourGroupDetail'

type TourGroupDetailOverlayProps = {
  currentLanguage: AppLanguage
  details: TourGroupDetailsResponse
  signedInUser: UserResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onClose: () => void
  onJoinGroup: () => Promise<void>
  onLeaveGroup: () => Promise<void>
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
  onNavigate: (viewKey: AppViewKey) => void
  onCreatePlanItem: (payload: {
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<GroupPlanItemResponse | null>
  activePlanItem: GroupPlanItemResponse | null
  onSelectPlanItem: (planItem: GroupPlanItemResponse) => void
  onCreateOption: (
    planItemId: string,
    payload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) => Promise<void>
  onOpenChoose: (planItem: GroupPlanItemResponse) => void
  onSubmitSelection: (selectionId: string) => Promise<void>
  onConfirmSelection: (selectionId: string, note: string) => Promise<void>
  onRejectSelection: (selectionId: string, note: string) => Promise<void>
  onBatchConfirmSelections: (selectionIds: string[]) => Promise<void>
  onBatchRejectSelections: (selectionIds: string[], note: string) => Promise<void>
  onKickMember: (targetUserId: string) => Promise<void>
  onBlacklistMember: (targetUserId: string) => Promise<void>
  onTransferOrganizer: (targetUserId: string) => Promise<void>
  onBatchPaySelections: (selectionIds: string[]) => Promise<void>
  onOpenBookings: () => void
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightPlannerResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelPlannerResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
  onLoadChatSettings: (groupId: string) => Promise<import('@/lib/mvp-types/index').TourGroupChatSettingsResponse>
  onUpdateChatSettings: (groupId: string, payload: { allowMemberDirectChat: boolean }) => Promise<import('@/lib/mvp-types/index').TourGroupChatSettingsResponse>
  onLoadConversations: (groupId: string) => Promise<import('@/lib/mvp-types/index').TourGroupConversationListResponse>
  onSearchConversations: (groupId: string, query: string) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse[]>
  onSearchMessages: (groupId: string, query: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageSearchResultResponse[]>
  onGetOrCreateDirectConversation: (groupId: string, payload: { targetUserId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
  onLoadMessages: (conversationId: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onSendMessage: (conversationId: string, payload: {
    messageType?: string
    content: string
    replyToMessageId?: string | null
    attachments?: import('@/lib/mvp-types/index').TourGroupUploadedAttachmentResponse[]
  }) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onUploadAttachment: (groupId: string, conversationId: string, attachmentFile: File) => Promise<import('@/lib/mvp-types/index').TourGroupUploadedAttachmentResponse>
  onMarkConversationRead: (conversationId: string) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
  onEditMessage: (messageId: string, payload: { content: string }) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onDeleteMessage: (messageId: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onRecallMessage: (messageId: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onAddReaction: (messageId: string, reactionType: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onRemoveReaction: (messageId: string, reactionType: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onUpdateMuteState: (conversationId: string, muted: boolean) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
  onUpdateArchiveState: (conversationId: string, archived: boolean) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
}

export function TourGroupDetailOverlay({ onClose, ...props }: TourGroupDetailOverlayProps) {
  return (
    <div className="fixed inset-0 z-[60] overflow-y-auto bg-slate-950/40 px-4 py-6 backdrop-blur-[2px]" onClick={onClose}>
      <div className="mx-auto w-full max-w-6xl" onClick={event => event.stopPropagation()}>
        <div className="mb-4 flex justify-end">
          <button
            type="button"
            className="grid h-11 w-11 place-items-center border border-slate-200 bg-white text-slate-950 shadow-sm transition hover:border-black hover:bg-black hover:text-white"
            onClick={onClose}
            aria-label="close"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>
        <TourGroupDetail {...props} />
      </div>
    </div>
  )
}
