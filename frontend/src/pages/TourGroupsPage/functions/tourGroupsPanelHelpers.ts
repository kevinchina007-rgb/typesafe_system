// 本文件定义 TourGroupsPage 的辅助函数，负责整理团组列表、成员数量和当前选中状态。

import type { GroupPlanItemResponse, GroupPlanOptionResponse, TourGroupDetailsResponse, TourGroupSummaryResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'

export function syncGroupSummary(groups: TourGroupSummaryResponse[], details: TourGroupDetailsResponse): TourGroupSummaryResponse[] {
  const nextGroups = groups.filter(group => group.groupId !== details.group.groupId)
  return [details.group, ...nextGroups].sort((left, right) => right.createdAt.localeCompare(left.createdAt))
}

export function getActiveMembership(selectedGroupDetails: TourGroupDetailsResponse | null, signedInUser: UserResponse | null) {
  if (!selectedGroupDetails || !signedInUser) {
    return null
  }
  return (
    selectedGroupDetails.memberships.find(
      membership => membership.userId === signedInUser.userId && membership.status === 'Active',
    ) ?? null
  )
}

export function getMembershipTravelerIds(selectedGroupDetails: TourGroupDetailsResponse | null, activeMembershipId: string | null | undefined) {
  if (!selectedGroupDetails || !activeMembershipId) {
    return new Set<string>()
  }
  return new Set(
    selectedGroupDetails.membershipTravelers
      .filter(row => row.membershipId === activeMembershipId && row.status === 'Active')
      .map(row => row.travelerId),
  )
}

export function getSelectionDialogOptions(
  selectedGroupDetails: TourGroupDetailsResponse | null,
  selectionPlanItem: GroupPlanItemResponse | null,
) {
  if (!selectionPlanItem || !selectedGroupDetails) {
    return []
  }
  return selectedGroupDetails.planOptions.filter(option => option.planItemId === selectionPlanItem.planItemId)
}

export function getSelectionDialogTravelers(travelers: TravelerResponse[], travelerIds: Set<string>) {
  return travelers.filter(traveler => travelerIds.has(traveler.travelerId))
}

export function pickInitialActivePlanItem(details: TourGroupDetailsResponse, currentPlanItem: GroupPlanItemResponse | null) {
  if (currentPlanItem) {
    return details.planItems.find(planItem => planItem.planItemId === currentPlanItem.planItemId) ?? currentPlanItem
  }
  return [...details.planItems].sort((left, right) => left.sequenceNo - right.sequenceNo)[0] ?? null
}

export function pickCreatedPlanItem(
  details: TourGroupDetailsResponse,
  previousPlanItemIds: Set<string>,
) {
  return (
    details.planItems
      .filter(planItem => !previousPlanItemIds.has(planItem.planItemId))
      .sort((left, right) => left.sequenceNo - right.sequenceNo)
      .at(-1) ?? null
  )
}

export function findNewestSelection(
  createdDetails: TourGroupDetailsResponse,
  planItemId: string,
  optionId: string,
  membershipId: string | null | undefined,
) {
  return [...createdDetails.selections]
    .filter(selection => selection.planItemId === planItemId && selection.optionId === optionId && selection.membershipId === membershipId)
    .sort((left, right) => right.createdAt.localeCompare(left.createdAt))[0]
    ?? null
}

export function getTourGroupOrderCategoryForSelectionOption(option: GroupPlanOptionResponse | null | undefined): 'flightOrders' | 'hotelOrders' | 'trainOrders' | 'attractionOrders' | null {
  if (!option) {
    return null
  }

  switch (option.resourceType) {
    case 'Flight':
      return 'flightOrders'
    case 'HotelRoomType':
      return 'hotelOrders'
    case 'TrainJourneySeat':
      return 'trainOrders'
    case 'AttractionTicketType':
      return 'attractionOrders'
    default:
      return null
  }
}

function normalizeSearchText(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[\s\p{P}\p{S}]+/gu, '')
}

function subsequenceMatchScore(query: string, text: string) {
  if (!query || !text) {
    return 0
  }

  let textIndex = 0
  let matched = 0

  for (const char of query) {
    const nextIndex = text.indexOf(char, textIndex)
    if (nextIndex < 0) {
      continue
    }
    matched += 1
    textIndex = nextIndex + 1
  }

  return matched / query.length
}

function diceCoefficient(query: string, text: string) {
  if (!query || !text) {
    return 0
  }
  if (query.length === 1) {
    return text.includes(query) ? 1 : 0
  }

  const grams = (input: string) => {
    const tokens = new Map<string, number>()
    for (let index = 0; index < input.length - 1; index += 1) {
      const gram = input.slice(index, index + 2)
      tokens.set(gram, (tokens.get(gram) ?? 0) + 1)
    }
    return tokens
  }

  const queryGrams = grams(query)
  const textGrams = grams(text)
  let intersection = 0
  let total = 0

  for (const [, count] of queryGrams) {
    total += count
  }
  for (const [, count] of textGrams) {
    total += count
  }

  for (const [gram, queryCount] of queryGrams) {
    const textCount = textGrams.get(gram) ?? 0
    intersection += Math.min(queryCount, textCount)
  }

  return total === 0 ? 0 : (2 * intersection) / total
}

function scoreField(query: string, value: string, weight: number) {
  const normalizedValue = normalizeSearchText(value)
  if (!normalizedValue) {
    return 0
  }
  if (normalizedValue.includes(query)) {
    return 1000 * weight + query.length * 10
  }

  const subsequenceScore = subsequenceMatchScore(query, normalizedValue)
  const diceScore = diceCoefficient(query, normalizedValue)
  return Math.max(subsequenceScore, diceScore) * 100 * weight
}

export function scoreTourGroupSummaryForQuery(group: TourGroupSummaryResponse, query: string) {
  const normalizedQuery = normalizeSearchText(query)
  if (!normalizedQuery) {
    return 1
  }

  const fields = [
    scoreField(normalizedQuery, group.title, 5),
    scoreField(normalizedQuery, group.description, 3),
    scoreField(normalizedQuery, group.destination, 4),
    scoreField(normalizedQuery, group.tags.join(' '), 4),
    scoreField(normalizedQuery, group.organizerUserId, 0.5),
  ]

  return Math.max(...fields, 0)
}
