// 本文件定义 RejectAdvertisementPlanner 使用的审核驳回请求，字段只覆盖该动作所需内容。

export type RejectAdvertisementRequest = {
  reviewerManagerId?: string
  reviewNote?: string | null
}

export const rejectAdvertisementRequestFromJson = (json: string): RejectAdvertisementRequest =>
  JSON.parse(json) as RejectAdvertisementRequest

export const rejectAdvertisementRequestToJson = (value: RejectAdvertisementRequest): string =>
  JSON.stringify(value)
