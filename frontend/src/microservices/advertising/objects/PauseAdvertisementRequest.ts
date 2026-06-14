// 本文件定义 PauseAdvertisementPlanner 使用的暂停请求，字段只覆盖该动作所需内容。

export type PauseAdvertisementRequest = {
  advertisementId: string
  ownerManagerId: string
  ownerType: string
}

export const pauseAdvertisementRequestFromJson = (json: string): PauseAdvertisementRequest =>
  JSON.parse(json) as PauseAdvertisementRequest

export const pauseAdvertisementRequestToJson = (value: PauseAdvertisementRequest): string =>
  JSON.stringify(value)
