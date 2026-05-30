import { useEffect, useState } from 'react'

import { useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'
import type { CustomerFeedbackPageController, CancellationOrderOption } from '../objects'
import { buildCancellationOrderTitle, inferOrderCategory } from '../functions'

export function useCustomerFeedbackPageController({
  signedInUser,
}: {
  signedInUser: { userId: string } | null
}): CustomerFeedbackPageController & {
  activeThread: FeedbackThread | null
  threads: FeedbackThread[]
} {
  const threads = useFeedbackChatStore(state => state.userThreads)
  const activeThread = useFeedbackChatStore(state => state.activeMiniThread)
  const loadUserThreads = useFeedbackChatStore(state => state.loadUserThreads)
  const [cancellationOrders, setCancellationOrders] = useState<CancellationOrderOption[]>([])

  useEffect(() => {
    if (!signedInUser) return

    void loadUserThreads()
    void travelMvpApiClient.listOrders(signedInUser.userId).then(response => {
      setCancellationOrders(response.orders.map(order => ({
        orderId: order.orderId,
        title: buildCancellationOrderTitle(order).trim() || order.orderId,
        category: inferOrderCategory(order),
      })))
    })
  }, [loadUserThreads, signedInUser])

  const orderedThreads = activeThread
    ? [activeThread, ...threads.filter(thread => thread.threadId !== activeThread.threadId)]
    : threads

  return {
    threads,
    activeThread,
    orderedThreads,
    cancellationOrders,
  }
}
