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

export const useUserStore = create<UserStore>(set => ({
  ...userStoreDefaultState,
  setHasResolvedUserState: hasResolvedUserState => set({ hasResolvedUserState }),
  setSignedInUser: signedInUser => set(state => ({ signedInUser, userTravelers: signedInUser ? state.userTravelers : [] })),
  setUserTravelers: userTravelers => set({ userTravelers }),
  clearUserState: () => set({ ...userStoreDefaultState }),
}))

export function getUserSnap(): UserStoreState {
  const { hasResolvedUserState, signedInUser, userTravelers } = useUserStore.getState()
  return { hasResolvedUserState, signedInUser, userTravelers }
}

export function setUserStateResolved(hasResolvedUserState: boolean) {
  useUserStore.getState().setHasResolvedUserState(hasResolvedUserState)
}

export function setCurrentUserSession(signedInUser: UserResponse | null) {
  useUserStore.getState().setSignedInUser(signedInUser)
}

export function setCurrentUserTravelers(userTravelers: TravelerResponse[]) {
  useUserStore.getState().setUserTravelers(userTravelers)
}

export function clearUserState() {
  useUserStore.getState().clearUserState()
}
