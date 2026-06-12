// 本文件定义 operations 模块的 `ManagerTaskListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { ManagerTaskResponse } from './ManagerTaskResponse'

export type ManagerTaskListResponse = {
  tasks: ManagerTaskResponse[]
}
export const managerTaskListResponseFromJson = (json: string): ManagerTaskListResponse =>
  JSON.parse(json) as ManagerTaskListResponse

export const managerTaskListResponseToJson = (value: ManagerTaskListResponse): string =>
  JSON.stringify(value)
