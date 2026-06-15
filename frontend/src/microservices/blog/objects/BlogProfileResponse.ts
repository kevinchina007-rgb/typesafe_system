// BlogProfileResponse：博客域博客主页返回对象。

export type BlogProfileResponse = {
  userId: string
  nickname: string
  avatarUrl: string | null
  followerCount: number
  followingCount: number
  receivedLikeCount: number
  isFollowing: boolean
  hideRelations: boolean
  relationListHidden: boolean
}
export const blogProfileResponseFromJson = (json: string): BlogProfileResponse =>
  JSON.parse(json) as BlogProfileResponse

export const blogProfileResponseToJson = (value: BlogProfileResponse): string =>
  JSON.stringify(value)