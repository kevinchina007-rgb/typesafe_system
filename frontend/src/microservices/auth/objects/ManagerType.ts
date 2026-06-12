// 管理员类型枚举。
export type ManagerType = 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin'

// 把管理员类型 JSON 解析成对象。
export const managerTypeFromJson = (json: string): ManagerType =>
  JSON.parse(json) as ManagerType

// 把管理员类型对象序列化成 JSON。
export const managerTypeToJson = (value: ManagerType): string =>
  JSON.stringify(value)
