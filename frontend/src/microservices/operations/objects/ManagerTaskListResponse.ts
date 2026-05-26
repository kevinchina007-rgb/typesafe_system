import type { ManagerTaskResponse } from './ManagerTaskResponse'

export type ManagerTaskListResponse = {
  tasks: ManagerTaskResponse[]
}
export const managerTaskListResponseFromJson = (json: string): ManagerTaskListResponse =>
  JSON.parse(json) as ManagerTaskListResponse

export const managerTaskListResponseToJson = (value: ManagerTaskListResponse): string =>
  JSON.stringify(value)
