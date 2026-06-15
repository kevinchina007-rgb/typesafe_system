// 本文件定义后端 LogoutOtherSessionsPlanner 对应的登出其他会话请求结构，并提供 JSON 编解码。

export type LogoutOtherSessionsPlannerRequest = {
  sessionId: string
}

export const logoutOtherSessionsPlannerRequestFromJson = (json: string): LogoutOtherSessionsPlannerRequest =>
  JSON.parse(json) as LogoutOtherSessionsPlannerRequest

export const logoutOtherSessionsPlannerRequestToJson = (value: LogoutOtherSessionsPlannerRequest): string =>
  JSON.stringify(value)
