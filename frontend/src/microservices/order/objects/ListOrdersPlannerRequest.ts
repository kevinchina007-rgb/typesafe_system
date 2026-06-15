// 本文件定义订单列表查询请求，和后端 `ListOrdersPlannerRequest` 保持同名镜像。

export type ListOrdersPlannerRequest = {
  userId: string
}

export const listOrdersPlannerRequestFromJson = (json: string): ListOrdersPlannerRequest =>
  JSON.parse(json) as ListOrdersPlannerRequest

export const listOrdersPlannerRequestToJson = (value: ListOrdersPlannerRequest): string =>
  JSON.stringify(value)
