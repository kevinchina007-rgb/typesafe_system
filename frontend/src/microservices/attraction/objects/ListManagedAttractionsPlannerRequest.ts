// 本文件定义 ListManagedAttractionsPlanner 的景点管理列表请求。

export type ListManagedAttractionsPlannerRequest = {
  managerId: string
}

export const listManagedAttractionsPlannerRequestFromJson = (json: string): ListManagedAttractionsPlannerRequest =>
  JSON.parse(json) as ListManagedAttractionsPlannerRequest

export const listManagedAttractionsPlannerRequestToJson = (value: ListManagedAttractionsPlannerRequest): string =>
  JSON.stringify(value)
