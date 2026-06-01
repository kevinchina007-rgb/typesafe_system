import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AttractionResponse, ReviewResponse, ResourceReviewSummaryResponse, UserResponse } from '@/lib/mvp-types/index'
import type { AttractionsSearchRequest } from '../objects'

export function splitAttractionHotSpotSelection(value: string) {
  const [city, ...restParts] = value.split(' ')
  return {
    city,
    keyword: restParts.join(' '),
  }
}

export function mapAdvertisementAttractionSelection(nextAttraction: AttractionResponse) {
  return {
    searchCity: nextAttraction.city,
    keyword: nextAttraction.attractionName,
  }
}

export async function loadDetailedAttractions(payload: AttractionsSearchRequest): Promise<AttractionResponse[]> {
  const attractionListResponse = await travelMvpApiClient.listAttractions(payload)
  return Promise.all(
    attractionListResponse.attractions.map(async attractionSummary => {
      try {
        return await travelMvpApiClient.getAttraction(attractionSummary.attractionId, { useDate: payload.useDate })
      } catch {
        return attractionSummary
      }
    }),
  )
}

export async function loadAttractionReviewSummary(
  signedInUser: UserResponse | null,
  translate: (translationKey: string) => string,
  payload: { resourceType: string; resourceId: string },
): Promise<ResourceReviewSummaryResponse> {
  if (!signedInUser) {
    throw new Error(translate('error.loginRequired'))
  }
  return travelMvpApiClient.getReviewResourceSummary({
    userId: signedInUser.userId,
    resourceType: payload.resourceType,
    resourceId: payload.resourceId,
  })
}

export async function loadAttractionReviews(
  signedInUser: UserResponse | null,
  translate: (translationKey: string) => string,
  payload: { resourceType: string; resourceId: string },
): Promise<ReviewResponse[]> {
  if (!signedInUser) {
    throw new Error(translate('error.loginRequired'))
  }
  const response = await travelMvpApiClient.listReviewsByResource({
    userId: signedInUser.userId,
    resourceType: payload.resourceType,
    resourceId: payload.resourceId,
  })
  return response.reviews
}

export function readAttractionTicketBookingForm(formData: FormData) {
  return {
    sessionId: String(formData.get('sessionId') ?? '').trim() || null,
  }
}
