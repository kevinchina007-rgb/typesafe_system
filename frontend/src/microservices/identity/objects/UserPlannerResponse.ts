// 本文件定义 identity 用户返回对象，对应后端 UserPlannerResponse，只承载用户资料字段并提供 JSON 编解码。
export type UserPlannerResponse = {
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

export const userPlannerResponseFromJson = (json: string): UserPlannerResponse =>
  JSON.parse(json) as UserPlannerResponse

export const userPlannerResponseToJson = (value: UserPlannerResponse): string =>
  JSON.stringify(value)
