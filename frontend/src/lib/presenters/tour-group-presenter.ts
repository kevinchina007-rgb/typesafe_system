// 本文件把 `tour-group` 服务的群组、会话、成员和消息数据整理成前端展示模型。

import type { AppLanguage, GroupPlanItemResponse, GroupPlanOptionResponse, GroupPlanSelectionResponse, OrderResponse, TourGroupSummaryResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeBookingKind, localizeTourGroupItemType, localizeTourGroupResourceType, localizeTourGroupStatus } from '@/lib/presenters/view-models'

export function formatTravelerChipLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function formatGroupCardSubtitle(group: TourGroupSummaryResponse): string {
  return `${group.destination} · ${group.usedCapacity}/${group.capacity}`
}

export function formatPlanItemSummary(planItem: GroupPlanItemResponse, language: AppLanguage, translate: (key: string) => string): string {
  const scheduleLabel = `${translate('tourGroups.scheduledAt')}: ${formatIsoDateTime(planItem.scheduledAt, '-')}`
  return `${localizeTourGroupItemType(planItem.itemType, language)} · ${scheduleLabel}`
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
    return `${planItem.title} · ${planOption.label}`
  }
  if (planItem) {
    return planItem.title
  }
  return selection.selectionId
}

export function formatBookingSummary(order: OrderResponse, language: AppLanguage, translate: (key: string) => string): string {
  return `${localizeBookingKind(order.orderType, language)} · ${translate('tourGroups.totalPrice')}: ${order.totalPrice} ${order.orderCurrency}`
}

export function getTourGroupConceptLabel(
  concept: 'group' | 'member' | 'traveler' | 'planItem' | 'option' | 'selection',
  language: AppLanguage,
): string {
  void language

  const chineseLabels = {
    group: '旅游团',
    member: '成员',
    traveler: '出行人',
    planItem: '行程项',
    option: '选项',
    selection: '我的选择',
  }

  return chineseLabels[concept]
}
