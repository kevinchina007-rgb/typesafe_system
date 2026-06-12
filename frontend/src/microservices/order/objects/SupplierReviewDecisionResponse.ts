// 本文件定义 order 模块的 `SupplierReviewDecisionResponse`，作为响应数据并提供 JSON 编解码。

export type SupplierReviewDecisionResponse = {
  decision: string
  reason: string | null
  decidedAt: string
  managerId: string
}
export const supplierReviewDecisionResponseFromJson = (json: string): SupplierReviewDecisionResponse =>
  JSON.parse(json) as SupplierReviewDecisionResponse

export const supplierReviewDecisionResponseToJson = (value: SupplierReviewDecisionResponse): string =>
  JSON.stringify(value)
