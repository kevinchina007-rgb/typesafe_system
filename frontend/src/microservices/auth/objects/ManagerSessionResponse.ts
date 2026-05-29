export type ManagerSessionResponse = {

  managerId: string

  managerType: string

  email: string

  displayName: string

  status: string

  scopeId: string

  logoAssetPath?: string | null

  createdAt: string

}
export const managerSessionResponseFromJson = (json: string): ManagerSessionResponse =>
  JSON.parse(json) as ManagerSessionResponse

export const managerSessionResponseToJson = (value: ManagerSessionResponse): string =>
  JSON.stringify(value)
