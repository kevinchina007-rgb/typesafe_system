// 本文件定义 train 模块的 `GetTrainDetailsPlannerRequest`，用于按火车编号读取详情并提供 JSON 编解码。

export type GetTrainDetailsPlannerRequest = {
  trainId: string
}
export const getTrainDetailsPlannerRequestFromJson = (json: string): GetTrainDetailsPlannerRequest =>
  JSON.parse(json) as GetTrainDetailsPlannerRequest

export const getTrainDetailsPlannerRequestToJson = (value: GetTrainDetailsPlannerRequest): string =>
  JSON.stringify(value)
