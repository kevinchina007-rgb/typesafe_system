import type { TourGroupUploadedAttachmentResponse } from '@/microservices/tour-group/objects/TourGroupUploadedAttachmentResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import { createChatQueryString, readFileAsBase64 } from './TourGroupPlannerSupportShared'

export const uploadConversationAttachment = (groupId: string, conversationId: string, attachmentFile: File): Promise<TourGroupUploadedAttachmentResponse> =>
  readFileAsBase64(attachmentFile).then(base64Content =>
    executeJsonApiRequest<TourGroupUploadedAttachmentResponse>(
      `/conversations/${conversationId}/attachments${createChatQueryString({ groupId })}`,
      'POST',
      {
        fileName: attachmentFile.name,
        mimeType: attachmentFile.type || 'application/octet-stream',
        base64Content,
      },
    ),
  )
