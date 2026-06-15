// BlogProfileUserResponse：博客域博客主页用户返回对象。

export type BlogProfileUserResponse = {
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

export const blogProfileUserResponseFromJson = (json: string): BlogProfileUserResponse =>
  JSON.parse(json) as BlogProfileUserResponse

export const blogProfileUserResponseToJson = (value: BlogProfileUserResponse): string =>
  JSON.stringify(value)