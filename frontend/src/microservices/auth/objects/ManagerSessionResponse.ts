import type { CurrentManagerPlannerResponse } from './CurrentManagerPlannerResponse'

// 本文件保留为兼容层，供旧管理页继续使用管理员会话摘要结构。
export type ManagerSessionResponse = Omit<CurrentManagerPlannerResponse, 'sessionId' | 'expiresAt'>

export const managerSessionResponseFromJson = (json: string): ManagerSessionResponse =>
  JSON.parse(json) as ManagerSessionResponse

export const managerSessionResponseToJson = (value: ManagerSessionResponse): string =>
  JSON.stringify(value)
