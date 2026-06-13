import type { ChatAttachmentPreviewItem, ChatMessageItem } from '@/pages/TourGroupsPage/components/TourGroupChatPanel.types'
import { ChatAttachmentPreview } from '@/pages/TourGroupsPage/components/ChatAttachmentPreview'

type ChatComposerProps = {
  translate: (translationKey: string) => string
  isBusy: boolean
  activeConversationId: string | null
  activeConversationCanSendMessage?: boolean
  draft: string
  setDraft: (value: string) => void
  replyTarget: ChatMessageItem | null
  setReplyTarget: (value: ChatMessageItem | null) => void
  attachments: ChatAttachmentPreviewItem[]
  editingMessageId: string | null
  editingDraft: string
  setEditingDraft: (value: string) => void
  onSendMessage: () => Promise<void>
  onUploadAttachment: (fileList: FileList | null) => Promise<void>
  onEditMessage: () => Promise<void>
  onCancelEditMessage: () => void
}

export function ChatComposer({
  translate,
  isBusy,
  activeConversationId,
  activeConversationCanSendMessage,
  draft,
  setDraft,
  replyTarget,
  setReplyTarget,
  attachments,
  editingMessageId,
  editingDraft,
  setEditingDraft,
  onSendMessage,
  onUploadAttachment,
  onEditMessage,
  onCancelEditMessage,
}: ChatComposerProps) {
  if (editingMessageId) {
    return (
      <div className="grid gap-4">
        <textarea value={editingDraft} onChange={event => setEditingDraft(event.target.value)} />
        <div className="flex flex-wrap items-center gap-3">
          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" onClick={onEditMessage}>
            {translate('tourGroups.saveEditedMessage')}
          </button>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={onCancelEditMessage}>
            {translate('tourGroups.cancelEditMessage')}
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="grid gap-4">
      {replyTarget ? (
        <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
          <p className="text-sm font-medium text-slate-500">
            {translate('tourGroups.replyingTo')}: {replyTarget.content}
          </p>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => setReplyTarget(null)}>
            {translate('tourGroups.cancelReply')}
          </button>
        </div>
      ) : null}

      {attachments.length > 0 ? <ChatAttachmentPreview attachments={attachments} /> : null}

      <textarea value={draft} onChange={event => setDraft(event.target.value)} />
      <div className="flex flex-wrap items-center gap-3">
        <label className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55">
          <input
            type="file"
            multiple
            disabled={isBusy || !activeConversationId}
            onChange={async event => {
              await onUploadAttachment(event.target.files)
              event.currentTarget.value = ''
            }}
          />
          {translate('tourGroups.attachFile')}
        </label>
        <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy || (!draft.trim() && attachments.length === 0) || !activeConversationCanSendMessage} onClick={onSendMessage}>
          {translate('tourGroups.sendMessage')}
        </button>
      </div>
    </div>
  )
}
