import type { TrainPlannerResponse } from '@/microservices/train/objects/TrainPlannerResponse'

// 本文件定义 train 管理员登录后返回的会话结果，包含管理员身份与可管理火车列表。
export type TrainAdminSessionResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainPlannerResponse[]
}

// 将火车管理员会话 JSON 解析成对象。
export const trainAdminSessionResponseFromJson = (json: string): TrainAdminSessionResponse =>
  JSON.parse(json) as TrainAdminSessionResponse

// 将火车管理员会话对象序列化成 JSON。
export const trainAdminSessionResponseToJson = (value: TrainAdminSessionResponse): string =>
  JSON.stringify(value)
