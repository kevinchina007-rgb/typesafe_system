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
