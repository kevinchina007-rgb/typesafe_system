// 本文件封装状态管理逻辑。

import { create } from 'zustand'

import type { CurrentManagerSessionResponse } from '@/lib/mvp-types/index'

type ManagerStoreState = {
  hasResolvedManagerState: boolean
  signedInManagerSession: CurrentManagerSessionResponse | null
}

type ManagerStoreActions = {
  setHasResolvedManagerState: (hasResolvedManagerState: boolean) => void
  setSignedInManagerSession: (signedInManagerSession: CurrentManagerSessionResponse | null) => void
  clearManagerState: () => void
}

type ManagerStore = ManagerStoreState & ManagerStoreActions

const managerStoreDefaultState: ManagerStoreState = {
  hasResolvedManagerState: false,
  signedInManagerSession: null,
}

// 管理者状态仓库，统一保存登录态和初始化完成标记。
export const useManagerStore = create<ManagerStore>(set => ({
  ...managerStoreDefaultState,
  setHasResolvedManagerState: hasResolvedManagerState => set({ hasResolvedManagerState }),
  setSignedInManagerSession: signedInManagerSession => set({ signedInManagerSession }),
  clearManagerState: () => set({ ...managerStoreDefaultState }),
}))

// 读取当前管理者状态快照，供非 React 场景调用。
export function getManagerSnap(): ManagerStoreState {
  const { hasResolvedManagerState, signedInManagerSession } = useManagerStore.getState()
  return { hasResolvedManagerState, signedInManagerSession }
}

// 标记管理者会话是否已经拉取完成。
export function setManagerStateResolved(hasResolvedManagerState: boolean) {
  useManagerStore.getState().setHasResolvedManagerState(hasResolvedManagerState)
}

// 写入当前管理者会话。
export function setCurrentManagerSession(signedInManagerSession: CurrentManagerSessionResponse | null) {
  useManagerStore.getState().setSignedInManagerSession(signedInManagerSession)
}

// 清空管理者状态，恢复到默认值。
export function clearManagerState() {
  useManagerStore.getState().clearManagerState()
}
