// 本文件定义 ApproveAdvertisementPlanner 使用的审核通过请求，字段只覆盖该动作所需内容。

export type ApproveAdvertisementRequest = {
  reviewerManagerId?: string
  reviewNote?: string | null
}

export const approveAdvertisementRequestFromJson = (json: string): ApproveAdvertisementRequest =>
  JSON.parse(json) as ApproveAdvertisementRequest

export const approveAdvertisementRequestToJson = (value: ApproveAdvertisementRequest): string =>
  JSON.stringify(value)
