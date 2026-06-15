import type { CurrentManagerPlannerResponse } from './CurrentManagerPlannerResponse'

// 本文件保留为兼容层，供旧页面继续使用“管理员会话对象”的外壳。
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
