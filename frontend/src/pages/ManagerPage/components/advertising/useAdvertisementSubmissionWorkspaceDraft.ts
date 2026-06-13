import type { AdvertisementSubmissionWorkspaceProps } from './AdvertisementSubmissionWorkspaceSupport'
import { validateDraftName } from './useAdvertisementSubmissionWorkspaceValidation'

export function useAdvertisementSubmissionWorkspaceDraft({
  draftNameInput,
  setDraftNameInput,
  setDraftName,
  setIsDraftNameDialogOpen,
  setWorkspaceTab,
  resetComposer,
  onShowNotice,
}: {
  draftNameInput: string
  setDraftNameInput: (value: string) => void
  setDraftName: (value: string) => void
  setIsDraftNameDialogOpen: (value: boolean) => void
  setWorkspaceTab: (value: 'create' | 'drafts') => void
  resetComposer: () => void
  onShowNotice?: AdvertisementSubmissionWorkspaceProps['onShowNotice']
}) {
  function openNewDraftDialog() {
    setDraftNameInput('')
    setIsDraftNameDialogOpen(true)
  }

  function confirmNewDraft() {
    const nextName = validateDraftName({ draftName: draftNameInput, onShowNotice })
    if (!nextName) {
      return
    }
    resetComposer()
    setDraftName(nextName)
    setDraftNameInput(nextName)
    setIsDraftNameDialogOpen(false)
    setWorkspaceTab('create')
  }

  return {
    openNewDraftDialog,
    confirmNewDraft,
  }
}
