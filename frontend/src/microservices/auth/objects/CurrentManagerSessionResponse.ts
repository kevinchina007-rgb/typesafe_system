import type { CurrentManagerPlannerResponse } from './CurrentManagerPlannerResponse'

// 本文件定义当前管理员会话展示对象，保留给旧页面直接消费。
export type CurrentManagerSessionResponse = Omit<CurrentManagerPlannerResponse, 'sessionId'>

export function currentManagerSessionResponseFromPlannerResponse(
  plannerResponse: CurrentManagerPlannerResponse,
): CurrentManagerSessionResponse {
  return {
    managerId: plannerResponse.managerId,
    managerType: plannerResponse.managerType,
    email: plannerResponse.email,
    displayName: plannerResponse.displayName,
    status: plannerResponse.status,
    scopeId: plannerResponse.scopeId,
    logoAssetPath: plannerResponse.logoAssetPath,
    createdAt: plannerResponse.createdAt,
    expiresAt: plannerResponse.expiresAt,
  }
}

export const currentManagerSessionResponseFromJson = (json: string): CurrentManagerSessionResponse =>
  JSON.parse(json) as CurrentManagerSessionResponse

export const currentManagerSessionResponseToJson = (value: CurrentManagerSessionResponse): string =>
  JSON.stringify(value)
