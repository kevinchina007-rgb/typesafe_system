// 本文件定义 traveler 模块的 `TravelerBasicInfo`，作为基础传输数据并提供 JSON 编解码。

export type TravelerBasicInfo = {
  fullName: string
  gender: string
  birthDate: string
  nationality: string
}

export const travelerBasicInfoFromJson = (json: string): TravelerBasicInfo =>
  JSON.parse(json) as TravelerBasicInfo

export const travelerBasicInfoToJson = (value: TravelerBasicInfo): string =>
  JSON.stringify(value)
