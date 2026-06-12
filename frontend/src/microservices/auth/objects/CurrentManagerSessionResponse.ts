// 当前管理员登录后返回的会话结构。
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

// 把当前管理员会话 JSON 解析成对象。
export const currentManagerSessionResponseFromJson = (json: string): CurrentManagerSessionResponse =>
  JSON.parse(json) as CurrentManagerSessionResponse

// 把当前管理员会话对象序列化成 JSON。
export const currentManagerSessionResponseToJson = (value: CurrentManagerSessionResponse): string =>
  JSON.stringify(value)
