// 本文件定义 operations 模块的 `ManagerBatchDecisionResponse`，作为响应数据并提供 JSON 编解码。

export type ManagerBatchDecisionResponse = {
  processedCount: number
  orderItemIds: string[]
  action: string
}
export const managerBatchDecisionResponseFromJson = (json: string): ManagerBatchDecisionResponse =>
  JSON.parse(json) as ManagerBatchDecisionResponse

export const managerBatchDecisionResponseToJson = (value: ManagerBatchDecisionResponse): string =>
  JSON.stringify(value)
