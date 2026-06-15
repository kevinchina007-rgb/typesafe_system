// 本文件定义后端 ManagerSessionPlannerRequest 对应的管理员会话请求结构，并提供 JSON 编解码。

export type ManagerSessionPlannerRequest = {
  sessionId: string
}

export const managerSessionPlannerRequestFromJson = (json: string): ManagerSessionPlannerRequest =>
  JSON.parse(json) as ManagerSessionPlannerRequest

export const managerSessionPlannerRequestToJson = (value: ManagerSessionPlannerRequest): string =>
  JSON.stringify(value)
