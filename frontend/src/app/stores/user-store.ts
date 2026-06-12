// 本文件封装状态管理逻辑。

import { create } from 'zustand'

import type { TravelerResponse, UserResponse } from '@/lib/mvp-types/index'

type UserStoreState = {
  hasResolvedUserState: boolean
  signedInUser: UserResponse | null
  userTravelers: TravelerResponse[]
}

type UserStoreActions = {
  setHasResolvedUserState: (hasResolvedUserState: boolean) => void
  setSignedInUser: (signedInUser: UserResponse | null) => void
  setUserTravelers: (userTravelers: TravelerResponse[]) => void
  clearUserState: () => void
}

type UserStore = UserStoreState & UserStoreActions

const userStoreDefaultState: UserStoreState = {
  hasResolvedUserState: false,
  signedInUser: null,
  userTravelers: [],
}

// 用户状态仓库，统一保存登录用户和其出行人列表。
export const useUserStore = create<UserStore>(set => ({
  ...userStoreDefaultState,
  setHasResolvedUserState: hasResolvedUserState => set({ hasResolvedUserState }),
  setSignedInUser: signedInUser => set(state => ({ signedInUser, userTravelers: signedInUser ? state.userTravelers : [] })),
  setUserTravelers: userTravelers => set({ userTravelers }),
  clearUserState: () => set({ ...userStoreDefaultState }),
}))

// 读取当前用户相关状态的快照，供非 React 场景调用。
export function getUserSnap(): UserStoreState {
  const { hasResolvedUserState, signedInUser, userTravelers } = useUserStore.getState()
  return { hasResolvedUserState, signedInUser, userTravelers }
}

// 标记当前用户会话是否已经拉取完成。
export function setUserStateResolved(hasResolvedUserState: boolean) {
  useUserStore.getState().setHasResolvedUserState(hasResolvedUserState)
}

// 写入当前登录用户，会在登出时顺带清空出行人列表。
export function setCurrentUserSession(signedInUser: UserResponse | null) {
  useUserStore.getState().setSignedInUser(signedInUser)
}

// 写入当前用户的出行人列表。
export function setCurrentUserTravelers(userTravelers: TravelerResponse[]) {
  useUserStore.getState().setUserTravelers(userTravelers)
}

// 清空用户态，恢复到默认状态。
export function clearUserState() {
  useUserStore.getState().clearUserState()
}
