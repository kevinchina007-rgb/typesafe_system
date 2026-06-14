// 本文件定义 CustomerFeedbackPage 页面的状态控制逻辑，负责条件维护、请求触发和动作调度。

import { useEffect, useState } from 'react'

import { useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { OrderListResponse } from '@/microservices/order/objects/OrderListResponse'
import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'
import type { CustomerFeedbackPageController, CancellationOrderOption } from '../objects'
import { buildCancellationOrderSelectionLabel, buildCancellationOrderTitle, inferOrderCategory } from '../functions'

// 客户反馈页控制器，负责会话线程和可取消订单的组合状态。
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

  // 登录后同步客户反馈线程和可发起取消的订单。
  useEffect(() => {
    if (!signedInUser) return

    void loadUserThreads()
    void travelMvpApiClient.listOrders(signedInUser.userId).then((response: OrderListResponse) => {
      setCancellationOrders(
        response.orders.map(order => ({
          orderId: order.orderId,
          title: buildCancellationOrderTitle(order).trim() || order.orderId,
          detailLabel: buildCancellationOrderSelectionLabel(order).trim() || order.orderId,
          category: inferOrderCategory(order),
        })),
      )
    })
  }, [loadUserThreads, signedInUser])

  // 把当前激活线程放到最前面，便于快速查看。
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
