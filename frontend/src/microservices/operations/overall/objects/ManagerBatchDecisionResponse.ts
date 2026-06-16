// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerBatchDecisionResponse`锛屼綔涓哄搷搴旀暟鎹苟鎻愪緵 JSON 缂栬В鐮併€?

export type ManagerBatchDecisionResponse = {
  processedCount: number
  orderItemIds: string[]
  action: string
}
export const managerBatchDecisionResponseFromJson = (json: string): ManagerBatchDecisionResponse =>
  JSON.parse(json) as ManagerBatchDecisionResponse

export const managerBatchDecisionResponseToJson = (value: ManagerBatchDecisionResponse): string =>
  JSON.stringify(value)

