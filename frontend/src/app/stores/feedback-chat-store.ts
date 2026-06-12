// 本文件封装状态管理逻辑。

import { create } from 'zustand'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { getManagerSnap } from '@/app/stores/manager-store'
import { getUserSnap } from '@/app/stores/user-store'
import type { FeedbackAudience } from '@/microservices/content/objects/FeedbackAudience'
import type { FeedbackManagerType } from '@/microservices/content/objects/FeedbackManagerType'
import type { FeedbackSenderRole } from '@/microservices/content/objects/FeedbackSenderRole'
import type { FeedbackSiteAdminChannel } from '@/microservices/content/objects/FeedbackSiteAdminChannel'
import type { FeedbackThreadResponse } from '@/microservices/content/objects/FeedbackThreadResponse'
import type { OrderCancellationRequestStatus } from '@/microservices/content/objects/OrderCancellationRequestPayload'

export type FeedbackThread = FeedbackThreadResponse
export type FeedbackMessage = FeedbackThread['messages'][number]

export type FeedbackAudienceView = 'user' | 'manager' | 'siteAdmin'

type FeedbackChatState = {
  userThreads: FeedbackThread[]
  managerThreads: FeedbackThread[]
  siteAdminUserThreads: FeedbackThread[]
  siteAdminManagerThreads: FeedbackThread[]
  activeMiniThread: FeedbackThread | null
  isLoading: boolean
}

type FeedbackChatActions = {
  loadUserThreads: () => Promise<FeedbackThread[]>
  loadManagerThreads: () => Promise<FeedbackThread[]>
  loadSiteAdminThreads: (channel: FeedbackSiteAdminChannel) => Promise<FeedbackThread[]>
  ensureOrderCancellationThread: (params: { userId: string; orderId: string }) => Promise<FeedbackThread>
  sendMessage: (params: {
    threadId: string
    senderRole: Exclude<FeedbackSenderRole, 'System'>
    senderDisplayName: string
    body: string
  }) => Promise<FeedbackThread | null>
  createOrderCancellationMessage: (params: {
    threadId: string
    orderId: string
    reason: string
  }) => Promise<FeedbackThread>
  handleOrderCancellationRequest: (params: {
    threadId: string
    messageId: string
    status: Exclude<OrderCancellationRequestStatus, 'pending'>
    managerNote?: string | null
    handledBy?: string | null
    handlerRole?: FeedbackSenderRole | null
  }) => Promise<FeedbackThread>
  markThreadRead: (threadId: string, audience: FeedbackAudience) => Promise<FeedbackThread | null>
  escalateThread: (params: { threadId: string; senderDisplayName: string; body: string }) => Promise<FeedbackThread>
  createComplaint: (params: { sourceThreadId: string; selectedMessageIds: string[]; userExplanation: string; userDisplayName: string }) => Promise<FeedbackThread>
  openComplaintManagerThread: (params: { complaintMessageId: string; siteAdminActorId: string }) => Promise<FeedbackThread>
  setActiveMiniThread: (thread: FeedbackThread | null) => void
  clearAllThreads: () => void
}

type FeedbackChatStore = FeedbackChatState & FeedbackChatActions

// 按更新时间倒序排列反馈线程。
function sortThreads(threads: FeedbackThread[]) {
  return [...threads].sort((left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt))
}

// 插入或更新单个线程。
function upsertThread(threads: FeedbackThread[], nextThread: FeedbackThread) {
  const existingIndex = threads.findIndex(thread => thread.threadId === nextThread.threadId)
  if (existingIndex < 0) {
    return sortThreads([nextThread, ...threads])
  }

  const nextThreads = [...threads]
  nextThreads[existingIndex] = nextThread
  return sortThreads(nextThreads)
}

