// 本文件定义 feedback 模块的 `CreateFeedbackComplaintPlannerRequest`，用于提交投诉创建所需的线程来源、选中消息和用户说明。

export type CreateFeedbackComplaintPlannerRequest = {
  sourceThreadId: string
  selectedMessageIds: string[]
  userExplanation: string
  userDisplayName: string
}