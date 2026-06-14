export type CreateManagerRoomTypePlannerRequest = {
  managerId: string
  roomTypeName: string
  capacity: number
  bedType: string
  nightlyPrice: string
  currency: string
  availableRooms: number
  inventoryStartDate: string
  inventoryEndDate: string
  roomImageUrl?: string | null
}

export const createManagerRoomTypePlannerRequestFromJson = (json: string): CreateManagerRoomTypePlannerRequest =>
  JSON.parse(json) as CreateManagerRoomTypePlannerRequest

export const createManagerRoomTypePlannerRequestToJson = (value: CreateManagerRoomTypePlannerRequest): string =>
  JSON.stringify(value)
