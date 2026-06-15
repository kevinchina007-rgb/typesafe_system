// 本文件定义 feedback 模块的 `EscalateFeedbackThreadPlannerRequest`，用于把已有线程升级为需要管理员介入的跟进线程。

export type EscalateFeedbackThreadPlannerRequest = {
  senderDisplayName: string
  body: string
}
export const escalateFeedbackThreadRequestFromJson = (json: string): EscalateFeedbackThreadPlannerRequest =>
  JSON.parse(json) as EscalateFeedbackThreadPlannerRequest

export const escalateFeedbackThreadRequestToJson = (value: EscalateFeedbackThreadPlannerRequest): string =>
  JSON.stringify(value)