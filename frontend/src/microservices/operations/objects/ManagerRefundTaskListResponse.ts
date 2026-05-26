import type { ManagerRefundTaskResponse } from './ManagerRefundTaskResponse'

export type ManagerRefundTaskListResponse = {
  tasks: ManagerRefundTaskResponse[]
}
export const managerRefundTaskListResponseFromJson = (json: string): ManagerRefundTaskListResponse =>
  JSON.parse(json) as ManagerRefundTaskListResponse

export const managerRefundTaskListResponseToJson = (value: ManagerRefundTaskListResponse): string =>
  JSON.stringify(value)
