export type TourGroupMessageReactionResponse = {
  reactionType: string
  count: number
  reactedByCurrentUser: boolean
}
export const tourGroupMessageReactionResponseFromJson = (json: string): TourGroupMessageReactionResponse =>
  JSON.parse(json) as TourGroupMessageReactionResponse

export const tourGroupMessageReactionResponseToJson = (value: TourGroupMessageReactionResponse): string =>
  JSON.stringify(value)
