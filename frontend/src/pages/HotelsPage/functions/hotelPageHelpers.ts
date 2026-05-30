import type { HotelSearchNotice } from '../objects/HotelsPageModels'

export function getLowestRoomPrice(hotelResponses: Array<{ roomTypes: Array<{ basePrice: string }> }>): number | null {
  const prices = hotelResponses.flatMap(hotel => hotel.roomTypes.map(roomType => Number(roomType.basePrice)).filter(Number.isFinite))
  if (prices.length === 0) {
    return null
  }

  return Math.min(...prices)
}

export function validateHotelSearchInput(
  translate: (translationKey: string) => string,
  nextLocation: string,
  nextCheckInDate: string,
  nextCheckOutDate: string,
): HotelSearchNotice | null {
  const normalizedLocation = nextLocation.trim()
  const normalizedCheckInDate = nextCheckInDate.trim()
  const normalizedCheckOutDate = nextCheckOutDate.trim()

  if (!normalizedLocation) {
    return {
      kind: 'error',
      message: translate('hotels.location') + ' ' + translate('error.requiredField'),
    }
  }

  if (!normalizedCheckInDate || !normalizedCheckOutDate) {
    return {
      kind: 'error',
      message: translate('error.friendly.default'),
    }
  }

  if (normalizedCheckOutDate <= normalizedCheckInDate) {
    return {
      kind: 'error',
      message: translate('error.hotelStayDate'),
    }
  }

  return null
}

export function formatHotelSearchRequest(
  location: string,
  checkInDate: string,
  checkOutDate: string,
): { location: string; checkInDate: string; checkOutDate: string } {
  return {
    location: location.trim(),
    checkInDate: checkInDate.trim(),
    checkOutDate: checkOutDate.trim(),
  }
}
