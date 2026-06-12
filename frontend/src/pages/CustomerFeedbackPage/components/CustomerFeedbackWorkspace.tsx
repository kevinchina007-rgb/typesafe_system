// 本文件定义 CustomerFeedbackPage 页面的工作区组件，负责组织主要操作区和信息区。

import { createFeedbackComplaint, createOrderCancellationMessage, markFeedbackThreadRead, sendFeedbackMessage } from '@/app/stores/feedback-chat-store'
import type { UserResponse } from '@/lib/mvp-types/index'
import { FeedbackConversationWorkspace } from '@/pages/shared/feedback/FeedbackConversationWorkspace'
import type { CancellationOrderOption } from '@/pages/CustomerFeedbackPage/objects'
import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'

type CustomerFeedbackWorkspaceProps = {
  signedInUser: UserResponse
  orderedThreads: FeedbackThread[]
  cancellationOrders: CancellationOrderOption[]
  translate: (translationKey: string) => string
}

export function CustomerFeedbackWorkspace({
  signedInUser,
  orderedThreads,
  cancellationOrders,
  translate,
}: CustomerFeedbackWorkspaceProps) {
  return (
    <FeedbackConversationWorkspace
      audience="User"
      audienceDisplayName={signedInUser.nickname}
      audienceAvatarUrl={signedInUser.avatarUrl}
      emptyTitle={translate('feedback.userTitle')}
      emptyDescription={translate('feedback.userEmpty')}
      fullScreen
      threads={orderedThreads}
      cancellationOrders={cancellationOrders}
      translate={translate}
      unreadCountSelector={thread => thread.unreadByUser}
      onMarkRead={markFeedbackThreadRead}
      onSendMessage={(threadId, body) =>
        sendFeedbackMessage({
          threadId,
          senderRole: 'User',
          senderDisplayName: signedInUser.nickname,
          body,
        })
      }
      onCreateCancellationRequest={(threadId, orderId, reason) =>
        createOrderCancellationMessage({
          threadId,
          orderId,
          reason,
        })
      }
      onCreateComplaint={createFeedbackComplaint}
    />
  )
}
