// 本文件定义 operations 模块的 `ManagerRefundTaskListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { ManagerRefundTaskResponse } from './ManagerRefundTaskResponse'

export type ManagerRefundTaskListResponse = {
  tasks: ManagerRefundTaskResponse[]
}
export const managerRefundTaskListResponseFromJson = (json: string): ManagerRefundTaskListResponse =>
  JSON.parse(json) as ManagerRefundTaskListResponse

export const managerRefundTaskListResponseToJson = (value: ManagerRefundTaskListResponse): string =>
  JSON.stringify(value)
