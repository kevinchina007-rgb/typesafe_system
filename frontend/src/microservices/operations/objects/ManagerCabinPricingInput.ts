// 本文件定义 operations 模块的 `ManagerCabinPricingInput`，作为输入模型并提供 JSON 编解码。

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
