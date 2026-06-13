import type { AppLanguage } from '@/lib/mvp-types/index'
import { getTravelBackendOrigin } from '@/lib/config/runtime-config'

const travelBackendOrigin = getTravelBackendOrigin()

function chooseLabel(language: AppLanguage, fallbackLabel: string, chineseLabel: string): string {
  void language
  void fallbackLabel
  return chineseLabel
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

export function formatIsoDateTime(isoDateTime: string | null, fallbackLabel: string): string {
  if (!isoDateTime) {
    return fallbackLabel
  }
  return new Date(isoDateTime).toLocaleString()
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

export { chooseLabel }
