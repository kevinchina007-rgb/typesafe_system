import type { CurrentUserPlannerResponse } from './CurrentUserPlannerResponse'
import type { UserResponse } from './UserResponse'

// 本文件保留为兼容层，供旧页面继续使用“用户对象 + 过期时间”的会话外壳。
export type CurrentUserSessionResponse = {
  user: UserResponse
  expiresAt: string
}

export function currentUserSessionResponseFromPlannerResponse(plannerResponse: CurrentUserPlannerResponse): CurrentUserSessionResponse {
  return {
    user: {
      userId: plannerResponse.userId,
      email: plannerResponse.email,
      nickname: plannerResponse.nickname,
      phone: plannerResponse.phone,
      avatarUrl: plannerResponse.avatarUrl,
      status: 'Active',
      membershipLevel: plannerResponse.membershipLevel,
      points: plannerResponse.points,
      defaultTravelerProfileId: null,
      createdAt: new Date().toISOString(),
    },
    expiresAt: plannerResponse.expiresAt,
  }
}

export const currentUserSessionResponseFromJson = (json: string): CurrentUserSessionResponse =>
  JSON.parse(json) as CurrentUserSessionResponse

export const currentUserSessionResponseToJson = (value: CurrentUserSessionResponse): string =>
  JSON.stringify(value)
