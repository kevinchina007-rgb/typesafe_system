import { create } from 'zustand'

import type { CurrentManagerSessionResponse, UserResponse } from '../../lib/mvp-types'

type AuthStoreState = {
  hasResolvedPrincipalState: boolean
  signedInUser: UserResponse | null
  signedInManagerSession: CurrentManagerSessionResponse | null
}

type AuthStoreActions = {
  setHasResolvedPrincipalState: (hasResolvedPrincipalState: boolean) => void
  setSignedInUser: (signedInUser: UserResponse | null) => void
  setSignedInManagerSession: (signedInManagerSession: CurrentManagerSessionResponse | null) => void
  clearPrincipalState: () => void
}

type AuthStore = AuthStoreState & AuthStoreActions

const authStoreDefaultState: AuthStoreState = {
  hasResolvedPrincipalState: false,
  signedInUser: null,
  signedInManagerSession: null,
}

export const useAuthStore = create<AuthStore>(set => ({
  ...authStoreDefaultState,
  setHasResolvedPrincipalState: hasResolvedPrincipalState => set({ hasResolvedPrincipalState }),
  setSignedInUser: signedInUser => set({ signedInUser }),
  setSignedInManagerSession: signedInManagerSession => set({ signedInManagerSession }),
  clearPrincipalState: () => set({ ...authStoreDefaultState }),
}))

export function getAuthSnap(): AuthStoreState {
  const { hasResolvedPrincipalState, signedInUser, signedInManagerSession } = useAuthStore.getState()
  return { hasResolvedPrincipalState, signedInUser, signedInManagerSession }
}

export function setPrincipalStateResolved(hasResolvedPrincipalState: boolean) {
  useAuthStore.getState().setHasResolvedPrincipalState(hasResolvedPrincipalState)
}

export function setCurrentUserSession(signedInUser: UserResponse | null) {
  useAuthStore.getState().setSignedInUser(signedInUser)
}

export function setCurrentManagerSession(signedInManagerSession: CurrentManagerSessionResponse | null) {
  useAuthStore.getState().setSignedInManagerSession(signedInManagerSession)
}

export function clearPrincipalState() {
  useAuthStore.getState().clearPrincipalState()
}
