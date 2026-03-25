import type { AppLanguage, OrderResponse, TravelerResponse, UserResponse } from './mvp-types'

export function deriveTravelerTypeLabelFromBirthDate(
  birthDateValue: string,
  language: AppLanguage,
): string {
  if (!birthDateValue) {
    return language === 'zh' ? '待填写' : 'Pending'
  }

  const todayDate = new Date()
  const parsedBirthDate = new Date(birthDateValue)
  let ageInYears = todayDate.getFullYear() - parsedBirthDate.getFullYear()
  const hasBirthdayPassedThisYear =
    todayDate.getMonth() > parsedBirthDate.getMonth() ||
    (todayDate.getMonth() === parsedBirthDate.getMonth() &&
      todayDate.getDate() >= parsedBirthDate.getDate())

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

export function mapBackendStatusToProductLabel(
  backendStatus: string,
  language: AppLanguage,
): string {
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
  }

  const chineseStatusLabels: Record<string, string> = {
    PendingActivation: '待启用',
    Active: '正常',
    Suspended: '暂停中',
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
  }

  const labelTable = language === 'zh' ? chineseStatusLabels : englishStatusLabels
  return labelTable[backendStatus] ?? backendStatus
}

export function mapTechnicalErrorToFriendlyMessage(
  backendMessage: string,
  language: AppLanguage,
): string {
  const normalizedMessage = backendMessage.toLowerCase()
  if (normalizedMessage.includes('already exists')) {
    return language === 'zh' ? '这个邮箱已经注册过账户了。' : 'This email is already linked to an account.'
  }
  if (normalizedMessage.includes('was not found')) {
    return language === 'zh' ? '没有找到对应的数据，请确认后重试。' : 'We could not find the requested record.'
  }
  if (normalizedMessage.includes('birth date') || normalizedMessage.includes('future')) {
    return language === 'zh' ? '出生日期不合法，请重新检查。' : 'Please check the date of birth and try again.'
  }
  return language === 'zh' ? '操作未成功，请稍后再试。' : 'Something went wrong. Please try again.'
}

export function formatIsoDateTime(
  isoDateTime: string | null,
  fallbackLabel: string,
): string {
  if (!isoDateTime) {
    return fallbackLabel
  }
  return new Date(isoDateTime).toLocaleString()
}

export function localizeDocumentType(documentTypeValue: string, language: AppLanguage): string {
  const optionKey = `travelers.document.${documentTypeValue}`
  const fallbackMap: Record<AppLanguage, Record<string, string>> = {
    en: {
      passport: 'Passport',
      'identity-card': 'ID card',
      'residence-permit': 'Residence permit',
      other: 'Other government document',
      Passport: 'Passport',
      NationalIdentityCard: 'ID card',
      ResidencePermit: 'Residence permit',
      OtherGovernmentDocument: 'Other government document',
    },
    zh: {
      passport: '护照',
      'identity-card': '身份证',
      'residence-permit': '居留许可',
      other: '其他政府证件',
      Passport: '护照',
      NationalIdentityCard: '身份证',
      ResidencePermit: '居留许可',
      OtherGovernmentDocument: '其他政府证件',
    },
  }

  return fallbackMap[language][documentTypeValue] ?? optionKey
}

export function localizeBookingKind(kindValue: string, language: AppLanguage): string {
  const map = language === 'zh'
    ? { hotel: '酒店', flight: '航班', HotelBooking: '酒店订单', FlightBooking: '航班订单', MixedBooking: '混合订单', PendingSelection: '待选择' }
    : { hotel: 'Hotel', flight: 'Flight', HotelBooking: 'Hotel booking', FlightBooking: 'Flight booking', MixedBooking: 'Mixed booking', PendingSelection: 'Pending selection' }
  return map[kindValue as keyof typeof map] ?? kindValue
}

export function localizePaymentMethod(paymentMethodValue: string, language: AppLanguage): string {
  const map = language === 'zh'
    ? { card: '银行卡', 'bank-transfer': '银行转账', wallet: '钱包', Card: '银行卡', BankTransfer: '银行转账', Wallet: '钱包', LoyaltyPoints: '积分' }
    : { card: 'Card', 'bank-transfer': 'Bank transfer', wallet: 'Wallet', Card: 'Card', BankTransfer: 'Bank transfer', Wallet: 'Wallet', LoyaltyPoints: 'Points' }
  return map[paymentMethodValue as keyof typeof map] ?? paymentMethodValue
}

export function buildAccountSummary(user: UserResponse | null) {
  if (!user) {
    return null
  }

  return {
    accountId: user.userId,
    nickname: user.nickname,
    email: user.email,
    phone: user.phone,
    membershipLevel: user.membershipLevel,
    points: user.points,
    status: user.status,
    primaryTravelerId: user.defaultTravelerProfileId,
    createdAt: user.createdAt,
  }
}

export function buildTravelerCards(travelers: TravelerResponse[], language: AppLanguage) {
  return travelers.map(traveler => ({
    travelerId: traveler.travelerId,
    fullName: traveler.fullName,
    primaryDocument: `${localizeDocumentType(traveler.documentType, language)} · ${traveler.documentNumber}`,
    phone: traveler.phone,
    birthDate: traveler.birthDate,
    travelerType: traveler.travelerType,
    status: traveler.status,
    isPrimary: traveler.isDefault,
  }))
}

export function buildBookingSummary(order: OrderResponse | null) {
  if (!order) {
    return null
  }

  return {
    bookingReference: order.orderId,
    bookingType: order.orderType,
    bookingStatus: order.status,
    totalPrice: `${order.totalPrice} ${order.orderCurrency}`,
    remainingRefundableAmount: `${order.remainingRefundableAmount} ${order.orderCurrency}`,
    createdAt: order.createdAt,
    paidAt: order.paidAt,
    confirmedAt: order.confirmedAt,
    completedAt: order.completedAt,
    cancelledAt: order.cancelledAt,
  }
}
