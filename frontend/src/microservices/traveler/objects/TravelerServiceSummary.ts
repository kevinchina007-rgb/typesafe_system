// 本文件定义 traveler 模块的 `TravelerServiceSummary`，作为服务摘要传输数据并提供 JSON 编解码。

export type TravelerServiceSummary = {
  age: number | null
  documentLabel: string
  contactLabel: string
  preferenceLabel: string
  requirementLabel: string
  warningLevel: string
}

export const travelerServiceSummaryFromJson = (json: string): TravelerServiceSummary =>
  JSON.parse(json) as TravelerServiceSummary

export const travelerServiceSummaryToJson = (value: TravelerServiceSummary): string =>
  JSON.stringify(value)
