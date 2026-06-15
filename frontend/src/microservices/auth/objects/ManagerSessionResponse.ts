import type { CurrentManagerPlannerResponse } from './CurrentManagerPlannerResponse'

// 本文件定义管理员会话摘要对象，供旧管理页继续直接消费。
export type ManagerSessionResponse = Omit<CurrentManagerPlannerResponse, 'sessionId' | 'expiresAt'>

export const managerSessionResponseFromJson = (json: string): ManagerSessionResponse =>
  JSON.parse(json) as ManagerSessionResponse

export const managerSessionResponseToJson = (value: ManagerSessionResponse): string =>
  JSON.stringify(value)
