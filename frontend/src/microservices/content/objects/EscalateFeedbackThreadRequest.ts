// 本文件定义 content 模块的 `EscalateFeedbackThreadRequest`，作为请求参数并提供 JSON 编解码。

export type EscalateFeedbackThreadRequest = {
  senderDisplayName: string
  body: string
}
export const escalateFeedbackThreadRequestFromJson = (json: string): EscalateFeedbackThreadRequest =>
  JSON.parse(json) as EscalateFeedbackThreadRequest

export const escalateFeedbackThreadRequestToJson = (value: EscalateFeedbackThreadRequest): string =>
  JSON.stringify(value)
