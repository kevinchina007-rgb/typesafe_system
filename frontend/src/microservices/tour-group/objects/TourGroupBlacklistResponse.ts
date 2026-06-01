export type TourGroupBlacklistResponse = {
  blacklistId: string
  groupId: string
  userId: string
  blacklistedByUserId: string
  reason: string
  createdAt: string
}

export const tourGroupBlacklistResponseFromJson = (json: string): TourGroupBlacklistResponse =>
  JSON.parse(json) as TourGroupBlacklistResponse

export const tourGroupBlacklistResponseToJson = (value: TourGroupBlacklistResponse): string =>
  JSON.stringify(value)
