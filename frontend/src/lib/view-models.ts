import type { AppLanguage } from './mvp-types'
import { getTravelBackendOrigin } from './runtime-config'

const travelBackendOrigin = getTravelBackendOrigin()

function chooseLabel(language: AppLanguage, englishLabel: string, chineseLabel: string): string {
  return language === 'zh' ? chineseLabel : englishLabel
}

export function deriveTravelerTypeLabelFromBirthDate(birthDateValue: string, language: AppLanguage): string {
  if (!birthDateValue) {
    return chooseLabel(language, 'Pending', '待判断')
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
    return chooseLabel(language, 'Infant', '婴儿')
  }
  if (ageInYears < 12) {
    return chooseLabel(language, 'Child', '儿童')
  }
  return chooseLabel(language, 'Adult', '成人')
}

export function mapBackendStatusToProductLabel(backendStatus: string, language: AppLanguage): string {
  const englishStatusLabels: Record<string, string> = {
    PendingActivation: 'Pending setup',
    Active: 'Active',
    Suspended: 'Paused',
    Closed: 'Closed',
    Draft: 'Draft',
    Verified: 'Ready to travel',
    Archived: 'Archived',
    PendingPayment: 'Pending payment',
    Confirmed: 'Booked',
    PartiallyRefunded: 'Partially refunded',
    Refunded: 'Refunded',
    Cancelled: 'Cancelled',
    Authorized: 'Authorized',
    Captured: 'Paid',
    Requested: 'Requested',
    Approved: 'Approved',
    Settled: 'Settled',
    Scheduled: 'Scheduled',
    OpenForBooking: 'Open for booking',
    ClosedForBooking: 'Closed for booking',
    SoldOut: 'Sold out',
    Open: 'Available',
    Available: 'Available',
    Inactive: 'Inactive',
    NotSubmitted: 'Not submitted',
    PendingSupplierConfirmation: 'Waiting for supplier confirmation',
    SupplierConfirmed: 'Confirmed by supplier',
    SupplierRejected: 'Rejected by supplier',
    Paid: 'Paid',
    Booked: 'Booked',
    PendingRefund: 'Pending refund',
    Expired: 'Expired',
    Released: 'Released',
    OnSale: 'On sale',
    Published: 'Published',
    Completed: 'Completed',
  }

  const chineseStatusLabels: Record<string, string> = {
    PendingActivation: '待启用',
    Active: '正常',
    Suspended: '暂停',
    Closed: '已关闭',
    Draft: '草稿',
    Verified: '可出行',
    Archived: '已归档',
    PendingPayment: '待支付',
    Confirmed: '已预订',
    PartiallyRefunded: '部分退款',
    Refunded: '已退款',
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
    Open: '可用',
    Available: '可预订',
    Inactive: '已停用',
    NotSubmitted: '未提交',
    PendingSupplierConfirmation: '待供应方确认',
    SupplierConfirmed: '供应方已确认',
    SupplierRejected: '供应方已拒绝',
    Paid: '已支付',
    Booked: '已预订',
    PendingRefund: '待退款',
    Expired: '已过期',
    Released: '已释放',
    OnSale: '已开售',
    Published: '已发布',
    Completed: '已完成',
  }

  const labelTable = language === 'zh' ? chineseStatusLabels : englishStatusLabels
  return labelTable[backendStatus] ?? backendStatus
}

