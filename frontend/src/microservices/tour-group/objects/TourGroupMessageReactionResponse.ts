// 本文件定义 tour-group 模块的 `TourGroupMessageReactionResponse`，作为响应数据并提供 JSON 编解码。

export type TourGroupMessageReactionResponse = {
  reactionType: string
  count: number
  reactedByCurrentUser: boolean
}
export const tourGroupMessageReactionResponseFromJson = (json: string): TourGroupMessageReactionResponse =>
  JSON.parse(json) as TourGroupMessageReactionResponse

export const tourGroupMessageReactionResponseToJson = (value: TourGroupMessageReactionResponse): string =>
  JSON.stringify(value)
