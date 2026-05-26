export type TourGroupMembershipTravelerResponse = {
  membershipTravelerId: string
  membershipId: string
  travelerId: string
  status: string
  joinedAt: string
}
export const tourGroupMembershipTravelerResponseFromJson = (json: string): TourGroupMembershipTravelerResponse =>
  JSON.parse(json) as TourGroupMembershipTravelerResponse

export const tourGroupMembershipTravelerResponseToJson = (value: TourGroupMembershipTravelerResponse): string =>
  JSON.stringify(value)
