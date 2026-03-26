import type { AppLanguage } from './mvp-types'

export function deriveTravelerTypeLabelFromBirthDate(birthDateValue: string, language: AppLanguage): string {
  if (!birthDateValue) {
    return language === 'zh' ? '待判断' : 'Pending'
  }

  const todayDate = new Date()
  const parsedBirthDate = new Date(birthDateValue)
  let ageInYears = todayDate.getFullYear() - parsedBirthDate.getFullYear()
  const hasBirthdayPassedThisYear =
    todayDate.getMonth() > parsedBirthDate.getMonth() ||
    (todayDate.getMonth() === parsedBirthDate.getMonth() && todayDate.getDate() >= parsedBirthDate.getDate())

  if (!hasBirthdayPassedThisYear) {
    ageInYears -= 1
  }

  if (ageInYears < 2) {
    return language === 'zh' ? '婴儿' : 'Infant'
  }
  if (ageInYears < 12) {
    return language === 'zh' ? '儿童' : 'Child'
  }
  return language === 'zh' ? '成人' : 'Adult'
}

export function mapBackendStatusToProductLabel(backendStatus: string, language: AppLanguage): string {
  const englishStatusLabels: Record<string, string> = {
    PendingActivation: 'Pending setup',
    Active: 'Active',
    Suspended: 'Paused',
    Closed: 'Closed',
    Draft: 'In progress',
    Verified: 'Ready to travel',
    Archived: 'Archived',
    PendingPayment: 'Waiting for payment',
    Confirmed: 'Booked',
    PartiallyRefunded: 'Partially refunded',
    Refunded: 'Fully refunded',
    Cancelled: 'Cancelled',
    Authorized: 'Authorized',
    Captured: 'Paid',
    Requested: 'Requested',
    Approved: 'Approved',
    Settled: 'Completed',
    Scheduled: 'Scheduled',
    OpenForBooking: 'Open for booking',
    ClosedForBooking: 'Closed for booking',
    SoldOut: 'Sold out',
    Open: 'Available',
    Available: 'Available',
    Inactive: 'Inactive',
  }

  const chineseStatusLabels: Record<string, string> = {
    PendingActivation: '待启用',
    Active: '正常',
    Suspended: '暂停',
    Closed: '已关闭',
    Draft: '进行中',
    Verified: '可出行',
    Archived: '已归档',
    PendingPayment: '待支付',
    Confirmed: '已预订',
    PartiallyRefunded: '部分退款',
    Refunded: '已全额退款',
    Cancelled: '已取消',
    Authorized: '已授权',
    Captured: '已支付',
    Requested: '已申请',
    Approved: '已批准',
    Settled: '已完成',
    Scheduled: '已排班',
    OpenForBooking: '可预订',
    ClosedForBooking: '停止预订',
    SoldOut: '已售罄',
    Open: '可售',
    Available: '可订',
    Inactive: '已停用',
  }

  const labelTable = language === 'zh' ? chineseStatusLabels : englishStatusLabels
  return labelTable[backendStatus] ?? backendStatus
}

