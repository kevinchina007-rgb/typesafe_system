export type AdvertisementSlotAssignmentRequest = {
  slotIndex: number
}

export const advertisementSlotAssignmentRequestFromJson = (json: string): AdvertisementSlotAssignmentRequest =>
  JSON.parse(json) as AdvertisementSlotAssignmentRequest

export const advertisementSlotAssignmentRequestToJson = (value: AdvertisementSlotAssignmentRequest): string =>
  JSON.stringify(value)
