import type { AppLanguage, GroupPlanItemResponse, GroupPlanOptionResponse, GroupPlanSelectionResponse, OrderResponse, TourGroupSummaryResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeBookingKind, localizeTourGroupItemType, localizeTourGroupResourceType, localizeTourGroupStatus } from '@/lib/presenters/view-models'

export function formatTravelerChipLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function formatGroupCardSubtitle(group: TourGroupSummaryResponse): string {
  return `${group.destination} 路 ${group.usedCapacity}/${group.capacity}`
}

export function formatPlanItemSummary(planItem: GroupPlanItemResponse, language: AppLanguage, translate: (key: string) => string): string {
  const scheduleLabel = `${translate('tourGroups.scheduledAt')}: ${formatIsoDateTime(planItem.scheduledAt, '-')}`
  return `${localizeTourGroupItemType(planItem.itemType, language)} 路 ${scheduleLabel}`
}

export function formatPlanOptionSummary(planOption: GroupPlanOptionResponse, language: AppLanguage): string {
  return localizeTourGroupResourceType(planOption.resourceType, language)
}

export function formatSelectionStatusLabel(selection: GroupPlanSelectionResponse, language: AppLanguage): string {
  return localizeTourGroupStatus(selection.status, language)
}

export function formatSelectionTitle(
  selection: GroupPlanSelectionResponse,
  planItem: GroupPlanItemResponse | undefined,
  planOption: GroupPlanOptionResponse | undefined,
): string {
  if (planItem && planOption) {
    return `${planItem.title} 路 ${planOption.label}`
  }
  if (planItem) {
    return planItem.title
  }
  return selection.selectionId
}

export function formatBookingSummary(order: OrderResponse, language: AppLanguage, translate: (key: string) => string): string {
  return `${localizeBookingKind(order.orderType, language)} 路 ${translate('tourGroups.totalPrice')}: ${order.totalPrice} ${order.orderCurrency}`
}

export function getTourGroupConceptLabel(
  concept: 'group' | 'member' | 'traveler' | 'planItem' | 'option' | 'selection',
  language: AppLanguage,
): string {
  void language

  const chineseLabels = {
    group: '???',
    member: '鎴愬憳',
    traveler: '?????',
    planItem: '???',
    option: '????',
    selection: '鎴戠殑閫夋嫨',
  }

  return chineseLabels[concept]
}