export function mapTechnicalErrorToFriendlyMessage(backendMessage: string, language: AppLanguage): string {
  const [errorCode, messageBody] = backendMessage.includes('|') ? backendMessage.split('|', 2) : [backendMessage, backendMessage]
  const normalizedMessage = `${errorCode} ${messageBody}`.toLowerCase()

  if (normalizedMessage.includes('user_email_exists')) {
    return chooseLabel(language, 'This email is already linked to an account.', '这个邮箱已经注册过账户了。')
  }
  if (normalizedMessage.includes('user_not_found') || normalizedMessage.includes('order_not_found')) {
    return chooseLabel(language, 'We could not find the requested record.', '没有找到对应的数据。')
  }
  if (normalizedMessage.includes('flight_not_found')) {
    return chooseLabel(language, 'That flight is no longer available.', '该航班当前不可用。')
  }
  if (normalizedMessage.includes('cabin_not_found')) {
    return chooseLabel(language, 'That cabin is not offered on this flight.', '该航班没有这个舱位。')
  }
  if (normalizedMessage.includes('cabin_not_bookable')) {
    return chooseLabel(language, 'That cabin is not bookable right now.', '这个舱位当前不可预订。')
  }
  if (normalizedMessage.includes('hotel_not_found')) {
    return chooseLabel(language, 'That hotel is no longer available.', '该酒店当前不可用。')
  }
  if (normalizedMessage.includes('room_type_not_found')) {
    return chooseLabel(language, 'That room type is not available.', '这个房型当前不可用。')
  }
  if (normalizedMessage.includes('room_inventory_not_bookable')) {
    return chooseLabel(language, 'This room type is not bookable for the selected stay.', '所选日期内这个房型暂时不可订。')
  }
  if (normalizedMessage.includes('stay_period_invalid')) {
    return chooseLabel(language, 'Please check your check-in and check-out dates.', '请检查入住和离店日期。')
  }
  if (normalizedMessage.includes('room_capacity_exceeded')) {
    return chooseLabel(language, 'The selected guests exceed the room capacity for this booking.', '入住人数超过了当前房间数可容纳的人数。')
  }
  if (normalizedMessage.includes('invalid_traveler_selection')) {
    return chooseLabel(language, 'Please choose valid travelers from your own list.', '请选择你自己名下的有效出行人。')
  }
  if (normalizedMessage.includes('traveler_document_exists')) {
    return chooseLabel(language, 'That document number is already used by another traveler.', '该证件号已经被其他出行人使用。')
  }
  if (normalizedMessage.includes('avatar_missing')) {
    return chooseLabel(language, 'Please choose an avatar image first.', '请先选择一张图片。')
  }
  if (normalizedMessage.includes('avatar_type_invalid')) {
    return chooseLabel(language, 'Avatars only support PNG, JPG, or JPEG images.', '头像只支持 PNG、JPG 或 JPEG。')
  }
  if (normalizedMessage.includes('avatar_too_large')) {
    return chooseLabel(language, 'Avatar images must be 2MB or smaller.', '头像图片不能超过 2MB。')
  }
  if (normalizedMessage.includes('avatar_upload_failed')) {
    return chooseLabel(language, 'Avatar upload failed. Please try again.', '头像上传失败，请稍后再试。')
  }
  if (normalizedMessage.includes('manager_not_found')) {
    return chooseLabel(language, 'We could not find that manager account.', '没有找到这个管理者账号。')
  }
  if (normalizedMessage.includes('manager_scope_mismatch')) {
    return chooseLabel(language, 'You can only act on items inside your own manager scope.', '你只能处理自己管理范围内的任务。')
  }
  if (normalizedMessage.includes('manager_email_exists')) {
    return chooseLabel(language, 'This manager email is already in use.', '这个管理者邮箱已经被使用了。')
  }
  if (normalizedMessage.includes('decision_reason_required')) {
    return chooseLabel(language, 'A reject action requires a reason.', '拒绝时必须填写原因。')
  }
  if (normalizedMessage.includes('order_item_not_actionable')) {
    return chooseLabel(language, 'This booking item cannot be reviewed right now.', '这个订单条目当前不能再处理。')
  }
  if (normalizedMessage.includes('currency_mismatch')) {
    return chooseLabel(language, 'The booking currency does not match the selected price.', '订单币种与所选价格币种不一致。')
  }
  if (normalizedMessage.includes('payment_already_completed')) {
    return chooseLabel(language, 'This order has already been paid.', '这个订单已经支付成功，不能重复支付。')
  }
  if (normalizedMessage.includes('inventory_not_available')) {
    return chooseLabel(language, 'The selected inventory is no longer available.', '当前库存已经不足，请重新选择。')
  }
  if (normalizedMessage.includes('reservation_expired')) {
    return chooseLabel(language, 'This reservation has expired. Please book again.', '当前锁定已超时，请重新下单。')
  }
  if (normalizedMessage.includes('traveler_not_eligible')) {
    return chooseLabel(language, 'One or more travelers do not meet this ticket type eligibility.', '一个或多个出行人不符合该票型的购票条件。')
  }
  if (normalizedMessage.includes('ticket_type_inactive')) {
    return chooseLabel(language, 'This ticket type is not available right now.', '这个票型当前不可用。')
  }
  if (normalizedMessage.includes('attraction_not_found')) {
    return chooseLabel(language, 'That attraction is no longer available.', '该景点当前不可用。')
  }
  if (normalizedMessage.includes('train_not_found')) {
    return chooseLabel(language, 'That train service is no longer available.', '该车次当前不可用。')
  }
  if (normalizedMessage.includes('train_not_on_sale')) {
    return chooseLabel(language, 'This train service is not on sale yet.', '该车次还未开售。')
  }
  if (normalizedMessage.includes('train_station_invalid') || normalizedMessage.includes('train_station_order_invalid')) {
    return chooseLabel(language, 'Please choose a valid station range on the same train.', '请选择同一车次上的有效区间。')
  }
  if (normalizedMessage.includes('train_inventory_not_available')) {
    return chooseLabel(language, 'The selected train seats are no longer available.', '当前车票库存不足，请重新选择。')
  }
  if (normalizedMessage.includes('birth date') || normalizedMessage.includes('future') || normalizedMessage.includes('validation_error')) {
    return chooseLabel(language, 'Please check your dates and input values, then try again.', '请检查日期和输入内容后再试。')
  }

  return chooseLabel(language, 'Something went wrong. Please try again.', '操作未成功，请稍后再试。')
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
          other: '其他证件',
          Passport: '护照',
          NationalIdentityCard: '身份证',
          ResidencePermit: '居留许可',
          OtherGovernmentDocument: '其他证件',
        }
      : {
          passport: 'Passport',
          'identity-card': 'ID card',
          'residence-permit': 'Residence permit',
          other: 'Other document',
          Passport: 'Passport',
          NationalIdentityCard: 'ID card',
          ResidencePermit: 'Residence permit',
          OtherGovernmentDocument: 'Other document',
        }

  return labels[documentTypeValue as keyof typeof labels] ?? documentTypeValue
}

