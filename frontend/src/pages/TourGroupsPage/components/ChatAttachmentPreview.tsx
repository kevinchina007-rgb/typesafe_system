import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { toBackendAssetUrl } from '@/lib/presenters/view-models'

import type { ChatAttachmentPreviewItem } from '@/pages/TourGroupsPage/components/TourGroupChatPanel.types'

export function ChatAttachmentPreview({ attachments }: { attachments: ChatAttachmentPreviewItem[] }) {
  if (attachments.length === 0) {
    return null
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {attachments.map(attachment =>
        attachment.attachmentType === 'Image' ? (
          <figure key={attachment.attachmentId} className="grid gap-2 border border-slate-200 bg-white p-2">
            <BackendAssetImage assetUrl={attachment.publicUrl} alt={attachment.originalFileName} className="aspect-video w-full object-cover" />
          </figure>
        ) : (
          <a
            key={attachment.attachmentId}
            href={toBackendAssetUrl(attachment.publicUrl)}
            target="_blank"
            rel="noreferrer"
            className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
          >
            {attachment.originalFileName}
          </a>
        ),
      )}
    </div>
  )
}
