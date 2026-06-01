import { useEffect, useMemo, useState } from 'react'

import type { GroupPlanItemResponse, TourGroupDetailsResponse, TourGroupSummaryResponse } from '@/lib/mvp-types/index'
import { CreateTourGroupDialog } from '@/pages/TourGroupsPage/components/CreateTourGroupDialog'
import { TourGroupDetail } from '@/pages/TourGroupsPage/components/TourGroupDetail'
import { TourGroupList } from '@/pages/TourGroupsPage/components/TourGroupList'
import { TourGroupSelectionDialog } from '@/pages/TourGroupsPage/components/TourGroupSelectionDialog'
import type { TourGroupsPanelCommonProps } from '../objects'
import {
  findNewestSelection,
  getActiveMembership,
  getMembershipTravelerIds,
  getSelectionDialogOptions,
  getSelectionDialogTravelers,
  pickCreatedPlanItem,
  pickInitialActivePlanItem,
  syncGroupSummary,
} from '../functions'

export function TourGroupsPanel({
  currentLanguage,
  isBusy,
  signedInUser,
  travelers,
  onNavigate,
  translate,
  onListGroups,
  onLoadGroupDetails,
  onCreateGroup,
  onJoinGroup,
  onLeaveGroup,
  onAddMembershipTraveler,
  onCreatePlanItem,
  onCreatePlanOption,
  onCreateSelection,
  onSubmitSelection,
  onConfirmSelection,
  onRejectSelection,
  onBatchConfirmSelections,
  onBatchRejectSelections,
  onKickMember,
  onBlacklistMember,
  onTransferOrganizer,
  onBatchPaySelections,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
  onOpenBookings,
  onLoadChatSettings,
  onUpdateChatSettings,
  onLoadConversations,
  onSearchConversations,
  onSearchMessages,
  onGetOrCreateDirectConversation,
  onLoadMessages,
  onSendMessage,
  onUploadAttachment,
  onMarkConversationRead,
  onEditMessage,
  onDeleteMessage,
  onRecallMessage,
  onAddReaction,
  onRemoveReaction,
  onUpdateMuteState,
  onUpdateArchiveState,
  pageMode = 'home',
}: TourGroupsPanelCommonProps) {
  const [groupSummaries, setGroupSummaries] = useState<TourGroupSummaryResponse[]>([])
  const [groupDetailsCache, setGroupDetailsCache] = useState<Record<string, TourGroupDetailsResponse>>({})
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null)
  const [selectedGroupDetails, setSelectedGroupDetails] = useState<TourGroupDetailsResponse | null>(null)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [activeOrganizerPlanItem, setActiveOrganizerPlanItem] = useState<GroupPlanItemResponse | null>(null)
  const [selectionPlanItem, setSelectionPlanItem] = useState<GroupPlanItemResponse | null>(null)

  useEffect(() => {
    void (async () => {
      const groups = await onListGroups()
      setGroupSummaries(groups)
      const detailsEntries = await Promise.all(
        groups.map(async group => {
          const details = await onLoadGroupDetails(group.groupId)
          return [group.groupId, details] as const
        }),
      )
      setGroupDetailsCache(Object.fromEntries(detailsEntries))
      if (groups.length > 0) {
        setSelectedGroupId(currentGroupId => currentGroupId ?? groups[0].groupId)
      }
    })()
  }, [onListGroups, onLoadGroupDetails])

  useEffect(() => {
    if (!selectedGroupId) {
      setSelectedGroupDetails(null)
      setActiveOrganizerPlanItem(null)
      return
    }

    void (async () => {
      const details = await onLoadGroupDetails(selectedGroupId)
      applyUpdatedGroupDetails(details)
    })()
  }, [onLoadGroupDetails, selectedGroupId])

  const activeMembership = useMemo(() => getActiveMembership(selectedGroupDetails, signedInUser), [selectedGroupDetails, signedInUser])

  const membershipTravelerIds = useMemo(
    () => getMembershipTravelerIds(selectedGroupDetails, activeMembership?.membershipId),
    [activeMembership?.membershipId, selectedGroupDetails],
  )

  const selectionDialogOptions = useMemo(
    () => getSelectionDialogOptions(selectedGroupDetails, selectionPlanItem),
    [selectedGroupDetails, selectionPlanItem],
  )
  const selectionDialogTravelers = useMemo(
    () => getSelectionDialogTravelers(travelers, membershipTravelerIds),
    [membershipTravelerIds, travelers],
  )

  const visibleGroupSummaries = useMemo(() => {
    if (pageMode !== 'mine' || !signedInUser) {
      return groupSummaries
    }

    return groupSummaries.filter(group => {
      if (group.organizerUserId === signedInUser.userId) {
        return true
      }

      const details = groupDetailsCache[group.groupId]
      return details?.memberships.some(membership => membership.userId === signedInUser.userId && membership.status === 'Active') ?? false
    })
  }, [groupDetailsCache, groupSummaries, pageMode, signedInUser?.userId])

  useEffect(() => {
    if (visibleGroupSummaries.length === 0) {
      return
    }
    if (!selectedGroupId || !visibleGroupSummaries.some(group => group.groupId === selectedGroupId)) {
      setSelectedGroupId(visibleGroupSummaries[0].groupId)
    }
  }, [selectedGroupId, visibleGroupSummaries])

  function applyUpdatedGroupDetails(details: TourGroupDetailsResponse) {
    setSelectedGroupDetails(details)
    setSelectedGroupId(details.group.groupId)
    setGroupDetailsCache(currentCache => ({ ...currentCache, [details.group.groupId]: details }))
    setGroupSummaries(currentGroups => syncGroupSummary(currentGroups, details))
    setActiveOrganizerPlanItem(currentPlanItem => {
      return pickInitialActivePlanItem(details, currentPlanItem)
    })
  }

  async function createSelectionAndOptionallySubmit(
    planItemId: string,
    payload: { optionId: string; quantity: number; travelerIds: string[] },
    submitAfterCreate: boolean,
  ) {
    if (!selectedGroupDetails || !signedInUser) {
      return
    }

    const createdDetails = await onCreateSelection(planItemId, selectedGroupDetails.group.groupId, {
      userId: signedInUser.userId,
      optionId: payload.optionId,
      quantity: payload.quantity,
      travelerIds: payload.travelerIds,
    })

    let nextDetails = createdDetails

    if (submitAfterCreate) {
      const membershipId = activeMembership?.membershipId
      const newestSelection = findNewestSelection(createdDetails, planItemId, payload.optionId, membershipId)

      if (newestSelection) {
        nextDetails = await onSubmitSelection(newestSelection.selectionId, { userId: signedInUser.userId })
      }
    }

    applyUpdatedGroupDetails(nextDetails)
    setSelectionPlanItem(null)
  }

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('nav.tourGroups')}</p>
          <h2>{translate('tourGroups.title')}</h2>
        </div>
        <button
          type="button"
          className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
          disabled={isBusy}
          onClick={async () => {
            const groups = await onListGroups()
            setGroupSummaries(groups)
            if (selectedGroupId) {
              const details = await onLoadGroupDetails(selectedGroupId)
              applyUpdatedGroupDetails(details)
            }
          }}
        >
          {translate('tourGroups.refresh')}
        </button>
      </div>

      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.description')}</p>

      <div className="grid gap-5 xl:grid-cols-[18rem_1fr]">
        <TourGroupList
          currentLanguage={currentLanguage}
          groups={visibleGroupSummaries}
          selectedGroupId={selectedGroupId}
          signedInUser={signedInUser}
          isBusy={isBusy}
          translate={translate}
          onOpenCreateDialog={() => setIsCreateDialogOpen(true)}
          onSelectGroup={setSelectedGroupId}
        />

        <div className="grid gap-4">
          {selectedGroupDetails ? (
            <TourGroupDetail
              currentLanguage={currentLanguage}
              details={selectedGroupDetails}
              signedInUser={signedInUser}
              travelers={travelers}
              isBusy={isBusy}
              translate={translate}
              onJoinGroup={async () => {
                if (!signedInUser) return
                const details = await onJoinGroup(selectedGroupDetails.group.groupId, { userId: signedInUser.userId })
                applyUpdatedGroupDetails(details)
              }}
              onLeaveGroup={async () => {
                if (!signedInUser) return
                const details = await onLeaveGroup(selectedGroupDetails.group.groupId, { userId: signedInUser.userId })
                applyUpdatedGroupDetails(details)
              }}
              onAddMembershipTraveler={async travelerId => {
                if (!signedInUser) return
                const details = await onAddMembershipTraveler(selectedGroupDetails.group.groupId, {
                  userId: signedInUser.userId,
                  travelerId,
                })
                applyUpdatedGroupDetails(details)
              }}
              onCreatePlanItem={async payload => {
                if (!signedInUser) return null
                const previousPlanItemIds = new Set(selectedGroupDetails.planItems.map(planItem => planItem.planItemId))
                const details = await onCreatePlanItem(selectedGroupDetails.group.groupId, {
                  organizerUserId: signedInUser.userId,
                  ...payload,
                })
                applyUpdatedGroupDetails(details)
                const createdPlanItem = pickCreatedPlanItem(details, previousPlanItemIds)
                if (createdPlanItem) {
                  setActiveOrganizerPlanItem(createdPlanItem)
                }
                return createdPlanItem
              }}
              activePlanItem={activeOrganizerPlanItem}
              onSelectPlanItem={setActiveOrganizerPlanItem}
              onCreateOption={async (planItemId, payload) => {
                if (!signedInUser || !selectedGroupDetails) return
                const details = await onCreatePlanOption(planItemId, selectedGroupDetails.group.groupId, {
                  organizerUserId: signedInUser.userId,
                  ...payload,
                })
                applyUpdatedGroupDetails(details)
              }}
              onOpenChoose={setSelectionPlanItem}
              onSubmitSelection={async selectionId => {
                if (!signedInUser) return
                const details = await onSubmitSelection(selectionId, { userId: signedInUser.userId })
                applyUpdatedGroupDetails(details)
              }}
              onConfirmSelection={async (selectionId, note) => {
                if (!signedInUser) return
                const details = await onConfirmSelection(selectionId, {
                  organizerUserId: signedInUser.userId,
                  reviewNote: note.trim() || null,
                })
                applyUpdatedGroupDetails(details)
              }}
              onRejectSelection={async (selectionId, note) => {
                if (!signedInUser) return
                const details = await onRejectSelection(selectionId, {
                  organizerUserId: signedInUser.userId,
                  reviewNote: note,
                })
                applyUpdatedGroupDetails(details)
              }}
              onBatchConfirmSelections={async selectionIds => {
                if (!signedInUser || selectionIds.length === 0) return
                const details = await onBatchConfirmSelections({
                  organizerUserId: signedInUser.userId,
                  selectionIds,
                  reviewNote: null,
                })
                applyUpdatedGroupDetails(details)
              }}
              onBatchRejectSelections={async (selectionIds, note) => {
                if (!signedInUser || selectionIds.length === 0) return
                const details = await onBatchRejectSelections({
                  organizerUserId: signedInUser.userId,
                  selectionIds,
                  reviewNote: note,
                })
                applyUpdatedGroupDetails(details)
              }}
              onKickMember={async targetUserId => {
                if (!signedInUser) return
                const details = await onKickMember(selectedGroupDetails.group.groupId, {
                  organizerUserId: signedInUser.userId,
                  targetUserId,
                })
                applyUpdatedGroupDetails(details)
              }}
              onBlacklistMember={async targetUserId => {
                if (!signedInUser) return
                const details = await onBlacklistMember(selectedGroupDetails.group.groupId, {
                  organizerUserId: signedInUser.userId,
                  targetUserId,
                })
                applyUpdatedGroupDetails(details)
              }}
              onTransferOrganizer={async targetUserId => {
                if (!signedInUser) return
                const details = await onTransferOrganizer(selectedGroupDetails.group.groupId, {
                  organizerUserId: signedInUser.userId,
                  targetUserId,
                })
                applyUpdatedGroupDetails(details)
              }}
              onOpenBookings={async () => {
                await onOpenBookings()
              }}
              onBatchPaySelections={async selectionIds => {
                if (!signedInUser || selectionIds.length === 0) return
                const result = await onBatchPaySelections({
                  userId: signedInUser.userId,
                  selectionIds,
                  paymentMethod: 'Wallet',
                })
                applyUpdatedGroupDetails(result.group)
              }}
              onSearchFlights={onSearchFlights}
              onSearchHotels={onSearchHotels}
              onSearchTrains={onSearchTrains}
              onSearchAttractions={onSearchAttractions}
              onLoadChatSettings={onLoadChatSettings}
              onUpdateChatSettings={onUpdateChatSettings}
              onLoadConversations={onLoadConversations}
              onSearchConversations={onSearchConversations}
              onSearchMessages={onSearchMessages}
              onGetOrCreateDirectConversation={onGetOrCreateDirectConversation}
              onLoadMessages={onLoadMessages}
              onSendMessage={onSendMessage}
              onUploadAttachment={onUploadAttachment}
              onMarkConversationRead={onMarkConversationRead}
              onEditMessage={onEditMessage}
              onDeleteMessage={onDeleteMessage}
              onRecallMessage={onRecallMessage}
              onAddReaction={onAddReaction}
              onRemoveReaction={onRemoveReaction}
              onUpdateMuteState={onUpdateMuteState}
              onUpdateArchiveState={onUpdateArchiveState}
              onNavigate={onNavigate}
            />
          ) : (
            <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
              <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.selectGroupHint')}</p>
            </section>
          )}
        </div>
      </div>

      <CreateTourGroupDialog
        isOpen={isCreateDialogOpen}
        isBusy={isBusy}
        translate={translate}
        onClose={() => setIsCreateDialogOpen(false)}
        onCreateGroup={async payload => {
          if (!signedInUser) return
          const details = await onCreateGroup({
            organizerUserId: signedInUser.userId,
            ...payload,
          })
          applyUpdatedGroupDetails(details)
          setIsCreateDialogOpen(false)
        }}
      />

      <TourGroupSelectionDialog
        currentLanguage={currentLanguage}
        isOpen={selectionPlanItem !== null}
        isBusy={isBusy}
        planItem={selectionPlanItem}
        options={selectionDialogOptions}
        availableTravelers={selectionDialogTravelers}
        translate={translate}
        onClose={() => setSelectionPlanItem(null)}
        onSaveDraft={async payload => {
          if (!selectionPlanItem) return
          await createSelectionAndOptionallySubmit(selectionPlanItem.planItemId, payload, false)
        }}
        onSaveAndSubmit={async payload => {
          if (!selectionPlanItem) return
          await createSelectionAndOptionallySubmit(selectionPlanItem.planItemId, payload, true)
        }}
      />
    </section>
  )
}
