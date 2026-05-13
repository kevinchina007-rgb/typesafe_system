import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
﻿import { TourGroupsPanel } from '@/pages/TourGroupsPage/components/TourGroupsPanel'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, UserResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'

type TourGroupsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function TourGroupsPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: TourGroupsPageProps) {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

  return (
    <TourGroupsPanel
      currentLanguage={currentLanguage}
      isBusy={isBusy}
      signedInUser={signedInUser}
      travelers={travelers}
      translate={translate}
      onListGroups={async () => {
        const response = await travelMvpApiClient.listTourGroups()
        return response.groups
      }}
      onLoadGroupDetails={groupId => travelMvpApiClient.getTourGroup(groupId)}
      onCreateGroup={payload =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroup(payload),
          translate('tourGroups.createGroup'),
          translate('notice.actionSuccess'),
        )
      }
      onJoinGroup={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.joinTourGroup(groupId, payload),
          translate('tourGroups.joinGroup'),
          translate('notice.actionSuccess'),
        )
      }
      onAddMembershipTraveler={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.addTourGroupMembershipTraveler(groupId, payload),
          translate('tourGroups.addMembershipTraveler'),
          translate('notice.actionSuccess'),
        )
      }
      onCreatePlanItem={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroupPlanItem(groupId, payload),
          translate('tourGroups.createPlanItem'),
          translate('notice.actionSuccess'),
        )
      }
      onCreatePlanOption={(planItemId, groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroupPlanOption(planItemId, groupId, payload),
          translate('tourGroups.createOption'),
          translate('notice.actionSuccess'),
        )
      }
      onCreateSelection={(planItemId, groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroupSelection(planItemId, groupId, payload),
          translate('tourGroups.createSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onSubmitSelection={(selectionId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.submitTourGroupSelection(selectionId, payload),
          translate('tourGroups.submitSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onConfirmSelection={(selectionId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.confirmTourGroupSelection(selectionId, payload),
          translate('tourGroups.confirmSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onRejectSelection={(selectionId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.rejectTourGroupSelection(selectionId, payload),
          translate('tourGroups.rejectSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onBatchConfirmSelections={payload =>
        runPageActionWithResult(
          () => travelMvpApiClient.batchConfirmTourGroupSelections(payload),
          translate('tourGroups.batchConfirmSelections'),
          translate('notice.actionSuccess'),
        )
      }
      onBatchRejectSelections={payload =>
        runPageActionWithResult(
          () => travelMvpApiClient.batchRejectTourGroupSelections(payload),
          translate('tourGroups.batchRejectSelections'),
          translate('notice.actionSuccess'),
        )
      }
      onBatchPaySelections={payload =>
        runPageActionWithResult(
          () => travelMvpApiClient.batchPayTourGroupSelections(payload),
          translate('tourGroups.batchPaySelections'),
          translate('notice.actionSuccess'),
        )
      }
      onSearchFlights={async payload => {
        const flightListResponse = await travelMvpApiClient.listFlights(payload)
        return flightListResponse.flights
      }}
      onSearchHotels={async payload => {
        const hotelListResponse = await travelMvpApiClient.listHotels(payload)
        return hotelListResponse.hotels
      }}
      onSearchTrains={async payload => {
        const trainListResponse = await travelMvpApiClient.listTrains(payload)
        return trainListResponse.trains
      }}
      onSearchAttractions={async payload => {
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
      }}
      onOpenBookings={async () => {
        await runPageAction(async () => {
          onNavigate('bookings')
        }, translate('nav.bookings'), translate('notice.actionSuccess'))
      }}
      onLoadChatSettings={groupId => travelMvpApiClient.getTourGroupChatSettings(groupId)}
      onUpdateChatSettings={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.updateTourGroupChatSettings(groupId, payload),
          translate('tourGroups.chatSettings'),
          translate('notice.actionSuccess'),
        )
      }
      onLoadConversations={groupId => travelMvpApiClient.listTourGroupConversations(groupId)}
      onSearchConversations={async (groupId, query) => {
        const response = await travelMvpApiClient.searchTourGroupConversations(groupId, query)
        return response.conversations
      }}
      onSearchMessages={async (groupId, query) => {
        const response = await travelMvpApiClient.searchTourGroupMessages(groupId, query)
        return response.results
      }}
      onGetOrCreateDirectConversation={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.getOrCreateTourGroupDirectConversation(groupId, payload),
          translate('tourGroups.directMessages'),
          translate('notice.actionSuccess'),
        )
      }
      onLoadMessages={async conversationId => {
        const response = await travelMvpApiClient.listConversationMessages(conversationId)
        return response.messages
      }}
      onSendMessage={(conversationId, payload) =>
        runPageActionWithResult(
          async () => {
            const response = await travelMvpApiClient.sendConversationMessage(conversationId, payload)
            return response.messages
          },
          translate('tourGroups.directMessages'),
          translate('notice.actionSuccess'),
        )
      }
      onUploadAttachment={(groupId, conversationId, attachmentFile) =>
        runPageActionWithResult(
          () => travelMvpApiClient.uploadConversationAttachment(groupId, conversationId, attachmentFile),
          translate('tourGroups.attachFile'),
          translate('notice.actionSuccess'),
        )
      }
      onMarkConversationRead={conversationId => travelMvpApiClient.markConversationRead(conversationId)}
      onEditMessage={(messageId, payload) =>
        runPageActionWithResult(
          async () => (await travelMvpApiClient.editConversationMessage(messageId, payload)).messages,
          translate('tourGroups.editMessage'),
          translate('notice.actionSuccess'),
        )
      }
      onDeleteMessage={messageId =>
        runPageActionWithResult(
          async () => (await travelMvpApiClient.deleteConversationMessage(messageId)).messages,
          translate('tourGroups.deleteMessage'),
          translate('notice.actionSuccess'),
        )
      }
      onRecallMessage={messageId =>
        runPageActionWithResult(
          async () => (await travelMvpApiClient.recallConversationMessage(messageId)).messages,
          translate('tourGroups.recallMessage'),
          translate('notice.actionSuccess'),
        )
      }
      onAddReaction={(messageId, reactionType) =>
        runPageActionWithResult(
          async () => (await travelMvpApiClient.addConversationReaction(messageId, reactionType)).messages,
          translate('tourGroups.addReaction'),
          translate('notice.actionSuccess'),
        )
      }
      onRemoveReaction={(messageId, reactionType) =>
        runPageActionWithResult(
          async () => (await travelMvpApiClient.removeConversationReaction(messageId, reactionType)).messages,
          translate('tourGroups.removeReaction'),
          translate('notice.actionSuccess'),
        )
      }
      onUpdateMuteState={(conversationId, muted) =>
        runPageActionWithResult(
          () => travelMvpApiClient.updateDirectConversationMuteState(conversationId, muted),
          translate('tourGroups.muteConversation'),
          translate('notice.actionSuccess'),
        )
      }
      onUpdateArchiveState={(conversationId, archived) =>
        runPageActionWithResult(
          () => travelMvpApiClient.updateDirectConversationArchiveState(conversationId, archived),
          translate('tourGroups.archiveConversation'),
          translate('notice.actionSuccess'),
        )
      }
    />
  )
}
