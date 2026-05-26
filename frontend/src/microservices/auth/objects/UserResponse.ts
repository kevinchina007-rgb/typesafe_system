export type UserResponse = {
  userId: string
  email: string
  nickname: string
  phone: string
  avatarUrl: string | null
  status: string
  membershipLevel: string
  points: number
  defaultTravelerProfileId: string | null
  createdAt: string
}
export const userResponseFromJson = (json: string): UserResponse =>
  JSON.parse(json) as UserResponse

export const userResponseToJson = (value: UserResponse): string =>
  JSON.stringify(value)
