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
