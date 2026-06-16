// 本文件定义 `ListManagerTasksPlanner` 对应的请求对象。

export type ManagerTasksPlannerRequest = {
  managerId: string
  managerType: string
  taskStatus?: string | null
  taskResourceType?: string | null
}

export const managerTasksPlannerRequestFromJson = (json: string): ManagerTasksPlannerRequest =>
  JSON.parse(json) as ManagerTasksPlannerRequest

export const managerTasksPlannerRequestToJson = (value: ManagerTasksPlannerRequest): string =>
  JSON.stringify(value)
