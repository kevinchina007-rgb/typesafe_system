import type { AppLanguage } from '@/lib/mvp-types/index'
import { mapBackendStatusToProductLabel, formatIsoDateTime } from '@/lib/presenters/shared-presenters'

export function localizeCabinClass(cabinClassValue: string, language: AppLanguage): string {
  const normalizedCabinClass = cabinClassValue.toUpperCase()
  const labels =
    language === 'zh'
      ? {
          ECONOMY: '经济舱',
          PREMIUM_ECONOMY: '超级经济舱',
          BUSINESS: '商务舱',
          FIRST: '头等舱',
        }
      : {
          ECONOMY: 'Economy',
          PREMIUM_ECONOMY: 'Premium economy',
          BUSINESS: 'Business',
          FIRST: 'First',
        }

  return labels[normalizedCabinClass as keyof typeof labels] ?? cabinClassValue
}

export function localizeBedType(bedTypeValue: string, language: AppLanguage): string {
  const normalizedBedType = bedTypeValue.toUpperCase()
  const labels =
    language === 'zh'
      ? {
          SINGLE: '单人床',
          DOUBLE: '双人床',
          TWIN: '双床',
          QUEEN: '大床',
          KING: '特大床',
          FAMILY: '家庭房',
        }
      : {
          SINGLE: 'Single bed',
          DOUBLE: 'Double bed',
          TWIN: 'Twin beds',
          QUEEN: 'Queen bed',
          KING: 'King bed',
          FAMILY: 'Family setup',
        }

  return labels[normalizedBedType as keyof typeof labels] ?? bedTypeValue
}

export function localizeSupplierReviewStatus(supplierReviewStatus: string, language: AppLanguage): string {
  return mapBackendStatusToProductLabel(supplierReviewStatus, language)
}

export function localizeReservationStatus(reservationStatus: string, language: AppLanguage): string {
  return mapBackendStatusToProductLabel(reservationStatus, language)
}

export function localizeTrainSeatClass(seatClassValue: string, language: AppLanguage): string {
  const normalizedSeatClass = seatClassValue.trim().toLowerCase()
  const labels =
    language === 'zh'
      ? {
          'second-class': '二等座',
          'first-class': '一等座',
          business: '商务座',
          sleeper: '卧铺',
          softsleeper: '软卧',
          hardsleeper: '硬卧',
        }
      : {
          'second-class': 'Second class',
          'first-class': 'First class',
          business: 'Business',
          sleeper: 'Sleeper',
          softsleeper: 'Soft sleeper',
          hardsleeper: 'Hard sleeper',
        }

  return labels[normalizedSeatClass as keyof typeof labels] ?? seatClassValue
}

export { formatIsoDateTime }
