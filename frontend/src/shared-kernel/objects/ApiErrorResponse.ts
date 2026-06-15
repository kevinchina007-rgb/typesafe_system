// 后端返回的标准错误结构。
export type ApiErrorResponse = {
  code: string
  message: string
}

// 把 JSON 文本解析成错误结构。
export const apiErrorResponseFromJson = (json: string): ApiErrorResponse =>
  JSON.parse(json) as ApiErrorResponse

// 把错误结构序列化成 JSON 文本。
export const apiErrorResponseToJson = (value: ApiErrorResponse): string =>
  JSON.stringify(value)
