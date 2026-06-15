// 后端健康检查接口返回的数据结构。
export type HealthResponse = {
  status: string
  service: string
  timestamp?: string
  backendPort?: number
}

// 把健康检查 JSON 文本解析成对象。
export const healthResponseFromJson = (json: string): HealthResponse =>
  JSON.parse(json) as HealthResponse

// 把健康检查对象序列化成 JSON 文本。
export const healthResponseToJson = (value: HealthResponse): string =>
  JSON.stringify(value)
