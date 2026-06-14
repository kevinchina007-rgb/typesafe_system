// 本文件定义 PauseAdvertisementDisplayPlanner 使用的暂停展示请求，字段只覆盖该动作所需内容。

export type PauseAdvertisementDisplayRequest = {
  reviewerManagerId?: string
  reviewNote?: string | null
}

export const pauseAdvertisementDisplayRequestFromJson = (json: string): PauseAdvertisementDisplayRequest =>
  JSON.parse(json) as PauseAdvertisementDisplayRequest

export const pauseAdvertisementDisplayRequestToJson = (value: PauseAdvertisementDisplayRequest): string =>
  JSON.stringify(value)
