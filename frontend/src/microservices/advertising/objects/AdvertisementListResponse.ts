import type { AdvertisementResponse } from './AdvertisementResponse'

export type AdvertisementListResponse = {
  advertisements: AdvertisementResponse[]
}

export const advertisementListResponseFromJson = (json: string): AdvertisementListResponse =>
  JSON.parse(json) as AdvertisementListResponse

export const advertisementListResponseToJson = (value: AdvertisementListResponse): string =>
  JSON.stringify(value)
