import type { TrainResponse } from '@/microservices/train/objects/TrainResponse'

// 火车管理员登录后返回的会话结构。
export type TrainAdminSessionResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainResponse[]
}

// 把火车管理员会话 JSON 解析成对象。
export const trainAdminSessionResponseFromJson = (json: string): TrainAdminSessionResponse =>
  JSON.parse(json) as TrainAdminSessionResponse

// 把火车管理员会话对象序列化成 JSON。
export const trainAdminSessionResponseToJson = (value: TrainAdminSessionResponse): string =>
  JSON.stringify(value)
