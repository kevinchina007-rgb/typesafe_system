import type { Dispatch, SetStateAction } from 'react'

import type { TourGroupChatSettingsResponse, TourGroupConversationListResponse, TourGroupConversationSummaryResponse } from '@/lib/mvp-types/index'

type ChatHeaderProps = {
  translate: (translationKey: string) => string
  isBusy: boolean
  isOrganizer: boolean
  groupId: string
  chatSettings: TourGroupChatSettingsResponse | null
  activeConversation: TourGroupConversationSummaryResponse | null
  onUpdateChatSettings: (groupId: string, payload: { allowMemberDirectChat: boolean }) => Promise<TourGroupChatSettingsResponse>
  onUpdateMuteState: (conversationId: string, muted: boolean) => Promise<TourGroupConversationSummaryResponse>
  onUpdateArchiveState: (conversationId: string, archived: boolean) => Promise<TourGroupConversationSummaryResponse>
  onReload: () => Promise<void>
  onSetConversationList: Dispatch<SetStateAction<TourGroupConversationListResponse | null>>
}

export function ChatHeader({
  translate,
  isBusy,
  isOrganizer,
  groupId,
  chatSettings,
  activeConversation,
  onUpdateChatSettings,
  onUpdateMuteState,
  onUpdateArchiveState,
  onReload,
  onSetConversationList,
}: ChatHeaderProps) {
  return (
    <div className="text-lg font-bold text-slate-950">
      <div>
        <p className="text-sm font-bold text-slate-500">{translate('tourGroups.chatEyebrow')}</p>
        <h3>{translate('tourGroups.chatTitle')}</h3>
      </div>
      {isOrganizer && chatSettings ? (
        <label className="toggle-chip">
          <input
            type="checkbox"
            checked={chatSettings.allowMemberDirectChat}
            disabled={isBusy}
            onChange={async event => {
              const nextSettings = await onUpdateChatSettings(groupId, {
                allowMemberDirectChat: event.target.checked,
              })
              void nextSettings
              await onReload()
            }}
          />
          <span>{translate('tourGroups.allowMemberDirectChat')}</span>
        </label>
      ) : null}

      {activeConversation && activeConversation.conversationType === 'Direct' ? (
        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
            onClick={async () => {
              const nextSummary = await onUpdateMuteState(activeConversation.conversationId, !activeConversation.isMuted)
              onSetConversationList(current =>
                current
                  ? {
                      ...current,
                      conversations: current.conversations.map(conversation =>
                        conversation.conversationId === nextSummary.conversationId ? nextSummary : conversation,
                      ),
                    }
                  : current,
              )
            }}
          >
            {activeConversation.isMuted ? translate('tourGroups.unmuteConversation') : translate('tourGroups.muteConversation')}
          </button>
          <button
            type="button"
            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
            onClick={async () => {
              const nextSummary = await onUpdateArchiveState(activeConversation.conversationId, !activeConversation.isArchived)
              onSetConversationList(current =>
                current
                  ? {
                      ...current,
                      conversations: current.conversations.map(conversation =>
                        conversation.conversationId === nextSummary.conversationId ? nextSummary : conversation,
                      ),
                    }
                  : current,
              )
            }}
          >
            {activeConversation.isArchived ? translate('tourGroups.unarchiveConversation') : translate('tourGroups.archiveConversation')}
          </button>
        </div>
      ) : null}
    </div>
  )
}
