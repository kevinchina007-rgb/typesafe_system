// 本文件定义 advertising 模块的 `AdvertisementSlotAssignmentRequest`，用于广告投放位分配并提供 JSON 编解码。

export type AdvertisementSlotAssignmentRequest = {
  reviewerManagerId: string
  slotIndex: number
}

export const advertisementSlotAssignmentRequestFromJson = (json: string): AdvertisementSlotAssignmentRequest =>
  JSON.parse(json) as AdvertisementSlotAssignmentRequest

export const advertisementSlotAssignmentRequestToJson = (value: AdvertisementSlotAssignmentRequest): string =>
  JSON.stringify(value)