export function localizeBookingKind(kindValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          hotel: '酒店',
          flight: '航班',
          train: '火车票',
          attraction: '景点门票',
          HotelBooking: '酒店订单',
          FlightBooking: '航班订单',
          TrainBooking: '火车票订单',
          AttractionBooking: '景点门票订单',
          MixedBooking: '混合订单',
          PendingSelection: '待选择',
        }
      : {
          hotel: 'Hotel',
          flight: 'Flight',
          train: 'Train',
          attraction: 'Attraction',
          HotelBooking: 'Hotel booking',
          FlightBooking: 'Flight booking',
          TrainBooking: 'Train booking',
          AttractionBooking: 'Attraction booking',
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
          alipay: '支付宝',
          'wechat-pay': '微信支付',
          'nailong-pay': '奶龙支付',
        }
      : {
          card: 'Card',
          'bank-transfer': 'Bank transfer',
          wallet: 'Wallet',
          Card: 'Card',
          BankTransfer: 'Bank transfer',
          Wallet: 'Wallet',
          LoyaltyPoints: 'Points',
          alipay: 'Alipay',
          'wechat-pay': 'WeChat Pay',
          'nailong-pay': 'NaiLong Pay',
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

export function localizeManagerTaskType(taskTypeValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Airline: '航班任务',
          Hotel: '酒店任务',
          Attraction: '景点任务',
          airline: '航班任务',
          hotel: '酒店任务',
          attraction: '景点任务',
        }
      : {
          Airline: 'Flight task',
          Hotel: 'Hotel task',
          Attraction: 'Attraction task',
          airline: 'Flight task',
          hotel: 'Hotel task',
          attraction: 'Attraction task',
        }

  return labels[taskTypeValue as keyof typeof labels] ?? taskTypeValue
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

export function toBackendAssetUrl(relativeAssetUrl: string): string {
  if (relativeAssetUrl.startsWith('http://') || relativeAssetUrl.startsWith('https://')) {
    return relativeAssetUrl
  }
  return `${travelBackendOrigin}${relativeAssetUrl}`
}
