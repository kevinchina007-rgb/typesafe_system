// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerCabinPricingInput`锛屼綔涓鸿緭鍏ユā鍨嬪苟鎻愪緵 JSON 缂栬В鐮併€?

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

