import { useEffect, useMemo, useState } from 'react'

import type { AppLanguage, TourGroupChatSettingsResponse, TourGroupConversationListResponse, TourGroupConversationSummaryResponse, TourGroupMembershipResponse, TourGroupMessageResponse, TourGroupMessageSearchResultResponse, TourGroupUploadedAttachmentResponse, UserResponse } from '@/lib/mvp-types/index'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { toBackendAssetUrl } from '@/lib/presenters/view-models'

type TourGroupChatPanelProps = {
  currentLanguage: AppLanguage
  groupId: string
  organizerUserId: string
  memberships: TourGroupMembershipResponse[]
  signedInUser: UserResponse | null
  isBusy: boolean
  translate: (translationKey: string) => string
  onLoadChatSettings: (groupId: string) => Promise<TourGroupChatSettingsResponse>
  onUpdateChatSettings: (groupId: string, payload: { allowMemberDirectChat: boolean }) => Promise<TourGroupChatSettingsResponse>
  onLoadConversations: (groupId: string) => Promise<TourGroupConversationListResponse>
  onSearchConversations: (groupId: string, query: string) => Promise<TourGroupConversationSummaryResponse[]>
  onSearchMessages: (groupId: string, query: string) => Promise<TourGroupMessageSearchResultResponse[]>
  onGetOrCreateDirectConversation: (groupId: string, payload: { targetUserId: string }) => Promise<TourGroupConversationSummaryResponse>
  onLoadMessages: (conversationId: string) => Promise<TourGroupMessageResponse[]>
  onSendMessage: (
    conversationId: string,
    payload: {
      messageType?: string
      content: string
      replyToMessageId?: string | null
      attachments?: TourGroupUploadedAttachmentResponse[]
    },
  ) => Promise<TourGroupMessageResponse[]>
  onUploadAttachment: (groupId: string, conversationId: string, attachmentFile: File) => Promise<TourGroupUploadedAttachmentResponse>
  onMarkConversationRead: (conversationId: string) => Promise<TourGroupConversationSummaryResponse>
  onEditMessage: (messageId: string, payload: { content: string }) => Promise<TourGroupMessageResponse[]>
  onDeleteMessage: (messageId: string) => Promise<TourGroupMessageResponse[]>
  onRecallMessage: (messageId: string) => Promise<TourGroupMessageResponse[]>
  onAddReaction: (messageId: string, reactionType: string) => Promise<TourGroupMessageResponse[]>
  onRemoveReaction: (messageId: string, reactionType: string) => Promise<TourGroupMessageResponse[]>
  onUpdateMuteState: (conversationId: string, muted: boolean) => Promise<TourGroupConversationSummaryResponse>
  onUpdateArchiveState: (conversationId: string, archived: boolean) => Promise<TourGroupConversationSummaryResponse>
}

const quickReactions = ['👍', '❤️', '👀', '✅']

