// 本文件定义 advertising 模块的 `ListAdvertisementsRequest`，用于列表查询入参并提供 JSON 编解码。

export type ListAdvertisementsRequest = {
  placement?: string | null
  reviewStatus?: string | null
  reviewStatuses?: string[] | null
  ownerManagerId?: string | null
  ownerType?: string | null
  deliverableOnly?: boolean | null
  currentTime?: string | null
}

export const listAdvertisementsRequestFromJson = (json: string): ListAdvertisementsRequest =>
  JSON.parse(json) as ListAdvertisementsRequest

export const listAdvertisementsRequestToJson = (value: ListAdvertisementsRequest): string =>
  JSON.stringify(value)
