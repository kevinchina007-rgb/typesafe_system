export type ListManagedTrainsPlannerRequest = {
  managerId: string
}

export const listManagedTrainsPlannerRequestFromJson = (json: string): ListManagedTrainsPlannerRequest =>
  JSON.parse(json) as ListManagedTrainsPlannerRequest

export const listManagedTrainsPlannerRequestToJson = (value: ListManagedTrainsPlannerRequest): string =>
  JSON.stringify(value)

