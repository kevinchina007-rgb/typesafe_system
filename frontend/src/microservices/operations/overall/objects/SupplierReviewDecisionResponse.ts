// 本文件定义 `overall` 模块里任务列表使用的供应商审核决策响应对象。

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
