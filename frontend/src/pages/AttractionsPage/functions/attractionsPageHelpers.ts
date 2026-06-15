// 本文件定义 AttractionsPage 的辅助函数，负责整理景点卡片、搜索条件和筛选结果。

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AttractionResponse, ReviewPlannerResponse, ResourceReviewSummaryPlannerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { AttractionsSearchRequest } from '../objects'

// 把热词选择拆成城市和关键词两部分。
export function splitAttractionHotSpotSelection(value: string) {
  const [city, ...restParts] = value.split(' ')
  return {
    city,
    keyword: restParts.join(' '),
  }
}

// 把广告里的景点数据映射成景点页的搜索条件。
export function mapAdvertisementAttractionSelection(nextAttraction: AttractionResponse) {
  return {
    searchCity: nextAttraction.city,
    keyword: nextAttraction.attractionName,
  }
}

// 加载景点详情，列表项失败时回退为摘要数据。
export async function loadDetailedAttractions(payload: AttractionsSearchRequest): Promise<AttractionResponse[]> {
  const attractionListResponse = await travelMvpApiClient.listAttractions(payload)
  return Promise.all(
    attractionListResponse.attractions.map(async (attractionSummary: AttractionResponse) => {
      try {
        return await travelMvpApiClient.getAttraction(attractionSummary.attractionId, { useDate: payload.useDate })
      } catch {
        return attractionSummary
      }
    }),
  )
}

// 加载单个景点的评价摘要。
export async function loadAttractionReviewSummary(
  signedInUser: UserResponse | null,
  translate: (translationKey: string) => string,
  payload: { resourceType: string; resourceId: string },
): Promise<ResourceReviewSummaryPlannerResponse> {
  if (!signedInUser) {
    throw new Error(translate('error.loginRequired'))
  }
  return travelMvpApiClient.getReviewResourceSummary({
    userId: signedInUser.userId,
    resourceType: payload.resourceType,
    resourceId: payload.resourceId,
  })
}

// 加载单个景点的评价列表。
export async function loadAttractionReviews(
  signedInUser: UserResponse | null,
  translate: (translationKey: string) => string,
  payload: { resourceType: string; resourceId: string },
): Promise<ReviewPlannerResponse[]> {
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

// 从表单中读取景点订票需要的字段。
export function readAttractionTicketBookingForm(formData: FormData) {
  return {
    sessionId: String(formData.get('sessionId') ?? '').trim() || null,
  }
}
