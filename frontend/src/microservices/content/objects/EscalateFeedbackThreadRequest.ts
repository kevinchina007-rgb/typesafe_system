export type EscalateFeedbackThreadRequest = {
  senderDisplayName: string
  body: string
}
export const escalateFeedbackThreadRequestFromJson = (json: string): EscalateFeedbackThreadRequest =>
  JSON.parse(json) as EscalateFeedbackThreadRequest

export const escalateFeedbackThreadRequestToJson = (value: EscalateFeedbackThreadRequest): string =>
  JSON.stringify(value)
