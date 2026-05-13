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

export const useManagerStore = create<ManagerStore>(set => ({
  ...managerStoreDefaultState,
  setHasResolvedManagerState: hasResolvedManagerState => set({ hasResolvedManagerState }),
  setSignedInManagerSession: signedInManagerSession => set({ signedInManagerSession }),
  clearManagerState: () => set({ ...managerStoreDefaultState }),
}))

export function getManagerSnap(): ManagerStoreState {
  const { hasResolvedManagerState, signedInManagerSession } = useManagerStore.getState()
  return { hasResolvedManagerState, signedInManagerSession }
}

export function setManagerStateResolved(hasResolvedManagerState: boolean) {
  useManagerStore.getState().setHasResolvedManagerState(hasResolvedManagerState)
}

export function setCurrentManagerSession(signedInManagerSession: CurrentManagerSessionResponse | null) {
  useManagerStore.getState().setSignedInManagerSession(signedInManagerSession)
}

export function clearManagerState() {
  useManagerStore.getState().clearManagerState()
}
