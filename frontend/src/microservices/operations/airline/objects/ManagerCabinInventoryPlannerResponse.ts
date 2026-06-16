// 本文件定义航空管理端的舱位库存响应结构。

export type ManagerCabinInventoryPlannerResponse = {
  inventoryId: string
  cabinClass: string
  availableSeats: number
  unitPrice: string
  currency: string
  status: string
  isBookable: boolean
}

export const managerCabinInventoryPlannerResponseFromJson = (json: string): ManagerCabinInventoryPlannerResponse =>
  JSON.parse(json) as ManagerCabinInventoryPlannerResponse

export const managerCabinInventoryPlannerResponseToJson = (value: ManagerCabinInventoryPlannerResponse): string =>
  JSON.stringify(value)
