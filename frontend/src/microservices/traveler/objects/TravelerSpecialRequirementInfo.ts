// 本文件定义 traveler 模块的 `TravelerSpecialRequirementInfo`，作为特殊需求传输数据并提供 JSON 编解码。

export type TravelerSpecialRequirementInfo = {
  assistanceType: string
  requirementNote: string | null
  hasLargeLuggage: boolean
  luggageNote: string | null
}

export const travelerSpecialRequirementInfoFromJson = (json: string): TravelerSpecialRequirementInfo =>
  JSON.parse(json) as TravelerSpecialRequirementInfo

export const travelerSpecialRequirementInfoToJson = (value: TravelerSpecialRequirementInfo): string =>
  JSON.stringify(value)
