// 本文件定义后端 ManagerAuthStatusPlannerResponse 对应的管理员状态响应结构，并提供 JSON 编解码。

export type ManagerAuthStatusPlannerResponse = {
  status: string
  revokedCount: number | null
}

export const managerAuthStatusPlannerResponseFromJson = (json: string): ManagerAuthStatusPlannerResponse =>
  JSON.parse(json) as ManagerAuthStatusPlannerResponse

export const managerAuthStatusPlannerResponseToJson = (value: ManagerAuthStatusPlannerResponse): string =>
  JSON.stringify(value)
