export type ManagerBatchDecisionResponse = {
  processedCount: number
  orderItemIds: string[]
  action: string
}
export const managerBatchDecisionResponseFromJson = (json: string): ManagerBatchDecisionResponse =>
  JSON.parse(json) as ManagerBatchDecisionResponse

export const managerBatchDecisionResponseToJson = (value: ManagerBatchDecisionResponse): string =>
  JSON.stringify(value)
