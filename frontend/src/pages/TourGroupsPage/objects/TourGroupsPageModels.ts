// 本文件定义 TourGroupsPage 页面数据模型和 JSON 编解码。

import type { AppLanguage, AppViewKey, AttractionResponse, FlightPlannerResponse, HotelPlannerResponse, TrainPlannerResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

export type TourGroupsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export type TourGroupsPageMode = 'home' | 'mine'

export type TourGroupsPageRegionKey = 'header' | 'groupList' | 'groupDetail' | 'dialogs'

export type TourGroupsPageRegion = {
  key: TourGroupsPageRegionKey
  title: string
  description: string
}

export const TOUR_GROUPS_PAGE_REGIONS: TourGroupsPageRegion[] = [
  {
    key: 'header',
    title: '页面标题区',
    description: '显示旅游团页标题和当前页面说明。',
  },
  {
    key: 'groupList',
    title: '团组列表区',
    description: '展示可浏览、切换和刷新团组的列表。',
  },
  {
    key: 'groupDetail',
    title: '团组详情区',
    description: '展示团组计划、成员、选择和订单操作。',
  },
  {
    key: 'dialogs',
    title: '弹窗区',
    description: '承载创建团组与选择提交弹窗。',
  },
]

export type TourGroupsPanelCommonProps = {
  pageMode?: TourGroupsPageMode
  refreshToken?: number
  isBusy: boolean
  currentLanguage: AppLanguage
  travelers: TravelerResponse[]
  signedInUser: UserResponse | null
  onNavigate: (viewKey: AppViewKey) => void
  translate: (translationKey: string) => string
  onListGroups: () => Promise<import('@/lib/mvp-types/index').TourGroupSummaryResponse[]>
  onLoadGroupDetails: (groupId: string) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onCreateGroup: (payload: {
    organizerUserId: string
    title: string
    description: string
    destination: string
    startDate: string
    endDate: string
    capacity: number
    coverImageFile?: File | null
    tags?: string[]
  }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onJoinGroup: (groupId: string, payload: { userId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onLeaveGroup: (groupId: string, payload: { userId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onAddMembershipTraveler: (groupId: string, payload: { userId: string; travelerId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onRemoveMembershipTraveler: (groupId: string, payload: { userId: string; travelerId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onCreatePlanItem: (groupId: string, payload: {
    organizerUserId: string
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onCreatePlanOption: (planItemId: string, groupId: string, payload: {
    organizerUserId: string
    resourceType: string
    resourceId: string
    resourceVariantCode?: string | null
    resourceContext?: string | null
    label: string
    description: string
    defaultQuantity: number
  }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onCreateSelection: (planItemId: string, groupId: string, payload: {
    userId: string
    optionId: string
    quantity: number
    travelerIds: string[]
  }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onSubmitSelection: (selectionId: string, payload: { userId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onConfirmSelection: (selectionId: string, payload: { organizerUserId: string; reviewNote?: string | null }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onRejectSelection: (selectionId: string, payload: { organizerUserId: string; reviewNote: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onBatchConfirmSelections: (payload: {
    organizerUserId: string
    selectionIds: string[]
    reviewNote?: string | null
  }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onBatchRejectSelections: (payload: {
    organizerUserId: string
    selectionIds: string[]
    reviewNote: string
  }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onKickMember: (groupId: string, payload: { organizerUserId: string; targetUserId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onBlacklistMember: (groupId: string, payload: { organizerUserId: string; targetUserId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onTransferOrganizer: (groupId: string, payload: { organizerUserId: string; targetUserId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupDetailsResponse>
  onBatchPaySelections: (payload: {
    userId: string
    selectionIds: string[]
    paymentMethod: string
  }) => Promise<{ group: import('@/lib/mvp-types/index').TourGroupDetailsResponse; orders: import('@/lib/mvp-types/index').OrderResponse[] }>
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightPlannerResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelPlannerResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainPlannerResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
  onOpenBookings: () => Promise<void>
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
