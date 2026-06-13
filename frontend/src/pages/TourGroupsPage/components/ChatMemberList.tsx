import type { Dispatch, SetStateAction } from 'react'

import type {
  TourGroupChatSettingsResponse,
  TourGroupConversationListResponse,
  TourGroupConversationSummaryResponse,
  TourGroupMembershipResponse,
  TourGroupMessageSearchResultResponse,
} from '@/lib/mvp-types/index'

import { getMemberDisplayNameMap, getUserDisplayName, localizeConversationTitle } from '@/pages/TourGroupsPage/components/TourGroupChatPanel.utils'

type ChatMemberListProps = {
  translate: (translationKey: string) => string
  groupId: string
  organizerUserId: string
  memberships: TourGroupMembershipResponse[]
  signedInUserId?: string
  signedInUserNickname?: string
  isBusy: boolean
  chatSettings: TourGroupChatSettingsResponse | null
  groupChatConversation: TourGroupConversationSummaryResponse | null
  effectiveConversationId: string | null
  targetMessageId: string | null
  directTargetUserId: string
  setDirectTargetUserId: (value: string) => void
  messageQuery: string
  setMessageQuery: (value: string) => void
  messageSearchResults: TourGroupMessageSearchResultResponse[]
  onSetTargetMessageId: Dispatch<SetStateAction<string | null>>
  onSearchMessages: (groupId: string, query: string) => Promise<TourGroupMessageSearchResultResponse[]>
  onGetOrCreateDirectConversation: (groupId: string, payload: { targetUserId: string }) => Promise<TourGroupConversationSummaryResponse>
  onSetMessageSearchResults: (value: TourGroupMessageSearchResultResponse[]) => void
  onSetConversationList: Dispatch<SetStateAction<TourGroupConversationListResponse | null>>
  onSetActiveConversationId: Dispatch<SetStateAction<string | null>>
}

export function ChatMemberList({
  translate,
  groupId,
  organizerUserId,
  memberships,
  signedInUserId,
  signedInUserNickname,
  isBusy,
  chatSettings,
  groupChatConversation,
  effectiveConversationId,
  targetMessageId,
  directTargetUserId,
  setDirectTargetUserId,
  messageQuery,
  setMessageQuery,
  messageSearchResults,
  onSetTargetMessageId,
  onSearchMessages,
  onGetOrCreateDirectConversation,
  onSetMessageSearchResults,
  onSetConversationList,
  onSetActiveConversationId,
}: ChatMemberListProps) {
  const memberDisplayNameMap = getMemberDisplayNameMap(memberships, signedInUserId, signedInUserNickname)
  const selectableDirectTargets =
    signedInUserId === undefined
      ? []
      : memberships.filter(membership => {
          if (membership.status !== 'Active') return false
          if (membership.userId === signedInUserId) return false
          if (signedInUserId === organizerUserId) return true
          if (membership.userId === organizerUserId) return true
          return chatSettings?.allowMemberDirectChat ?? false
        })

  const defaultDirectTargetUserId = (() => {
    if (!signedInUserId) return ''
    if (signedInUserId !== organizerUserId && selectableDirectTargets.some(target => target.userId === organizerUserId)) {
      return organizerUserId
    }
    return selectableDirectTargets.find(target => target.userId !== signedInUserId)?.userId ?? ''
  })()

  return (
    <aside className="grid gap-4 border border-slate-200 bg-white p-4">
      <div className="grid gap-2">
        <label>{translate('tourGroups.groupChat')}</label>
        <button
          className={
            groupChatConversation?.conversationId === effectiveConversationId
              ? 'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 border-black bg-black text-white'
              : 'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'
          }
          type="button"
          disabled={isBusy || !groupChatConversation}
          onClick={() => {
            if (!groupChatConversation) return
            onSetActiveConversationId(groupChatConversation.conversationId)
          }}
        >
          {translate('tourGroups.groupChat')}
        </button>
      </div>

      <div className="grid gap-4">
        <label>{translate('tourGroups.searchMessages')}</label>
        <div className="grid gap-4 md:grid-cols-2">
          <input
            type="search"
            autoComplete="off"
            spellCheck={false}
            placeholder={translate('tourGroups.searchMessages')}
            value={messageQuery}
            onChange={event => setMessageQuery(event.target.value)}
          />
          <button
            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
            type="button"
            disabled={isBusy || !messageQuery.trim()}
            onClick={async () => onSetMessageSearchResults(await onSearchMessages(groupId, messageQuery))}
          >
            {translate('blog.confirmSearch')}
          </button>
        </div>
      </div>

      <div className="grid gap-4">
        <label>{translate('tourGroups.startChatWith')}</label>
        <select value={directTargetUserId} onChange={event => setDirectTargetUserId(event.target.value)}>
          <option value="">{translate('tourGroups.selectMemberToChat')}</option>
          {selectableDirectTargets.map(target => (
            <option key={target.userId} value={target.userId}>
              {target.userId === organizerUserId ? translate('tourGroups.messageOrganizer') : getUserDisplayName(target.userId, organizerUserId, memberDisplayNameMap, translate)}
            </option>
          ))}
        </select>
        <button
          className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
          type="button"
          disabled={isBusy || !directTargetUserId}
          onClick={async () => {
            if (!directTargetUserId) return
            try {
              const conversation = await onGetOrCreateDirectConversation(groupId, { targetUserId: directTargetUserId })
              onSetMessageSearchResults([])
              onSetConversationList(current =>
                current
                  ? {
                      ...current,
                      conversations: [conversation, ...current.conversations.filter(item => item.conversationId !== conversation.conversationId)],
                    }
                  : { conversations: [conversation], groupChatConversationId: null },
              )
              onSetActiveConversationId(conversation.conversationId)
            } catch {
              setDirectTargetUserId(defaultDirectTargetUserId)
              onSetActiveConversationId(groupChatConversation?.conversationId ?? null)
            }
          }}
        >
          {translate('tourGroups.openConversation')}
        </button>
      </div>

      {messageSearchResults.length > 0 ? (
        <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
          {messageSearchResults.map(result => (
            <button
              key={result.message.messageId}
              type="button"
              className={
                result.message.messageId === targetMessageId
                  ? 'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 border-black bg-black text-white'
                  : 'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'
              }
              onClick={() => {
                onSetMessageSearchResults([])
                onSetTargetMessageId(result.message.messageId)
                onSetActiveConversationId(result.conversationId)
              }}
            >
              {localizeConversationTitle(translate, result.conversationTitle)}: {result.message.content}
            </button>
          ))}
        </div>
      ) : null}
    </aside>
  )
}
