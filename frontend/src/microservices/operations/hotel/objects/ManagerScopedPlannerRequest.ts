export type ManagerScopedPlannerRequest = {
  managerId: string
  managerType: string
}

export const managerScopedPlannerRequestFromJson = (json: string): ManagerScopedPlannerRequest =>
  JSON.parse(json) as ManagerScopedPlannerRequest

export const managerScopedPlannerRequestToJson = (value: ManagerScopedPlannerRequest): string =>
  JSON.stringify(value)
