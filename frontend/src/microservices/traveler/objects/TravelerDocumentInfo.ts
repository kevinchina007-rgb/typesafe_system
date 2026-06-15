// 本文件定义 traveler 模块的 `TravelerDocumentInfo`，作为证件传输数据并提供 JSON 编解码。

export type TravelerDocumentInfo = {
  documentType: string
  documentNumber: string
  documentExpiryDate: string | null
}

export const travelerDocumentInfoFromJson = (json: string): TravelerDocumentInfo =>
  JSON.parse(json) as TravelerDocumentInfo

export const travelerDocumentInfoToJson = (value: TravelerDocumentInfo): string =>
  JSON.stringify(value)