// 把同一个线程同步到不同的状态桶里。
function syncThreadBuckets(state: FeedbackChatState, nextThread: FeedbackThread): FeedbackChatState {
  return {
    ...state,
    userThreads: nextThread.ownerUserId ? upsertThread(state.userThreads, nextThread) : state.userThreads,
    managerThreads:
      nextThread.kind === 'ServiceReview' || nextThread.kind === 'ManagerEscalation'
        ? upsertThread(state.managerThreads, nextThread)
        : state.managerThreads,
    siteAdminUserThreads: nextThread.kind === 'ServiceReview' ? upsertThread(state.siteAdminUserThreads, nextThread) : state.siteAdminUserThreads,
    siteAdminManagerThreads:
      nextThread.kind === 'ManagerEscalation' ? upsertThread(state.siteAdminManagerThreads, nextThread) : state.siteAdminManagerThreads,
    activeMiniThread: state.activeMiniThread?.threadId === nextThread.threadId ? nextThread : state.activeMiniThread,
  }
}

// 反馈聊天仓库，保存用户、管理者和站点管理员的反馈线程。
export const useFeedbackChatStore = create<FeedbackChatStore>()((set, get) => ({
  userThreads: [],
  managerThreads: [],
  siteAdminUserThreads: [],
  siteAdminManagerThreads: [],
  activeMiniThread: null,
  isLoading: false,
  loadUserThreads: async () => {
    set({ isLoading: true })
    try {
      const user = getUserSnap().signedInUser
      const response = await travelMvpApiClient.listMyFeedbackThreads(user?.userId)
      const threads = sortThreads(response.threads)
      set({ userThreads: threads, isLoading: false })
      return threads
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },
  loadManagerThreads: async () => {
    set({ isLoading: true })
    try {
      const manager = getManagerSnap().signedInManagerSession
      const response = await travelMvpApiClient.listManagerFeedbackThreads(manager?.managerType, manager?.scopeId, manager?.managerId)
      const threads = sortThreads(response.threads)
      set({ managerThreads: threads, isLoading: false })
      return threads
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },
  loadSiteAdminThreads: async channel => {
    set({ isLoading: true })
    try {
      const manager = getManagerSnap().signedInManagerSession
      const response = await travelMvpApiClient.listSiteAdminFeedbackThreads(channel, manager?.managerId)
      const threads = sortThreads(response.threads)
      if (channel === 'user') {
        set({ siteAdminUserThreads: threads, isLoading: false })
      } else {
        set({ siteAdminManagerThreads: threads, isLoading: false })
      }
      return threads
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },
  ensureOrderCancellationThread: async ({ userId, orderId }) => {
    const nextThread = await travelMvpApiClient.ensureOrderCancellationThread({ userId, orderId })
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  sendMessage: async ({ threadId, senderRole, senderDisplayName, body }) => {
    if (body.trim().length === 0) {
      return null
    }

    const nextThread = await travelMvpApiClient.sendFeedbackMessage(threadId, {
      senderRole,
      senderDisplayName,
      body,
    })
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  createOrderCancellationMessage: async ({ threadId, orderId, reason }) => {
    const nextThread = await travelMvpApiClient.createOrderCancellationMessage({ threadId, orderId, reason })
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  handleOrderCancellationRequest: async ({ threadId, messageId, status, managerNote, handledBy, handlerRole }) => {
    const nextThread = await travelMvpApiClient.handleOrderCancellationRequest({
      threadId,
      messageId,
      status,
      managerNote,
      handledBy,
      handlerRole,
    })
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  markThreadRead: async (threadId, audience) => {
    const matchingThread = [
      ...get().userThreads,
      ...get().managerThreads,
      ...get().siteAdminUserThreads,
      ...get().siteAdminManagerThreads,
    ].find(thread => thread.threadId === threadId)

    if (!matchingThread) {
      return null
    }

    const unreadCount =
      audience === 'User'
        ? matchingThread.unreadByUser
        : audience === 'Manager'
          ? matchingThread.unreadByManager
          : matchingThread.unreadBySiteAdmin

    if (unreadCount === 0) {
      return matchingThread
    }

    const nextThread = await travelMvpApiClient.markFeedbackThreadRead(threadId, { audience })
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  escalateThread: async ({ threadId, senderDisplayName, body }) => {
    const nextThread = await travelMvpApiClient.escalateFeedbackThread(threadId, {
      senderDisplayName,
      body,
    })
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  createComplaint: async params => {
    const nextThread = await travelMvpApiClient.createFeedbackComplaint(params)
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  openComplaintManagerThread: async params => {
    const nextThread = await travelMvpApiClient.openComplaintManagerThread(params)
    set(state => syncThreadBuckets(state, nextThread))
    return nextThread
  },
  setActiveMiniThread: thread => set({ activeMiniThread: thread }),
  clearAllThreads: () =>
    set({
      userThreads: [],
      managerThreads: [],
      siteAdminUserThreads: [],
      siteAdminManagerThreads: [],
      activeMiniThread: null,
      isLoading: false,
  }),
}))

// 读取反馈聊天仓库快照。
export function getFeedbackChatSnap() {
  const {
    userThreads,
    managerThreads,
    siteAdminUserThreads,
    siteAdminManagerThreads,
    activeMiniThread,
    isLoading,
  } = useFeedbackChatStore.getState()

  return {
    userThreads,
    managerThreads,
    siteAdminUserThreads,
    siteAdminManagerThreads,
    activeMiniThread,
    isLoading,
  }
}

// 确保订单取消线程存在。
export function ensureOrderCancellationThread(params: { userId: string; orderId: string }) {
  return useFeedbackChatStore.getState().ensureOrderCancellationThread(params)
}

// 发送一条反馈消息。
export function sendFeedbackMessage(params: {
  threadId: string
  senderRole: Exclude<FeedbackSenderRole, 'System'>
  senderDisplayName: string
  body: string
}) {
  return useFeedbackChatStore.getState().sendMessage(params)
}

// 创建订单取消消息。
export function createOrderCancellationMessage(params: { threadId: string; orderId: string; reason: string }) {
  return useFeedbackChatStore.getState().createOrderCancellationMessage(params)
}

// 处理订单取消请求。
export function handleOrderCancellationRequest(params: {
  threadId: string
  messageId: string
  status: Exclude<OrderCancellationRequestStatus, 'pending'>
  managerNote?: string | null
  handledBy?: string | null
  handlerRole?: FeedbackSenderRole | null
}) {
  return useFeedbackChatStore.getState().handleOrderCancellationRequest(params)
}

// 标记反馈线程已读。
export function markFeedbackThreadRead(threadId: string, audience: FeedbackAudience) {
  return useFeedbackChatStore.getState().markThreadRead(threadId, audience)
}

// 创建管理者升级线程。
export function createManagerEscalationThread(params: { threadId: string; senderDisplayName: string; body: string }) {
  return useFeedbackChatStore.getState().escalateThread(params)
}

// 创建投诉线程。
export function createFeedbackComplaint(params: { sourceThreadId: string; selectedMessageIds: string[]; userExplanation: string; userDisplayName: string }) {
  return useFeedbackChatStore.getState().createComplaint(params)
}

// 打开投诉对应的管理者线程。
export function openComplaintManagerThread(params: { complaintMessageId: string; siteAdminActorId: string }) {
  return useFeedbackChatStore.getState().openComplaintManagerThread(params)
}

// 设置当前激活的迷你反馈线程。
export function setActiveFeedbackMiniThread(thread: FeedbackThread | null) {
  useFeedbackChatStore.getState().setActiveMiniThread(thread)
}

// 读取用户侧反馈线程。
export function getFeedbackThreadsForUser() {
  return useFeedbackChatStore.getState().userThreads
}

// 读取管理者侧反馈线程。
export function getFeedbackThreadsForManager(managerType?: FeedbackManagerType) {
  const threads = useFeedbackChatStore.getState().managerThreads
  return managerType ? threads.filter(thread => thread.managerType === managerType) : threads
}

// 读取站点管理员侧反馈线程。
export function getFeedbackThreadsForSiteAdmin(channel: FeedbackSiteAdminChannel) {
  return channel === 'user'
  ? useFeedbackChatStore.getState().siteAdminUserThreads
    : useFeedbackChatStore.getState().siteAdminManagerThreads
}

// 清空所有反馈线程缓存。
export function clearAllThreads() {
  useFeedbackChatStore.getState().clearAllThreads()
}