export function TourGroupChatPanel(props: TourGroupChatPanelProps) {
  const {
    currentLanguage,
    groupId,
    organizerUserId,
    memberships,
    signedInUser,
    isBusy,
    translate,
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
  } = props

  const [chatSettings, setChatSettings] = useState<TourGroupChatSettingsResponse | null>(null)
  const [conversationList, setConversationList] = useState<TourGroupConversationListResponse | null>(null)
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null)
  const [messages, setMessages] = useState<TourGroupMessageResponse[]>([])
  const [draft, setDraft] = useState('')
  const [replyTarget, setReplyTarget] = useState<TourGroupMessageResponse | null>(null)
  const [editingMessageId, setEditingMessageId] = useState<string | null>(null)
  const [editingDraft, setEditingDraft] = useState('')
  const [attachments, setAttachments] = useState<TourGroupUploadedAttachmentResponse[]>([])
  const [directTargetUserId, setDirectTargetUserId] = useState('')
  const [conversationQuery, setConversationQuery] = useState('')
  const [messageQuery, setMessageQuery] = useState('')
  const [conversationSearchResults, setConversationSearchResults] = useState<TourGroupConversationSummaryResponse[]>([])
  const [messageSearchResults, setMessageSearchResults] = useState<TourGroupMessageSearchResultResponse[]>([])
  const [targetMessageId, setTargetMessageId] = useState<string | null>(null)

  const isOrganizer = signedInUser?.userId === organizerUserId
  const isMember = memberships.some(membership => membership.userId === signedInUser?.userId && membership.status === 'Active')
  const activeConversation =
    conversationList?.conversations.find(conversation => conversation.conversationId === activeConversationId) ?? null

  const memberDisplayNameMap = useMemo(() => {
    const entries = memberships.map(membership => [membership.userId, membership.userDisplayName ?? membership.userId] as const)
    if (signedInUser) {
      entries.push([signedInUser.userId, signedInUser.nickname] as const)
    }
    return new Map(entries)
  }, [memberships, signedInUser])

  function getUserDisplayName(userId: string) {
    if (userId === organizerUserId) {
      return memberDisplayNameMap.get(userId) ?? translate('tourGroups.organizer')
    }
    return memberDisplayNameMap.get(userId) ?? userId
  }

  function localizeConversationTitle(conversationTitle: string, conversationType?: string) {
    if (conversationType === 'GroupPublic' || conversationTitle === 'Group chat') {
      return translate('tourGroups.groupChat')
    }
    if (conversationTitle === 'Direct chat') {
      return translate('tourGroups.directMessages')
    }
    return conversationTitle
  }

  const activeConversationParticipantsSummary =
    activeConversation?.conversationType === 'GroupPublic'
      ? memberships
          .filter(membership => membership.status === 'Active')
          .map(membership => getUserDisplayName(membership.userId))
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

  useEffect(() => {
    if (!signedInUser || !isMember) return
    void reload()
  }, [groupId, signedInUser?.userId, isMember])

  useEffect(() => {
    if (!activeConversationId) {
      setMessages([])
      return
    }
    void openConversation(activeConversationId)
  }, [activeConversationId])

  useEffect(() => {
    if (!targetMessageId || messages.every(message => message.messageId !== targetMessageId)) {
      return
    }

    const element = document.getElementById(`tour-group-message-${targetMessageId}`)
    if (!element) {
      return
    }

    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    element.classList.add('chat-message-card-highlight')
    const timeoutId = window.setTimeout(() => {
      element.classList.remove('chat-message-card-highlight')
      setTargetMessageId(current => (current == targetMessageId ? null : current))
    }, 2200)

    return () => window.clearTimeout(timeoutId)
  }, [messages, targetMessageId])

  async function reload() {
    const [settings, conversations] = await Promise.all([
      onLoadChatSettings(groupId),
      onLoadConversations(groupId),
    ])
    setChatSettings(settings)
    setConversationList(conversations)
    setActiveConversationId(current => current ?? conversations.groupChatConversationId ?? conversations.conversations[0]?.conversationId ?? null)
    setDirectTargetUserId(current => current || selectableDirectTargets[0]?.userId || '')
  }

  async function openConversation(conversationId: string) {
    const [nextMessages, updatedSummary] = await Promise.all([
      onLoadMessages(conversationId),
      onMarkConversationRead(conversationId),
    ])
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

  return (
    <section className="list-surface">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('tourGroups.chatEyebrow')}</p>
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
                setChatSettings(nextSettings)
                await reload()
              }}
            />
            <span>{translate('tourGroups.allowMemberDirectChat')}</span>
          </label>
        ) : null}
      </div>

      <div className="tour-group-chat-shell">
        <aside className="tour-group-direct-sidebar">
          <div className="stack-form">
            <label>{translate('tourGroups.searchConversations')}</label>
            <div className="inline-form">
              <input value={conversationQuery} onChange={event => setConversationQuery(event.target.value)} />
              <button
                type="button"
                disabled={isBusy || !conversationQuery.trim()}
                onClick={async () => setConversationSearchResults(await onSearchConversations(groupId, conversationQuery))}
              >
                {translate('blog.confirmSearch')}
              </button>
            </div>
            <label>{translate('tourGroups.searchMessages')}</label>
            <div className="inline-form">
              <input value={messageQuery} onChange={event => setMessageQuery(event.target.value)} />
              <button
                type="button"
                disabled={isBusy || !messageQuery.trim()}
                onClick={async () => setMessageSearchResults(await onSearchMessages(groupId, messageQuery))}
              >
                {translate('blog.confirmSearch')}
              </button>
            </div>
          </div>

          <div className="stack-form">
            <label>{translate('tourGroups.startChatWith')}</label>
            <select value={directTargetUserId} onChange={event => setDirectTargetUserId(event.target.value)}>
              <option value="">{translate('tourGroups.selectMemberToChat')}</option>
              {selectableDirectTargets.map(target => (
                <option key={target.userId} value={target.userId}>
                  {target.userId === organizerUserId ? translate('tourGroups.messageOrganizer') : getUserDisplayName(target.userId)}
                </option>
              ))}
            </select>
            <button
              type="button"
              disabled={isBusy || !directTargetUserId}
              onClick={async () => {
                if (!directTargetUserId) return
                const conversation = await onGetOrCreateDirectConversation(groupId, { targetUserId: directTargetUserId })
                setConversationList(current =>
                  current
                    ? {
                        ...current,
                        conversations: [conversation, ...current.conversations.filter(item => item.conversationId !== conversation.conversationId)],
                      }
                    : { conversations: [conversation], groupChatConversationId: null },
                )
                setActiveConversationId(conversation.conversationId)
              }}
            >
              {translate('tourGroups.openConversation')}
            </button>
          </div>

          {conversationSearchResults.length > 0 ? (
            <div className="tour-group-direct-list">
              {conversationSearchResults.map(conversation => (
                <button key={conversation.conversationId} type="button" onClick={() => setActiveConversationId(conversation.conversationId)}>
                  <strong>{localizeConversationTitle(conversation.conversationTitle, conversation.conversationType)}</strong>
                  <span>{conversation.lastMessagePreview ?? translate('tourGroups.noMessagesYet')}</span>
                </button>
              ))}
            </div>
          ) : null}

          <div className="tour-group-direct-list">
            {conversationList?.conversations.map(conversation => (
              <button
                key={conversation.conversationId}
                type="button"
                className={conversation.conversationId === activeConversationId ? 'secondary-button' : undefined}
                onClick={() => setActiveConversationId(conversation.conversationId)}
              >
                <strong>{localizeConversationTitle(conversation.conversationTitle, conversation.conversationType)}</strong>
                <span>{conversation.lastMessagePreview ?? translate('tourGroups.noMessagesYet')}</span>
                {conversation.unreadCount > 0 ? <em>{conversation.unreadCount}</em> : null}
              </button>
            )) ?? null}
          </div>
        </aside>

        <div className="tour-group-chat-thread">
          {activeConversation ? (
            <>
              <div className="panel-heading">
                <div>
                  <h4>{localizeConversationTitle(activeConversation.conversationTitle, activeConversation.conversationType)}</h4>
                  <p>{activeConversationParticipantsSummary}</p>
                </div>
                {activeConversation.conversationType === 'Direct' ? (
                  <div className="manager-task-actions">
                    <button type="button" className="secondary-button" onClick={async () => {
                      const nextSummary = await onUpdateMuteState(activeConversation.conversationId, !activeConversation.isMuted)
                      setConversationList(current => current ? ({
                        ...current,
                        conversations: current.conversations.map(conversation => conversation.conversationId === nextSummary.conversationId ? nextSummary : conversation),
                      }) : current)
                    }}>
                      {activeConversation.isMuted ? translate('tourGroups.unmuteConversation') : translate('tourGroups.muteConversation')}
                    </button>
                    <button type="button" className="secondary-button" onClick={async () => {
                      const nextSummary = await onUpdateArchiveState(activeConversation.conversationId, !activeConversation.isArchived)
                      setConversationList(current => current ? ({
                        ...current,
                        conversations: current.conversations.map(conversation => conversation.conversationId === nextSummary.conversationId ? nextSummary : conversation),
                      }) : current)
                    }}>
                      {activeConversation.isArchived ? translate('tourGroups.unarchiveConversation') : translate('tourGroups.archiveConversation')}
                    </button>
                  </div>
                ) : null}
              </div>

              {messageSearchResults.length > 0 ? (
                <div className="list-surface">
              {messageSearchResults.map(result => (
                    <button
                      key={result.message.messageId}
                      type="button"
                      className={result.message.messageId === targetMessageId ? 'secondary-button chat-search-result-active' : 'secondary-button'}
                      onClick={() => {
                        setTargetMessageId(result.message.messageId)
                        if (activeConversationId !== result.conversationId) {
                          setActiveConversationId(result.conversationId)
                        }
                      }}
                    >
                      {localizeConversationTitle(result.conversationTitle)}: {result.message.content}
                    </button>
                  ))}
                </div>
              ) : null}

              <div className="tour-group-chat-messages">
                {messages.map(message => (
                  <article
                    key={message.messageId}
                    id={`tour-group-message-${message.messageId}`}
                    className={message.isMine ? 'chat-message-card mine' : 'chat-message-card'}
                  >
                    <div className="chat-message-meta">
                      <strong>{message.senderDisplayName}</strong>
                      <span>{new Date(message.createdAt).toLocaleString(currentLanguage === 'zh' ? 'zh-CN' : 'en-US')}</span>
                    </div>
                    {message.replyToPreview ? <p className="detail-label">{translate('tourGroups.replyingTo')}: {message.replyToPreview}</p> : null}
                    {message.content ? <p className="chat-message-content">{message.content}</p> : null}
                    {message.attachments.length > 0 ? (
                      <div className="content-image-gallery">
                        {message.attachments.map(attachment => (
                          attachment.attachmentType === 'Image' ? (
                            <figure key={attachment.attachmentId} className="content-image-card">
                              <BackendAssetImage assetUrl={attachment.publicUrl} alt={attachment.originalFileName} className="content-image" />
                            </figure>
                          ) : (
                            <a key={attachment.attachmentId} href={toBackendAssetUrl(attachment.publicUrl)} target="_blank" rel="noreferrer" className="secondary-button">
                              {attachment.originalFileName}
                            </a>
                          )
                        ))}
                      </div>
                    ) : null}
                    <div className="chat-message-actions">
                      <button type="button" className="secondary-button" onClick={() => setReplyTarget(message)}>{translate('tourGroups.replyMessage')}</button>
                      {message.canEdit ? <button type="button" className="secondary-button" onClick={() => {
                        setEditingMessageId(message.messageId)
                        setEditingDraft(message.content)
                      }}>{translate('tourGroups.editMessage')}</button> : null}
                      {message.canDelete ? <button type="button" className="secondary-button" onClick={async () => setMessages(await onDeleteMessage(message.messageId))}>{translate('tourGroups.deleteMessage')}</button> : null}
                      {message.canRecall ? <button type="button" className="secondary-button" onClick={async () => setMessages(await onRecallMessage(message.messageId))}>{translate('tourGroups.recallMessage')}</button> : null}
                    </div>
                    {message.canReact ? (
                      <div className="chat-message-reactions">
                        {quickReactions.map(reactionType => {
                          const existingReaction = message.reactions.find(reaction => reaction.reactionType === reactionType)
                          return (
                            <button
                              key={reactionType}
                              type="button"
                              className="secondary-button"
                              onClick={async () =>
                                setMessages(
                                  existingReaction?.reactedByCurrentUser
                                    ? await onRemoveReaction(message.messageId, reactionType)
                                    : await onAddReaction(message.messageId, reactionType),
                                )
                              }
                            >
                              {reactionType} {existingReaction?.count ?? 0}
                            </button>
                          )
                        })}
                      </div>
                    ) : null}
                  </article>
                ))}
              </div>

              {editingMessageId ? (
                <div className="stack-form">
                  <textarea value={editingDraft} onChange={event => setEditingDraft(event.target.value)} />
                  <div className="manager-task-actions">
                    <button type="button" onClick={async () => {
                      setMessages(await onEditMessage(editingMessageId, { content: editingDraft }))
                      setEditingMessageId(null)
                      setEditingDraft('')
                    }}>{translate('tourGroups.saveEditedMessage')}</button>
                    <button type="button" className="secondary-button" onClick={() => {
                      setEditingMessageId(null)
                      setEditingDraft('')
                    }}>{translate('tourGroups.cancelEditMessage')}</button>
                  </div>
                </div>
              ) : (
                <div className="stack-form">
                  {replyTarget ? (
                    <div className="list-surface">
                      <p className="detail-label">{translate('tourGroups.replyingTo')}: {replyTarget.content}</p>
                      <button type="button" className="secondary-button" onClick={() => setReplyTarget(null)}>
                        {translate('tourGroups.cancelReply')}
                      </button>
                    </div>
                  ) : null}
                  {attachments.length > 0 ? (
                    <div className="content-image-gallery">
                      {attachments.map(attachment => (
                        <figure key={attachment.attachmentId} className="content-image-card">
                          {attachment.attachmentType === 'Image' ? (
                            <BackendAssetImage assetUrl={attachment.publicUrl} alt={attachment.originalFileName} className="content-image" />
                          ) : (
                            <figcaption>{attachment.originalFileName}</figcaption>
                          )}
                        </figure>
                      ))}
                    </div>
                  ) : null}
                  <textarea value={draft} onChange={event => setDraft(event.target.value)} placeholder={translate('tourGroups.chatInputPlaceholder')} />
                  <div className="manager-task-actions">
                    <label className="secondary-button file-upload-button">
                      <input
                        type="file"
                        multiple
                        disabled={isBusy || !activeConversationId}
                        onChange={async event => {
                          await handleUploadAttachment(event.target.files)
                          event.currentTarget.value = ''
                        }}
                      />
                      {translate('tourGroups.attachFile')}
                    </label>
                    <button type="button" disabled={isBusy || (!draft.trim() && attachments.length === 0) || !activeConversation?.canSendMessage} onClick={handleSendMessage}>
                      {translate('tourGroups.sendMessage')}
                    </button>
                  </div>
                </div>
              )}
            </>
          ) : (
            <p className="empty-state">{translate('tourGroups.chooseConversationHint')}</p>
          )}
        </div>
      </div>
    </section>
  )
}
