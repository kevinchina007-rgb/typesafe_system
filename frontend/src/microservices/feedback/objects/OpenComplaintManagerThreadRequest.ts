// 本文件定义 content 模块的 `OpenComplaintManagerThreadRequest`，作为请求参数并提供 JSON 编解码。

export type OpenComplaintManagerThreadRequest = {
  complaintMessageId: string
  siteAdminActorId: string
}
