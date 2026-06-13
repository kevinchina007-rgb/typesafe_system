import type { AppLanguage } from '@/lib/mvp-types/index'

import { mapBackendStatusToProductLabel } from '@/lib/presenters/shared-presenters'

export function localizeTourGroupStatus(statusValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Draft: '草稿',
          Open: '开放中',
          Closed: '已关闭',
          Cancelled: '已取消',
          Pending: '待处理',
          Active: '已加入',
          Left: '已退出',
          Removed: '已移除',
          Submitted: '待团长确认',
          OrganizerConfirmed: '团长已确认',
          Rejected: '已拒绝',
          ConvertedToOrder: '已转为订单',
        }
      : {
          Draft: 'Draft',
          Open: 'Open',
          Closed: 'Closed',
          Cancelled: 'Cancelled',
          Pending: 'Pending',
          Active: 'Active',
          Left: 'Left',
          Removed: 'Removed',
          Submitted: 'Waiting for organizer review',
          OrganizerConfirmed: 'Confirmed by organizer',
          Rejected: 'Rejected',
          ConvertedToOrder: 'Converted to booking',
        }

  return labels[statusValue as keyof typeof labels] ?? mapBackendStatusToProductLabel(statusValue, language)
}

export function localizeTourGroupItemType(itemTypeValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Flight: '航班',
          Hotel: '酒店',
          Train: '火车',
          Attraction: '景点',
        }
      : {
          Flight: 'Flight',
          Hotel: 'Hotel',
          Train: 'Train',
          Attraction: 'Attraction',
        }

  return labels[itemTypeValue as keyof typeof labels] ?? itemTypeValue
}

export function localizeTourGroupResourceType(resourceTypeValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Flight: '航班资源',
          HotelRoomType: '房型',
          TrainJourneySeat: '火车座位',
          AttractionTicketType: '票型',
        }
      : {
          Flight: 'Flight option',
          HotelRoomType: 'Room type',
          TrainJourneySeat: 'Train seat',
          AttractionTicketType: 'Ticket type',
        }

  return labels[resourceTypeValue as keyof typeof labels] ?? resourceTypeValue
}
