// 本文件定义 `ListManagerRefundTasksPlanner` 返回的退款任务列表对象。

import type { ManagerRefundTaskResponse } from './ManagerRefundTaskResponse'

export type ManagerRefundTaskListResponse = {
  tasks: ManagerRefundTaskResponse[]
}

export const managerRefundTaskListResponseFromJson = (json: string): ManagerRefundTaskListResponse =>
  JSON.parse(json) as ManagerRefundTaskListResponse

export const managerRefundTaskListResponseToJson = (value: ManagerRefundTaskListResponse): string =>
  JSON.stringify(value)
