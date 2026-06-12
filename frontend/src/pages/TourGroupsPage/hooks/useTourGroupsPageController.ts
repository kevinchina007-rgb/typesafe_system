// 本文件定义 TourGroupsPage 页面的状态控制逻辑，负责条件维护、请求触发和动作调度。

import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { TourGroupsPageProps, TourGroupsPanelCommonProps } from '../objects'

export function useTourGroupsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: TourGroupsPageProps): TourGroupsPanelCommonProps {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

  return {
    currentLanguage,
    isBusy,
    signedInUser,
    travelers,
    onNavigate,
    translate,
    // 拉取旅游团列表。
    onListGroups: async () => {
      const response = await travelMvpApiClient.listTourGroups()
      return response.groups
    },
    // 拉取单个旅游团详情。
    onLoadGroupDetails: groupId => travelMvpApiClient.getTourGroup(groupId),
    // 创建旅游团并在需要时先上传封面图。
    onCreateGroup: payload =>
      runPageActionWithResult(
        async () => {
          const { coverImageFile, ...groupPayload } = payload
          const coverImageUrl = coverImageFile
            ? (await travelMvpApiClient.uploadTourGroupCoverImage(payload.organizerUserId, coverImageFile)).publicUrl
            : null
          return travelMvpApiClient.createTourGroup({ ...groupPayload, coverImageUrl })
        },
        translate('tourGroups.createGroup'),
        translate('notice.actionSuccess'),
      ),
    // 加入旅游团。
    onJoinGroup: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.joinTourGroup(groupId, payload),
        translate('tourGroups.joinGroup'),
        translate('notice.actionSuccess'),
      ),
    // 退出旅游团。
    onLeaveGroup: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.leaveTourGroup(groupId, payload),
        translate('tourGroups.leaveGroup'),
        translate('notice.actionSuccess'),
      ),
    // 添加成员旅客。
    onAddMembershipTraveler: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.addTourGroupMembershipTraveler(groupId, payload),
        translate('tourGroups.addMembershipTraveler'),
        translate('notice.actionSuccess'),
      ),
    // 移除成员旅客。
    onRemoveMembershipTraveler: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.removeTourGroupMembershipTraveler(groupId, payload),
        translate('tourGroups.removeMembershipTraveler'),
        translate('notice.actionSuccess'),
      ),
    // 创建行程项。
    onCreatePlanItem: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.createTourGroupPlanItem(groupId, payload),
        translate('tourGroups.createPlanItem'),
        translate('notice.actionSuccess'),
      ),
    // 为行程项创建方案。
    onCreatePlanOption: (planItemId, groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.createTourGroupPlanOption(planItemId, groupId, payload),
        translate('tourGroups.createOption'),
        translate('notice.actionSuccess'),
      ),
    // 创建选择项。
    onCreateSelection: (planItemId, groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.createTourGroupSelection(planItemId, groupId, payload),
        translate('tourGroups.createSelection'),
        translate('notice.actionSuccess'),
      ),
    // 提交选择结果。
    onSubmitSelection: (selectionId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.submitTourGroupSelection(selectionId, payload),
        translate('tourGroups.submitSelection'),
        translate('notice.actionSuccess'),
      ),
    // 确认选择结果。
    onConfirmSelection: (selectionId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.confirmTourGroupSelection(selectionId, payload),
        translate('tourGroups.confirmSelection'),
        translate('notice.actionSuccess'),
      ),
    // 驳回选择结果。
    onRejectSelection: (selectionId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.rejectTourGroupSelection(selectionId, payload),
        translate('tourGroups.rejectSelection'),
        translate('notice.actionSuccess'),
      ),
    // 批量确认选择结果。
    onBatchConfirmSelections: payload =>
      runPageActionWithResult(
        () => travelMvpApiClient.batchConfirmTourGroupSelections(payload),
        translate('tourGroups.batchConfirmSelections'),
        translate('notice.actionSuccess'),
      ),
    // 批量驳回选择结果。
    onBatchRejectSelections: payload =>
      runPageActionWithResult(
        () => travelMvpApiClient.batchRejectTourGroupSelections(payload),
        translate('tourGroups.batchRejectSelections'),
        translate('notice.actionSuccess'),
      ),
    // 移除成员。
    onKickMember: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.kickTourGroupMember(groupId, payload),
        translate('tourGroups.kickMember'),
        translate('notice.actionSuccess'),
      ),
    // 加入黑名单。
    onBlacklistMember: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.blacklistTourGroupMember(groupId, payload),
        translate('tourGroups.blacklistMember'),
        translate('notice.actionSuccess'),
      ),
    // 转让团长。
    onTransferOrganizer: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.transferTourGroupLeader(groupId, payload),
        translate('tourGroups.transferOrganizer'),
        translate('notice.actionSuccess'),
      ),
    // 批量支付选择项。
    onBatchPaySelections: payload =>
      runPageActionWithResult(
        () => travelMvpApiClient.batchPayTourGroupSelections(payload),
        translate('tourGroups.batchPaySelections'),
        translate('notice.actionSuccess'),
      ),
    // 搜索航班资源。
    onSearchFlights: async payload => {
      const flightListResponse = await travelMvpApiClient.searchFlightsPlanner(payload)
      return flightListResponse.flights
    },
    // 搜索酒店资源。
    onSearchHotels: async payload => {
      const hotelListResponse = await travelMvpApiClient.searchHotelsPlanner(payload)
      return hotelListResponse.hotels
    },
    // 搜索列车资源。
    onSearchTrains: async payload => {
      const trainListResponse = await travelMvpApiClient.listTrains(payload)
      return trainListResponse.trains
    },
    // 搜索景点资源并补全详情。
    onSearchAttractions: async payload => {
      const attractionListResponse = await travelMvpApiClient.listAttractions(payload)
      const detailedAttractions = await Promise.all(
        attractionListResponse.attractions.map(async attractionSummary => {
          try {
            return await travelMvpApiClient.getAttraction(attractionSummary.attractionId)
          } catch {
            return attractionSummary
          }
        }),
      )
      return detailedAttractions
    },
    // 跳转到订单页。
    onOpenBookings: async () => {
      await runPageAction(async () => {
        onNavigate('bookings')
      }, translate('nav.bookings'), translate('notice.actionSuccess'))
    },
    // 拉取旅游团聊天设置。
    onLoadChatSettings: groupId => travelMvpApiClient.getTourGroupChatSettings(groupId),
    // 更新旅游团聊天设置。
    onUpdateChatSettings: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.updateTourGroupChatSettings(groupId, payload),
        translate('tourGroups.chatSettings'),
        translate('notice.actionSuccess'),
      ),
    // 拉取会话列表。
    onLoadConversations: groupId => travelMvpApiClient.listTourGroupConversations(groupId),
    // 搜索会话。
    onSearchConversations: async (groupId, query) => {
      const response = await travelMvpApiClient.searchTourGroupConversations(groupId, query)
      return response.conversations
    },
    // 搜索消息。
    onSearchMessages: async (groupId, query) => {
      const response = await travelMvpApiClient.searchTourGroupMessages(groupId, query)
      return response.results
    },
    // 获取或创建私聊会话。
    onGetOrCreateDirectConversation: (groupId, payload) =>
      runPageActionWithResult(
        () => travelMvpApiClient.getOrCreateTourGroupDirectConversation(groupId, payload),
        translate('tourGroups.directMessages'),
        translate('notice.actionSuccess'),
      ),
    // 拉取会话消息。
    onLoadMessages: async conversationId => {
      const response = await travelMvpApiClient.listConversationMessages(conversationId)
      return response.messages
    },
    // 发送会话消息。
    onSendMessage: (conversationId, payload) =>
      runPageActionWithResult(
        async () => {
          const response = await travelMvpApiClient.sendConversationMessage(conversationId, payload)
          return response.messages
        },
        translate('tourGroups.directMessages'),
        translate('notice.actionSuccess'),
      ),
    // 上传会话附件。
    onUploadAttachment: (groupId, conversationId, attachmentFile) =>
      runPageActionWithResult(
        () => travelMvpApiClient.uploadConversationAttachment(groupId, conversationId, attachmentFile),
        translate('tourGroups.attachFile'),
        translate('notice.actionSuccess'),
      ),
    // 标记会话已读。
    onMarkConversationRead: conversationId => travelMvpApiClient.markConversationRead(conversationId),
    // 编辑消息。
    onEditMessage: (messageId, payload) =>
      runPageActionWithResult(
        async () => (await travelMvpApiClient.editConversationMessage(messageId, payload)).messages,
        translate('tourGroups.editMessage'),
        translate('notice.actionSuccess'),
      ),
    // 删除消息。
    onDeleteMessage: messageId =>
      runPageActionWithResult(
        async () => (await travelMvpApiClient.deleteConversationMessage(messageId)).messages,
        translate('tourGroups.deleteMessage'),
        translate('notice.actionSuccess'),
      ),
    // 撤回消息。
    onRecallMessage: messageId =>
      runPageActionWithResult(
        async () => (await travelMvpApiClient.recallConversationMessage(messageId)).messages,
        translate('tourGroups.recallMessage'),
        translate('notice.actionSuccess'),
      ),
    // 添加消息表情反应。
    onAddReaction: (messageId, reactionType) =>
      runPageActionWithResult(
        async () => (await travelMvpApiClient.addConversationReaction(messageId, reactionType)).messages,
        translate('tourGroups.addReaction'),
        translate('notice.actionSuccess'),
      ),
    // 移除消息表情反应。
    onRemoveReaction: (messageId, reactionType) =>
      runPageActionWithResult(
        async () => (await travelMvpApiClient.removeConversationReaction(messageId, reactionType)).messages,
        translate('tourGroups.removeReaction'),
        translate('notice.actionSuccess'),
      ),
    // 更新私聊静音状态。
    onUpdateMuteState: (conversationId, muted) =>
      runPageActionWithResult(
        () => travelMvpApiClient.updateDirectConversationMuteState(conversationId, muted),
        translate('tourGroups.muteConversation'),
        translate('notice.actionSuccess'),
      ),
    // 更新私聊归档状态。
    onUpdateArchiveState: (conversationId, archived) =>
      runPageActionWithResult(
        () => travelMvpApiClient.updateDirectConversationArchiveState(conversationId, archived),
        translate('tourGroups.archiveConversation'),
        translate('notice.actionSuccess'),
      ),
  }
}
