// 本文件定义 traveler 模块的 `TravelerPreferenceInfo`，作为偏好信息传输数据并提供 JSON 编解码。

export type TravelerPreferenceInfo = {
  seatPreference: string
  mealPreference: string
  quietSeatPreferred: boolean
}

export const travelerPreferenceInfoFromJson = (json: string): TravelerPreferenceInfo =>
  JSON.parse(json) as TravelerPreferenceInfo

export const travelerPreferenceInfoToJson = (value: TravelerPreferenceInfo): string =>
  JSON.stringify(value)
