import { useEffect, useMemo, useState } from 'react'

import type {
  TourGroupChatSettingsResponse,
  TourGroupConversationListResponse,
  TourGroupConversationSummaryResponse,
  TourGroupMessageResponse,
  TourGroupMessageSearchResultResponse,
  TourGroupUploadedAttachmentResponse,
} from '@/lib/mvp-types/index'

import { ChatComposer } from '@/pages/TourGroupsPage/components/ChatComposer'
import { ChatHeader } from '@/pages/TourGroupsPage/components/ChatHeader'
import { ChatMemberList } from '@/pages/TourGroupsPage/components/ChatMemberList'
import { ChatMessageList } from '@/pages/TourGroupsPage/components/ChatMessageList'
import type { ChatMessageItem, TourGroupChatPanelProps } from '@/pages/TourGroupsPage/components/TourGroupChatPanel.types'
import { getMemberDisplayNameMap, getUserDisplayName, getDirectConversationIdentityLabel, localizeConversationTitle } from '@/pages/TourGroupsPage/components/TourGroupChatPanel.utils'

function normalizeMessage(message: TourGroupMessageResponse): ChatMessageItem {
  return message as unknown as ChatMessageItem
}

export function TourGroupChatPanel(props: TourGroupChatPanelProps) {
  const {
    groupId,
    organizerUserId,
    memberships,
    signedInUser,
    isBusy,
    translate,
    onLoadChatSettings,
    onUpdateChatSettings,
    onLoadConversations,
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
  } = props

  const [chatSettings, setChatSettings] = useState<TourGroupChatSettingsResponse | null>(null)
  const [conversationList, setConversationList] = useState<TourGroupConversationListResponse | null>(null)
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null)
  const [messages, setMessages] = useState<TourGroupMessageResponse[]>([])
  const [draft, setDraft] = useState('')
  const [replyTarget, setReplyTarget] = useState<ChatMessageItem | null>(null)
  const [editingMessageId, setEditingMessageId] = useState<string | null>(null)
  const [editingDraft, setEditingDraft] = useState('')
  const [attachments, setAttachments] = useState<TourGroupUploadedAttachmentResponse[]>([])
  const [directTargetUserId, setDirectTargetUserId] = useState('')
  const [messageQuery, setMessageQuery] = useState('')
  const [messageSearchResults, setMessageSearchResults] = useState<TourGroupMessageSearchResultResponse[]>([])
  const [targetMessageId, setTargetMessageId] = useState<string | null>(null)

  const isOrganizer = signedInUser?.userId === organizerUserId
  const isMember = memberships.some(membership => membership.userId === signedInUser?.userId && membership.status === 'Active')
  const groupChatConversation =
    conversationList?.conversations.find(conversation => conversation.conversationType === 'GroupPublic') ?? null
  const effectiveConversationId = activeConversationId ?? groupChatConversation?.conversationId ?? null
  const activeConversation =
    conversationList?.conversations.find(conversation => conversation.conversationId === effectiveConversationId) ?? groupChatConversation ?? null
  const hasConversationSelected = activeConversation !== null

  const memberDisplayNameMap = useMemo(
    () => getMemberDisplayNameMap(memberships, signedInUser?.userId, signedInUser?.nickname),
    [memberships, signedInUser?.nickname, signedInUser?.userId],
  )

  const activeConversationParticipantsSummary =
    activeConversation?.conversationType === 'GroupPublic'
      ? memberships
          .filter(membership => membership.status === 'Active')
          .map(membership => getUserDisplayName(membership.userId, organizerUserId, memberDisplayNameMap, translate))
          .join(', ')
      : activeConversation?.participantsSummary ?? ''

  const selectableDirectTargets = useMemo(() => {
    if (!signedInUser) return []
    return memberships.filter(membership => {
      if (membership.status !== 'Active') return false
      if (membership.userId === signedInUser.userId) return false
      if (signedInUser.userId === organizerUserId) return true
      if (membership.userId === organizerUserId) return true
      return chatSettings?.allowMemberDirectChat ?? false
    })
  }, [chatSettings?.allowMemberDirectChat, memberships, organizerUserId, signedInUser])

  function getDefaultDirectTargetUserId() {
    const currentUserId = signedInUser?.userId
    if (!currentUserId) return ''
    if (currentUserId !== organizerUserId && selectableDirectTargets.some(target => target.userId === organizerUserId)) {
      return organizerUserId
    }
    return selectableDirectTargets.find(target => target.userId !== currentUserId)?.userId ?? ''
  }

  useEffect(() => {
    setChatSettings(null)
    setConversationList(null)
    setActiveConversationId(null)
    setMessages([])
    setDraft('')
    setReplyTarget(null)
    setEditingMessageId(null)
    setEditingDraft('')
    setAttachments([])
    setDirectTargetUserId('')
    setMessageQuery('')
    setMessageSearchResults([])
    setTargetMessageId(null)
  }, [groupId])

  async function reload() {
    const [settings, conversations] = await Promise.all([onLoadChatSettings(groupId), onLoadConversations(groupId)])
    setChatSettings(settings)
    setConversationList(conversations)
    setActiveConversationId(current => current ?? conversations.groupChatConversationId ?? conversations.conversations[0]?.conversationId ?? null)
    setDirectTargetUserId(current => current || getDefaultDirectTargetUserId())
  }

  async function openConversation(conversationId: string) {
    const [nextMessages, updatedSummary] = await Promise.all([onLoadMessages(conversationId), onMarkConversationRead(conversationId)])
    setMessages(nextMessages)
    setConversationList(current =>
      current
        ? {
            ...current,
            conversations: current.conversations.map(conversation =>
              conversation.conversationId === updatedSummary.conversationId ? updatedSummary : conversation,
            ),
          }
        : current,
    )
  }

  useEffect(() => {
    if (!signedInUser || !isMember) return
    void reload()
  }, [groupId, signedInUser?.userId, isMember])

  useEffect(() => {
    if (activeConversationId || !conversationList?.groupChatConversationId) {
      return
    }
    setActiveConversationId(conversationList.groupChatConversationId)
  }, [activeConversationId, conversationList?.groupChatConversationId])

  useEffect(() => {
    if (!activeConversationId) {
      setMessages([])
      return
    }
    void openConversation(activeConversationId)
  }, [activeConversationId])

  async function handleSendMessage() {
    if (!activeConversationId) return
    const nextMessages = await onSendMessage(activeConversationId, {
      messageType: attachments.length > 0 && draft.trim() ? 'Mixed' : attachments.length > 0 ? attachments[0]?.attachmentType : 'Text',
      content: draft,
      replyToMessageId: replyTarget?.messageId ?? null,
      attachments,
    })
    setMessages(nextMessages)
    setDraft('')
    setReplyTarget(null)
    setAttachments([])
  }

  async function handleUploadAttachment(fileList: FileList | null) {
    if (!fileList || !activeConversationId) return
    const uploaded = await Promise.all(Array.from(fileList).slice(0, 4).map(file => onUploadAttachment(groupId, activeConversationId, file)))
    setAttachments(current => [...current, ...uploaded].map((item, index) => ({ ...item, sortOrder: index })))
  }

  if (!signedInUser || !isMember) {
    return null
  }

  const activeConversationAsChat = activeConversation as TourGroupConversationSummaryResponse | null
  const canSendMessage = activeConversationAsChat?.canSendMessage ?? false

  return (
    <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
      <ChatHeader
        translate={translate}
        isBusy={isBusy}
        isOrganizer={isOrganizer}
        groupId={groupId}
        chatSettings={chatSettings}
        activeConversation={activeConversationAsChat}
        onUpdateChatSettings={onUpdateChatSettings}
        onUpdateMuteState={onUpdateMuteState}
        onUpdateArchiveState={onUpdateArchiveState}
        onReload={reload}
        onSetConversationList={setConversationList}
      />

      <div className="grid gap-4 xl:grid-cols-[18rem_1fr]">
        <ChatMemberList
          translate={translate}
          groupId={groupId}
          organizerUserId={organizerUserId}
          memberships={memberships}
          signedInUserId={signedInUser?.userId}
          signedInUserNickname={signedInUser?.nickname}
          isBusy={isBusy}
          chatSettings={chatSettings}
          groupChatConversation={groupChatConversation}
          effectiveConversationId={effectiveConversationId}
          targetMessageId={targetMessageId}
          directTargetUserId={directTargetUserId}
          setDirectTargetUserId={setDirectTargetUserId}
          messageQuery={messageQuery}
          setMessageQuery={setMessageQuery}
          messageSearchResults={messageSearchResults}
          onSetTargetMessageId={setTargetMessageId}
          onSearchMessages={onSearchMessages}
          onGetOrCreateDirectConversation={onGetOrCreateDirectConversation}
          onSetMessageSearchResults={setMessageSearchResults}
          onSetConversationList={setConversationList}
          onSetActiveConversationId={setActiveConversationId}
        />

        <div className="grid gap-4">
          {hasConversationSelected ? (
            <>
              <div className="text-lg font-bold text-slate-950">
                <div>
                  <h4>{activeConversation ? localizeConversationTitle(translate, activeConversation.conversationTitle, activeConversation.conversationType) : translate('tourGroups.chatTitle')}</h4>
                  <p>
                    {activeConversation?.conversationType === 'Direct'
                      ? getDirectConversationIdentityLabel(translate, organizerUserId, activeConversation)
                      : activeConversationParticipantsSummary}
                  </p>
                </div>
              </div>

              <ChatMessageList
                translate={translate}
                messages={messages.map(normalizeMessage)}
                targetMessageId={targetMessageId}
                onTargetMessageHandled={setTargetMessageId}
                onReplyTarget={setReplyTarget}
                onEditStart={message => {
                  setEditingMessageId(message.messageId)
                  setEditingDraft(message.content)
                }}
                onDeleteMessage={async messageId => {
                  setMessages(await onDeleteMessage(messageId))
                }}
                onRecallMessage={async messageId => {
                  setMessages(await onRecallMessage(messageId))
                }}
                onAddReaction={async (messageId, reactionType) => {
                  setMessages(await onAddReaction(messageId, reactionType))
                }}
                onRemoveReaction={async (messageId, reactionType) => {
                  setMessages(await onRemoveReaction(messageId, reactionType))
                }}
              />

              <ChatComposer
                translate={translate}
                isBusy={isBusy}
                activeConversationId={activeConversationId}
                activeConversationCanSendMessage={canSendMessage}
                draft={draft}
                setDraft={setDraft}
                replyTarget={replyTarget}
                setReplyTarget={setReplyTarget}
                attachments={attachments}
                editingMessageId={editingMessageId}
                editingDraft={editingDraft}
                setEditingDraft={setEditingDraft}
                onSendMessage={handleSendMessage}
                onUploadAttachment={handleUploadAttachment}
                onEditMessage={async () => {
                  if (!editingMessageId) return
                  setMessages(await onEditMessage(editingMessageId, { content: editingDraft }))
                  setEditingMessageId(null)
                  setEditingDraft('')
                }}
                onCancelEditMessage={() => {
                  setEditingMessageId(null)
                  setEditingDraft('')
                }}
              />
            </>
          ) : (
            <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
              <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.chooseConversationHint')}</p>
              {groupChatConversation ? (
                <button
                  type="button"
                  className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                  onClick={() => setActiveConversationId(groupChatConversation.conversationId)}
                >
                  {translate('tourGroups.groupChat')}
                </button>
              ) : null}
            </div>
          )}
        </div>
      </div>
    </section>
  )
}
