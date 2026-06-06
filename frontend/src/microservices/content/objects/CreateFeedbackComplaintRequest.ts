export type CreateFeedbackComplaintRequest = {
  sourceThreadId: string
  selectedMessageIds: string[]
  userExplanation: string
  userDisplayName: string
}
