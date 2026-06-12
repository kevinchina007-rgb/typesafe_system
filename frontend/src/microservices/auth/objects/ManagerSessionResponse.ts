// 管理员会话接口返回的数据结构。
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

// 把管理员会话 JSON 解析成对象。
export const managerSessionResponseFromJson = (json: string): ManagerSessionResponse =>
  JSON.parse(json) as ManagerSessionResponse

// 把管理员会话对象序列化成 JSON。
export const managerSessionResponseToJson = (value: ManagerSessionResponse): string =>
  JSON.stringify(value)
