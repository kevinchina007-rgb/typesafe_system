// 本文件定义 TourGroupsPage 页面的面板组件，负责集中展示一组相关内容。

import { useEffect, useMemo, useState, type FormEvent } from 'react'

import type { GroupPlanItemResponse, TourGroupDetailsResponse, TourGroupSummaryResponse } from '@/lib/mvp-types/index'
import { CreateTourGroupDialog } from '@/pages/TourGroupsPage/components/CreateTourGroupDialog'
import { TourGroupDetailOverlay } from '@/pages/TourGroupsPage/components/TourGroupDetailOverlay'
import { TourGroupList } from '@/pages/TourGroupsPage/components/TourGroupList'
import { TourGroupSelectionDialog } from '@/pages/TourGroupsPage/components/TourGroupSelectionDialog'
import type { TourGroupsPanelCommonProps } from '../objects'
import {
  findNewestSelection,
  getActiveMembership,
  getMembershipTravelerIds,
  getSelectionDialogOptions,
  getSelectionDialogTravelers,
  getTourGroupOrderCategoryForSelectionOption,
  pickInitialActivePlanItem,
  scoreTourGroupSummaryForQuery,
  syncGroupSummary,
} from '../functions'

function getGroupIdFromUrl(): string | null {
  if (typeof window === 'undefined') {
    return null
  }

  const groupId = new URLSearchParams(window.location.search).get('groupId')
  return groupId && groupId.trim().length > 0 ? groupId : null
}

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
  onRemoveMembershipTraveler,
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
  refreshToken = 0,
}: TourGroupsPanelCommonProps) {
  const [groupSummaries, setGroupSummaries] = useState<TourGroupSummaryResponse[]>([])
  const [groupDetailsCache, setGroupDetailsCache] = useState<Record<string, TourGroupDetailsResponse>>({})
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null)
  const [selectedGroupDetails, setSelectedGroupDetails] = useState<TourGroupDetailsResponse | null>(null)
  const [isGroupDetailOpen, setIsGroupDetailOpen] = useState(false)
  const [searchInput, setSearchInput] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [activeOrganizerPlanItem, setActiveOrganizerPlanItem] = useState<GroupPlanItemResponse | null>(null)
  const [selectionPlanItem, setSelectionPlanItem] = useState<GroupPlanItemResponse | null>(null)
  const [initialGroupId] = useState(() => getGroupIdFromUrl())

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
      if (initialGroupId && groups.some(group => group.groupId === initialGroupId)) {
        setSelectedGroupId(initialGroupId)
        setIsGroupDetailOpen(true)
      }
    })()
  }, [initialGroupId, onListGroups, onLoadGroupDetails, refreshToken])

  useEffect(() => {
    if (!selectedGroupId) {
      setSelectedGroupDetails(null)
      setActiveOrganizerPlanItem(null)
      setIsGroupDetailOpen(false)
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
    const scopedGroupSummaries =
      pageMode !== 'mine' || !signedInUser
        ? groupSummaries
        : groupSummaries.filter(group => {
            if (group.organizerUserId === signedInUser.userId) {
              return true
            }

            const details = groupDetailsCache[group.groupId]
            return details?.memberships.some(membership => membership.userId === signedInUser.userId && membership.status === 'Active') ?? false
          })

    const query = pageMode === 'home' ? searchQuery : ''

    return scopedGroupSummaries
      .map(group => ({
        group,
        score: scoreTourGroupSummaryForQuery(group, query),
      }))
      .filter(({ score }) => query.trim().length === 0 || score > 0)
      .sort((left, right) => right.score - left.score || right.group.createdAt.localeCompare(left.group.createdAt))
      .map(({ group }) => group)
  }, [groupDetailsCache, groupSummaries, pageMode, searchQuery, signedInUser?.userId])

  function applyUpdatedGroupDetails(details: TourGroupDetailsResponse) {
    setSelectedGroupDetails(details)
    setSelectedGroupId(details.group.groupId)
    setGroupDetailsCache(currentCache => ({ ...currentCache, [details.group.groupId]: details }))
    setGroupSummaries(currentGroups => syncGroupSummary(currentGroups, details))
    setActiveOrganizerPlanItem(currentPlanItem => {
      return pickInitialActivePlanItem(details, currentPlanItem)
    })
  }

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSearchQuery(searchInput.trim())
  }

  async function createSelectionAndOptionallySubmit(
    planItemId: string,
    payload: { optionId: string; quantity: number; travelerIds: string[] },
    submitAfterCreate: boolean,
  ) {
    if (!selectedGroupDetails || !signedInUser) {
      return
    }

    const selectedOption = selectionDialogOptions.find(option => option.optionId === payload.optionId) ?? null
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
    if (submitAfterCreate) {
      const orderCategory = getTourGroupOrderCategoryForSelectionOption(selectedOption)
      if (orderCategory) {
        onNavigate(orderCategory)
      }
    }
    setSelectionPlanItem(null)
  }

  return (
    <section className="grid gap-5 border-y border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-6 text-slate-950 shadow-sm shadow-sky-100/40">
      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.description')}</p>

      {pageMode === 'home' ? (
          <form className="grid gap-3 border border-sky-200 bg-white/80 p-4 text-slate-950 shadow-sm shadow-sky-100/40" onSubmit={handleSearchSubmit}>
          <label className="grid gap-2">
            <span className="text-sm font-semibold text-slate-500">{translate('tourGroups.searchGroups')}</span>
            <input
              value={searchInput}
              placeholder={translate('tourGroups.searchGroupsPlaceholder')}
              onChange={event => setSearchInput(event.target.value)}
            />
          </label>
          <div className="flex flex-wrap items-center gap-3">
            <button
              type="submit"
              className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
              disabled={isBusy}
            >
              {translate('tourGroups.searchGroupsButton')}
            </button>
            <button
              type="button"
              className="inline-flex min-h-11 items-center justify-center border border-sky-200 bg-sky-50 px-4 py-2 text-sm font-semibold text-sky-700 transition hover:border-sky-700 hover:bg-white hover:text-slate-950 disabled:cursor-not-allowed disabled:opacity-55"
              disabled={isBusy}
              onClick={() => {
                setSearchInput('')
                setSearchQuery('')
              }}
            >
              {translate('tourGroups.clearSearch')}
            </button>
          </div>
          <p className="text-sm leading-6 text-slate-500">
            {searchQuery.trim().length > 0
              ? translate('tourGroups.searchGroupsResultsHint').replace('{count}', String(visibleGroupSummaries.length))
              : translate('tourGroups.searchGroupsHint')}
          </p>
        </form>
      ) : null}

      <div className="grid gap-5">
        <TourGroupList
          currentLanguage={currentLanguage}
          groups={visibleGroupSummaries}
          selectedGroupId={selectedGroupId}
          signedInUser={signedInUser}
          isBusy={isBusy}
          translate={translate}
          onOpenCreateDialog={() => setIsCreateDialogOpen(true)}
          onSelectGroup={groupId => {
            setSelectedGroupId(groupId)
            setIsGroupDetailOpen(true)
          }}
        />

        {visibleGroupSummaries.length === 0 ? (
        <section className="grid gap-3 border border-sky-200 bg-white/85 p-4 text-slate-950 shadow-sm shadow-sky-100/40">
          <p className="text-sm leading-6 text-slate-500">
              {searchQuery.trim().length > 0 ? translate('tourGroups.searchGroupsEmpty') : translate('tourGroups.empty')}
            </p>
          </section>
        ) : null}

      </div>

      {isGroupDetailOpen && selectedGroupDetails ? (
        <TourGroupDetailOverlay
          currentLanguage={currentLanguage}
          details={selectedGroupDetails}
          signedInUser={signedInUser}
          travelers={travelers}
          isBusy={isBusy}
          translate={translate}
          onClose={() => setIsGroupDetailOpen(false)}
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
          onRemoveMembershipTraveler={async travelerId => {
            if (!signedInUser) return
            const details = await onRemoveMembershipTraveler(selectedGroupDetails.group.groupId, {
              userId: signedInUser.userId,
              travelerId,
            })
            applyUpdatedGroupDetails(details)
          }}
          activePlanItem={activeOrganizerPlanItem}
          onSelectPlanItem={setActiveOrganizerPlanItem}
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
      ) : null}

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
