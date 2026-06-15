// 本文件定义后端 ManagerSessionPlannerResponse 对应的管理员会话条目结构，并提供 JSON 编解码。

export type ManagerSessionPlannerResponse = {
  sessionId: string
  createdAt: string
  lastSeenAt: string
  expiresAt: string
  status: string
  isCurrent: boolean
}

export const managerSessionPlannerResponseFromJson = (json: string): ManagerSessionPlannerResponse =>
  JSON.parse(json) as ManagerSessionPlannerResponse

export const managerSessionPlannerResponseToJson = (value: ManagerSessionPlannerResponse): string =>
  JSON.stringify(value)
