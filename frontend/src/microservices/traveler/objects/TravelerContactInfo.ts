// 本文件定义 traveler 模块的 `TravelerContactInfo`，作为联系信息传输数据并提供 JSON 编解码。

export type TravelerContactInfo = {
  phone: string
  email: string | null
}

export const travelerContactInfoFromJson = (json: string): TravelerContactInfo =>
  JSON.parse(json) as TravelerContactInfo

export const travelerContactInfoToJson = (value: TravelerContactInfo): string =>
  JSON.stringify(value)
