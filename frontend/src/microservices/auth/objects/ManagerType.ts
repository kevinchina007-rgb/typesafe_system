export type ManagerType = 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin'
export const managerTypeFromJson = (json: string): ManagerType =>
  JSON.parse(json) as ManagerType

export const managerTypeToJson = (value: ManagerType): string =>
  JSON.stringify(value)
