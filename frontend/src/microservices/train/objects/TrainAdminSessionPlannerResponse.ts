// 本文件定义 train 模块的 `TrainAdminSessionPlannerResponse`，用于表示铁路管理员会话并提供 JSON 编解码。

import type { TrainPlannerResponse } from './TrainPlannerResponse'

export type TrainAdminSessionPlannerResponse = {
  managerId: string
  operatorCode: string
  email: string
  displayName: string
  status: string
  managedTrains: TrainPlannerResponse[]
}
export const trainAdminSessionPlannerResponseFromJson = (json: string): TrainAdminSessionPlannerResponse =>
  JSON.parse(json) as TrainAdminSessionPlannerResponse

export const trainAdminSessionPlannerResponseToJson = (value: TrainAdminSessionPlannerResponse): string =>
  JSON.stringify(value)
