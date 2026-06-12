import type { CurrentUserSessionResponse } from '@/microservices/auth/objects/CurrentUserSessionResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

// 登录接口返回的后端原始数据结构。
type CurrentUserPlannerResponse = {
  sessionId: string
  userId: string
  email: string
  nickname: string
  phone: string
  avatarUrl: string | null
  membershipLevel: string
  points: number
  expiresAt: string
}

// 用户会话在本地存储里的 key。
const userSessionStorageKey = 'flypig.userSessionId'

// 保存用户会话 ID，供后续请求复用。
function rememberUserSession(sessionId: string) {
  window.localStorage.setItem(userSessionStorageKey, sessionId)
}

// 把登录接口原始返回转换成前端会话对象。
function toCurrentUserSessionResponse(plannerResponse: CurrentUserPlannerResponse): CurrentUserSessionResponse {
  rememberUserSession(plannerResponse.sessionId)
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

// 用户使用邮箱和密码登录。
export const loginUserWithPassword = (payload: {
  email: string
  password: string
}): Promise<CurrentUserSessionResponse> =>
  executeJsonApiRequest<CurrentUserPlannerResponse>('/LoginPlanner', 'POST', payload).then(toCurrentUserSessionResponse)
