// 本文件定义后端 AuthStatusPlannerResponse 对应的状态响应结构，并提供 JSON 编解码。

export type AuthStatusPlannerResponse = {
  status: string
  revokedCount: number | null
}

export const authStatusPlannerResponseFromJson = (json: string): AuthStatusPlannerResponse =>
  JSON.parse(json) as AuthStatusPlannerResponse

export const authStatusPlannerResponseToJson = (value: AuthStatusPlannerResponse): string =>
  JSON.stringify(value)
