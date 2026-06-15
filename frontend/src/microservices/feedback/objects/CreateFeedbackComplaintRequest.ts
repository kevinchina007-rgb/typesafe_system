// 本文件定义 content 模块的 `CreateFeedbackComplaintRequest`，作为请求参数并提供 JSON 编解码。

export type CreateFeedbackComplaintRequest = {
  sourceThreadId: string
  selectedMessageIds: string[]
  userExplanation: string
  userDisplayName: string
}
