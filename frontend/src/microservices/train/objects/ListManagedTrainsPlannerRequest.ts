// 本文件定义 train 模块的 `ListManagedTrainsPlannerRequest`，用于管理员列出可管理火车并提供 JSON 编解码。

export type ListManagedTrainsPlannerRequest = {
  managerId: string
}
export const listManagedTrainsPlannerRequestFromJson = (json: string): ListManagedTrainsPlannerRequest =>
  JSON.parse(json) as ListManagedTrainsPlannerRequest

export const listManagedTrainsPlannerRequestToJson = (value: ListManagedTrainsPlannerRequest): string =>
  JSON.stringify(value)
