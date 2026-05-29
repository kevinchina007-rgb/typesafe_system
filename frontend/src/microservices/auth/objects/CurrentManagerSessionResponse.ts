export type CurrentManagerSessionResponse = {

  managerId: string

  managerType: string

  email: string

  displayName: string

  status: string

  scopeId: string

  logoAssetPath?: string | null

  createdAt: string

  expiresAt: string

}
export const currentManagerSessionResponseFromJson = (json: string): CurrentManagerSessionResponse =>
  JSON.parse(json) as CurrentManagerSessionResponse

export const currentManagerSessionResponseToJson = (value: CurrentManagerSessionResponse): string =>
  JSON.stringify(value)
