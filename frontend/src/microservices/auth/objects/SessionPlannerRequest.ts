// 本文件定义后端 SessionPlannerRequest 对应的会话请求结构，并提供 JSON 编解码。

export type SessionPlannerRequest = {
  sessionId: string
}

export const sessionPlannerRequestFromJson = (json: string): SessionPlannerRequest =>
  JSON.parse(json) as SessionPlannerRequest

export const sessionPlannerRequestToJson = (value: SessionPlannerRequest): string =>
  JSON.stringify(value)
