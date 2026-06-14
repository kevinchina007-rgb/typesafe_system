// 本文件定义 flight 模块的 `CabinInventoryPlannerResponse`，作为舱位响应数据并提供 JSON 编解码。

export type CabinInventoryPlannerResponse = {
  inventoryId: string
  cabinClass: string
  availableSeats: number
  unitPrice: string
  currency: string
  status: string
  isBookable: boolean
}

export const cabinInventoryPlannerResponseFromJson = (json: string): CabinInventoryPlannerResponse =>
  JSON.parse(json) as CabinInventoryPlannerResponse

export const cabinInventoryPlannerResponseToJson = (value: CabinInventoryPlannerResponse): string =>
  JSON.stringify(value)

