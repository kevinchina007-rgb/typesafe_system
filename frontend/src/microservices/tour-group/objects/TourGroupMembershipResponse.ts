export type TourGroupMembershipResponse = {
  membershipId: string
  userId: string
  userDisplayName?: string
  status: string
  joinedAt: string
}
export const tourGroupMembershipResponseFromJson = (json: string): TourGroupMembershipResponse =>
  JSON.parse(json) as TourGroupMembershipResponse

export const tourGroupMembershipResponseToJson = (value: TourGroupMembershipResponse): string =>
  JSON.stringify(value)
