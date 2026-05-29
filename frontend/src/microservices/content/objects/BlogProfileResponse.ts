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

export type BlogProfileUserListResponse = {
  users: BlogProfileUserResponse[]
}
