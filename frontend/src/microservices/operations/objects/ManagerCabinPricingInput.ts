export type ManagerCabinPricingInput = {
  seatCount: number
  originalPrice: string
  discounted: boolean
  discountRate: string
}

export const managerCabinPricingInputFromJson = (json: string): ManagerCabinPricingInput =>
  JSON.parse(json) as ManagerCabinPricingInput

export const managerCabinPricingInputToJson = (value: ManagerCabinPricingInput): string =>
  JSON.stringify(value)
