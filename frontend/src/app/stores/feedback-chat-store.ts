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
  setActiveMiniThread: (thread: FeedbackThread | null) => void
  clearAllThreads: () => void
}

type FeedbackChatStore = FeedbackChatState & FeedbackChatActions

function sortThreads(threads: FeedbackThread[]) {
  return [...threads].sort((left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt))
}

function upsertThread(threads: FeedbackThread[], nextThread: FeedbackThread) {
  const existingIndex = threads.findIndex(thread => thread.threadId === nextThread.threadId)
  if (existingIndex < 0) {
    return sortThreads([nextThread, ...threads])
  }

  const nextThreads = [...threads]
  nextThreads[existingIndex] = nextThread
  return sortThreads(nextThreads)
}

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

export function ensureOrderCancellationThread(params: { userId: string; orderId: string }) {
  return useFeedbackChatStore.getState().ensureOrderCancellationThread(params)
}

export function sendFeedbackMessage(params: {
  threadId: string
  senderRole: Exclude<FeedbackSenderRole, 'System'>
  senderDisplayName: string
  body: string
}) {
  return useFeedbackChatStore.getState().sendMessage(params)
}

export function createOrderCancellationMessage(params: { threadId: string; orderId: string; reason: string }) {
  return useFeedbackChatStore.getState().createOrderCancellationMessage(params)
}

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

export function markFeedbackThreadRead(threadId: string, audience: FeedbackAudience) {
  return useFeedbackChatStore.getState().markThreadRead(threadId, audience)
}

export function createManagerEscalationThread(params: { threadId: string; senderDisplayName: string; body: string }) {
  return useFeedbackChatStore.getState().escalateThread(params)
}

export function setActiveFeedbackMiniThread(thread: FeedbackThread | null) {
  useFeedbackChatStore.getState().setActiveMiniThread(thread)
}

export function getFeedbackThreadsForUser() {
  return useFeedbackChatStore.getState().userThreads
}

export function getFeedbackThreadsForManager(managerType?: FeedbackManagerType) {
  const threads = useFeedbackChatStore.getState().managerThreads
  return managerType ? threads.filter(thread => thread.managerType === managerType) : threads
}

export function getFeedbackThreadsForSiteAdmin(channel: FeedbackSiteAdminChannel) {
  return channel === 'user'
    ? useFeedbackChatStore.getState().siteAdminUserThreads
    : useFeedbackChatStore.getState().siteAdminManagerThreads
}

export function clearAllThreads() {
  useFeedbackChatStore.getState().clearAllThreads()
}
