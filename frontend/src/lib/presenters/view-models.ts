import type { AppLanguage } from '@/lib/mvp-types/index'
import { getTravelBackendOrigin } from '@/lib/config/runtime-config'

const travelBackendOrigin = getTravelBackendOrigin()

// 这里集中收口“核心值 -> 展示值”的转换。
// 后端状态、技术错误、资源相对路径都会先过这一层，再进入页面组件。
function chooseLabel(language: AppLanguage, fallbackLabel: string, chineseLabel: string): string {
  void language
  void fallbackLabel
  return chineseLabel
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
  void language

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

  return chineseStatusLabels[backendStatus] ?? backendStatus
}

export function mapTechnicalErrorToFriendlyMessage(backendMessage: string, language: AppLanguage): string {
  const normalizedMessage = backendMessage.toLowerCase()

  if (normalizedMessage.includes('password cannot be empty') || normalizedMessage.includes('passwordwasempty')) {
    return chooseLabel(language, 'Password is required.', '密码还没填呢，小猪没法帮你悄悄猜。')
  }
  if (normalizedMessage.includes('password must be at least 10') || normalizedMessage.includes('passwordwastooshort')) {
    return chooseLabel(language, 'Password must be at least 10 characters.', '密码至少需要 10 位，再给它加一点长度吧。')
  }
  if (normalizedMessage.includes('password must include letters and numbers') || normalizedMessage.includes('passwordwastooweak')) {
    return chooseLabel(language, 'Password is too weak.', '这个密码太好猜啦，请同时包含字母和数字，并避开 1234567890、qwerty123 这类弱密码。')
  }
  if (normalizedMessage.includes('invalid password') || normalizedMessage.includes('password did not match')) {
    return chooseLabel(language, 'The password is not correct.', '密码不对，再检查一下输入吧。')
  }
  if (
    normalizedMessage.includes('user credential') &&
    normalizedMessage.includes('was not found')
  ) {
    return chooseLabel(language, 'This account does not exist.', '没有找到这个用户账号，先确认邮箱有没有写错吧。')
  }
  if (
    normalizedMessage.includes('credential') &&
    normalizedMessage.includes('was not found') &&
    (normalizedMessage.includes('airline') ||
      normalizedMessage.includes('hotel') ||
      normalizedMessage.includes('train') ||
      normalizedMessage.includes('attraction') ||
      normalizedMessage.includes('siteadmin') ||
      normalizedMessage.includes('site admin'))
  ) {
    return chooseLabel(language, 'This manager account does not exist.', '没有找到这个管理者账号，先确认邮箱和管理身份有没有选对。')
  }
  if (normalizedMessage.includes('email') && (normalizedMessage.includes('invalid') || normalizedMessage.includes('format'))) {
    return chooseLabel(language, 'Please check the email address.', '邮箱格式看起来不太对，请检查一下 @ 和后缀。')
  }
  if (normalizedMessage.includes('failed to fetch') || normalizedMessage.includes('networkerror')) {
    return chooseLabel(language, 'The backend is not reachable.', '后端好像没连上，请确认服务已经启动。')
  }
  if (
    normalizedMessage.includes('org.h2.driver') ||
    normalizedMessage.includes('org.postgresql.driver') ||
    normalizedMessage.includes('jdbc') ||
    normalizedMessage.includes('connection refused')
  ) {
    return chooseLabel(language, 'The database is not ready.', '数据库还没接好，请确认后端正在连接 PostgreSQL。')
  }
  if (normalizedMessage.includes('http 404') || normalizedMessage.includes('unknown planner')) {
    return chooseLabel(language, 'The API endpoint was not found.', '没有找到对应接口，前后端地址可能没对上。')
  }
  if (
    normalizedMessage.includes('user_email_exists') ||
    (normalizedMessage.includes('user credential') && normalizedMessage.includes('already exists'))
  ) {
    return chooseLabel(language, 'This email is already linked to an account.', '这个邮箱已经注册过账户了，换一个邮箱试试吧。')
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
    return chooseLabel(language, 'We could not find that manager account.', '没有找到这个管理者账户。')
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

  return chooseLabel(language, 'Something went wrong. Please try again.', '未知错误。小猪还没定位到摔在哪一步。')
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
          Flight: '航班订单',
          Hotel: '酒店订单',
          Train: '火车票订单',
          Attraction: '景点门票订单',
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

export function localizeAccountStatus(statusValue: string, language: AppLanguage): string {
  return mapBackendStatusToProductLabel(statusValue, language)
}

export function localizeMembershipLevel(membershipLevelValue: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Standard: '普通会员',
          Silver: '白银会员',
          Gold: '黄金会员',
          Platinum: '白金会员',
        }
      : {
          Standard: 'Standard',
          Silver: 'Silver',
          Gold: 'Gold',
          Platinum: 'Platinum',
        }

  return labels[membershipLevelValue as keyof typeof labels] ?? membershipLevelValue
}

export function formatTravelerReference(
  traveler: { fullName: string; documentNumber: string } | null | undefined,
  fallbackLabel = '-',
): string {
  if (!traveler) {
    return fallbackLabel
  }

  const documentTail = traveler.documentNumber.trim().slice(-4)
  return documentTail ? `${traveler.fullName} (${documentTail})` : traveler.fullName
}

export function toBackendAssetUrl(relativeAssetUrl: string | null | undefined): string {
  // 数据库存的是相对 publicUrl；前端在这里补齐 backend origin，得到真正的访问地址。
  // 同时兼容完整 URL、/uploads/...、旧版 /api/assets/... 以及 uploads/... 这几类输入。
  if (!relativeAssetUrl) {
    return ''
  }

  const normalizedAssetUrl = relativeAssetUrl.trim()
  if (!normalizedAssetUrl) {
    return ''
  }

  if (
    normalizedAssetUrl.startsWith('http://') ||
    normalizedAssetUrl.startsWith('https://') ||
    normalizedAssetUrl.startsWith('data:image/')
  ) {
    return normalizedAssetUrl
  }

  if (normalizedAssetUrl.startsWith('/images/') || normalizedAssetUrl.startsWith('images/')) {
    return normalizedAssetUrl.startsWith('/') ? normalizedAssetUrl : `/${normalizedAssetUrl}`
  }

  const normalizedPath = normalizedAssetUrl.startsWith('/api/assets/')
    ? normalizedAssetUrl.replace('/api/assets/', '/uploads/assets/')
    : normalizedAssetUrl.startsWith('/')
      ? normalizedAssetUrl
      : `/${normalizedAssetUrl}`
  return `${travelBackendOrigin}${encodeURI(normalizedPath)}`
}
