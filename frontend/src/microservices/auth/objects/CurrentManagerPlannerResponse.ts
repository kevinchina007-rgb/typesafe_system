// 本文件定义后端 CurrentManagerPlanner 对应的当前管理员会话响应结构，并提供 JSON 编解码。

export type CurrentManagerPlannerResponse = {
  sessionId: string
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  logoAssetPath?: string | null
  createdAt: string
  expiresAt: string
}

export const currentManagerPlannerResponseFromJson = (json: string): CurrentManagerPlannerResponse =>
  JSON.parse(json) as CurrentManagerPlannerResponse

export const currentManagerPlannerResponseToJson = (value: CurrentManagerPlannerResponse): string =>
  JSON.stringify(value)
