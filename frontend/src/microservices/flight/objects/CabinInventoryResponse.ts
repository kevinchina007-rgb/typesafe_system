// 本文件定义 flight 模块的 `CabinInventoryResponse`，作为响应数据并提供 JSON 编解码。

export type CabinInventoryResponse = {
  inventoryId: string
  cabinClass: string
  availableSeats: number
  unitPrice: string
  currency: string
  status: string
  isBookable: boolean
}
export const cabinInventoryResponseFromJson = (json: string): CabinInventoryResponse =>
  JSON.parse(json) as CabinInventoryResponse

export const cabinInventoryResponseToJson = (value: CabinInventoryResponse): string =>
  JSON.stringify(value)
