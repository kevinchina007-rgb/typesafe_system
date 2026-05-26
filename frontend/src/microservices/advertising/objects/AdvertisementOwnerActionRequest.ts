export type AdvertisementOwnerActionRequest = {
  advertisementId: string
  ownerManagerId: string
  ownerType: string
}

export const advertisementOwnerActionRequestFromJson = (json: string): AdvertisementOwnerActionRequest =>
  JSON.parse(json) as AdvertisementOwnerActionRequest

export const advertisementOwnerActionRequestToJson = (value: AdvertisementOwnerActionRequest): string =>
  JSON.stringify(value)
