import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { cloneCreative, defaultCreative, parseCreativeJson } from './AdvertisementSubmissionWorkspaceSupport'

export function useAdvertisementSubmissionWorkspaceRestore({
  setEditingAdvertisementId,
  setDraftName,
  setDraftNameInput,
  setTargetResourceId,
  setHyperlinkEnabled,
  setCreative,
  setTextPrompt,
  setImagePrompt,
  setFlightSearchResults,
  setHasSearchedFlights,
  setWorkspaceTab,
}: {
  setEditingAdvertisementId: (value: string | null) => void
  setDraftName: (value: string) => void
  setDraftNameInput: (value: string) => void
  setTargetResourceId: (value: string) => void
  setHyperlinkEnabled: (value: boolean) => void
  setCreative: (value: ReturnType<typeof cloneCreative>) => void
  setTextPrompt: (value: string) => void
  setImagePrompt: (value: string) => void
  setFlightSearchResults: (value: any) => void
  setHasSearchedFlights: (value: boolean) => void
  setWorkspaceTab: (value: 'create' | 'drafts') => void
}) {
  function openAdvertisementDraft(advertisement: AdvertisementResponse) {
    const restoredCreative = parseCreativeJson(advertisement.creativeJson)
    setEditingAdvertisementId(advertisement.advertisementId)
    setDraftName(advertisement.title)
    setDraftNameInput(advertisement.title)
    setTargetResourceId(advertisement.targetResourceId)
    setHyperlinkEnabled(advertisement.landingTarget !== 'disabled')
    setCreative(restoredCreative ? cloneCreative(restoredCreative) : cloneCreative(defaultCreative))
    setTextPrompt(advertisement.title)
    setImagePrompt(advertisement.landingTarget === 'disabled' ? '' : advertisement.resourceSummaryTitle)
    setFlightSearchResults([])
    setHasSearchedFlights(false)
    setWorkspaceTab('create')
  }

  return { openAdvertisementDraft }
}