export function mapTechnicalErrorToFriendlyMessage(backendMessage: string, language: AppLanguage): string {
  const [errorCode, messageBody] = backendMessage.includes('|') ? backendMessage.split('|', 2) : [backendMessage, backendMessage]
  const normalizedMessage = `${errorCode} ${messageBody}`.toLowerCase()

  if (normalizedMessage.includes('user_email_exists')) {
    return language === 'zh' ? '这个邮箱已经注册过账户了。' : 'This email is already linked to an account.'
  }
  if (normalizedMessage.includes('user_not_found') || normalizedMessage.includes('order_not_found')) {
    return language === 'zh' ? '没有找到对应的数据，请确认后重试。' : 'We could not find the requested record.'
  }
  if (normalizedMessage.includes('flight_not_found')) {
    return language === 'zh' ? '该航班已经不可用了。' : 'That flight is no longer available.'
  }
  if (normalizedMessage.includes('cabin_not_found')) {
    return language === 'zh' ? '该航班不提供这个舱位。' : 'That cabin is not offered on this flight.'
  }
  if (normalizedMessage.includes('cabin_not_bookable')) {
    return language === 'zh' ? '这个舱位当前不可预订。' : 'That cabin is not bookable right now.'
  }
  if (normalizedMessage.includes('hotel_not_found')) {
    return language === 'zh' ? '该酒店当前不可用。' : 'That hotel is no longer available.'
  }
  if (normalizedMessage.includes('room_type_not_found')) {
    return language === 'zh' ? '这个房型当前不可用。' : 'That room type is not available.'
  }
  if (normalizedMessage.includes('room_inventory_not_bookable')) {
    return language === 'zh' ? '所选日期内这个房型暂时不可订。' : 'This room type is not bookable for the selected stay.'
  }
  if (normalizedMessage.includes('stay_period_invalid')) {
    return language === 'zh' ? '请检查入住和离店日期。' : 'Please check your check-in and check-out dates.'
  }
  if (normalizedMessage.includes('room_capacity_exceeded')) {
    return language === 'zh' ? '入住人数超过了当前房间数量可容纳的人数。' : 'The selected guests exceed the room capacity for this booking.'
  }
  if (normalizedMessage.includes('invalid_traveler_selection')) {
    return language === 'zh' ? '请选择你自己名下的有效出行人。' : 'Please choose valid travelers from your own list.'
  }
  if (normalizedMessage.includes('traveler_document_exists')) {
    return language === 'zh' ? '这个证件号已经被其他出行人使用了。' : 'That document number is already used by another traveler.'
  }
  if (normalizedMessage.includes('avatar_missing')) {
    return language === 'zh' ? '请先选择一张头像图片。' : 'Please choose an avatar image first.'
  }
  if (normalizedMessage.includes('avatar_type_invalid')) {
    return language === 'zh' ? '头像只支持 PNG、JPG 或 JPEG 图片。' : 'Avatars only support PNG, JPG, or JPEG images.'
  }
  if (normalizedMessage.includes('avatar_too_large')) {
    return language === 'zh' ? '头像图片不能超过 2MB。' : 'Avatar images must be 2MB or smaller.'
  }
  if (normalizedMessage.includes('avatar_upload_failed')) {
    return language === 'zh' ? '头像上传失败，请稍后再试。' : 'Avatar upload failed. Please try again.'
  }
  if (normalizedMessage.includes('currency_mismatch')) {
    return language === 'zh' ? '订单币种与所选资源价格币种不一致。' : 'The booking currency does not match the selected price.'
  }
  if (normalizedMessage.includes('birth date') || normalizedMessage.includes('future') || normalizedMessage.includes('validation_error')) {
    return language === 'zh' ? '请检查日期和输入内容后再试。' : 'Please check your dates and input values, then try again.'
  }

  return language === 'zh' ? '操作未成功，请稍后再试。' : 'Something went wrong. Please try again.'
}

export function formatIsoDateTime(isoDateTime: string | null, fallbackLabel: string): string {
  if (!isoDateTime) {
    return fallbackLabel
  }
  return new Date(isoDateTime).toLocaleString()
}

export function localizeDocumentType(documentTypeValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          passport: '护照',
          'identity-card': '身份证',
          'residence-permit': '居留许可',
          other: '其他政府证件',
          Passport: '护照',
          NationalIdentityCard: '身份证',
          ResidencePermit: '居留许可',
          OtherGovernmentDocument: '其他政府证件',
        }
      : {
          passport: 'Passport',
          'identity-card': 'ID card',
          'residence-permit': 'Residence permit',
          other: 'Other government document',
          Passport: 'Passport',
          NationalIdentityCard: 'ID card',
          ResidencePermit: 'Residence permit',
          OtherGovernmentDocument: 'Other government document',
        }

  return labels[documentTypeValue as keyof typeof labels] ?? documentTypeValue
}

export function localizeBookingKind(kindValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          hotel: '酒店',
          flight: '航班',
          HotelBooking: '酒店订单',
          FlightBooking: '航班订单',
          MixedBooking: '混合订单',
          PendingSelection: '待选择',
        }
      : {
          hotel: 'Hotel',
          flight: 'Flight',
          HotelBooking: 'Hotel booking',
          FlightBooking: 'Flight booking',
          MixedBooking: 'Mixed booking',
          PendingSelection: 'Pending selection',
        }

  return labels[kindValue as keyof typeof labels] ?? kindValue
}

export function localizePaymentMethod(paymentMethodValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          card: '银行卡',
          'bank-transfer': '银行转账',
          wallet: '钱包',
          Card: '银行卡',
          BankTransfer: '银行转账',
          Wallet: '钱包',
          LoyaltyPoints: '积分',
        }
      : {
          card: 'Card',
          'bank-transfer': 'Bank transfer',
          wallet: 'Wallet',
          Card: 'Card',
          BankTransfer: 'Bank transfer',
          Wallet: 'Wallet',
          LoyaltyPoints: 'Points',
        }

  return labels[paymentMethodValue as keyof typeof labels] ?? paymentMethodValue
}

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
          FAMILY: '家庭床型',
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

export function toBackendAssetUrl(relativeAssetUrl: string): string {
  if (relativeAssetUrl.startsWith('http://') || relativeAssetUrl.startsWith('https://')) {
    return relativeAssetUrl
  }
  return `http://localhost:8080${relativeAssetUrl}`
}
