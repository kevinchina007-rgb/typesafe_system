// 本文件定义 feedback 模块的 `OpenComplaintManagerThreadPlannerRequest`，用于把投诉消息转换为站内客服管理线程。

export type OpenComplaintManagerThreadPlannerRequest = {
  complaintMessageId: string
  siteAdminActorId: string
}