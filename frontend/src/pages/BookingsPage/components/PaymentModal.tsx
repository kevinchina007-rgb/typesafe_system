import { useEffect, useState } from 'react'

import type { OrderResponse, PaymentLinkResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatTravelerIdentity } from '@/pages/BookingsPage/components/orderViewModel'

type PaymentMethodValue = 'alipay' | 'wechat-pay' | 'nailong-pay'

type PaymentModalProps = {
  isOpen: boolean
  order: OrderResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  onClose: () => void
  onCreatePaymentLink: (payload: { orderId: string; paymentMethod: PaymentMethodValue }) => Promise<PaymentLinkResponse>
  onConfirmPayment: (payload: { orderId: string; paymentMethod: PaymentMethodValue; travelerIds?: string[] }) => Promise<void>
}

const paymentMethodOptions: Array<{ value: PaymentMethodValue; label: string }> = [
  { value: 'alipay', label: '支付宝' },
  { value: 'wechat-pay', label: '微信支付' },
  { value: 'nailong-pay', label: '奶龙支付' },
]

export function PaymentModal({
  isOpen,
  order,
  travelers,
  isBusy,
  onClose,
  onCreatePaymentLink,
  onConfirmPayment,
}: PaymentModalProps) {
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethodValue | null>(null)
  const [selectedTravelerId, setSelectedTravelerId] = useState<string>('')
  const [paymentLink, setPaymentLink] = useState<PaymentLinkResponse | null>(null)
  const [paymentStep, setPaymentStep] = useState<'selection' | 'qr'>('selection')
  const [isLoadingPaymentLink, setIsLoadingPaymentLink] = useState(false)

  useEffect(() => {
    if (!isOpen) {
      setSelectedPaymentMethod(null)
      setSelectedTravelerId('')
      setPaymentLink(null)
      setPaymentStep('selection')
      setIsLoadingPaymentLink(false)
    }
  }, [isOpen])

  if (!isOpen || !order) {
    return null
  }

  const activeOrder = order
  const flightTravelerIds = getFlightOrderTravelerIds(order)
  const needsTravelerSelection = isFlightOrder(order) && !isOrderPaid(order.status)
  const selectedPaymentTravelerId = needsTravelerSelection ? selectedTravelerId : flightTravelerIds[0] ?? ''
  const canConfirmPayment = !!selectedPaymentMethod && (!needsTravelerSelection || !!selectedTravelerId)
  const paymentQrLabel = selectedPaymentMethod ? paymentMethodOptions.find(option => option.value === selectedPaymentMethod)?.label ?? '支付二维码' : '支付二维码'

  async function showPaymentQr() {
    if (!selectedPaymentMethod || !canConfirmPayment) {
      return
    }

    setIsLoadingPaymentLink(true)
    try {
      const response = await onCreatePaymentLink({ orderId: activeOrder.orderId, paymentMethod: selectedPaymentMethod })
      setPaymentLink(response)
      setPaymentStep('qr')
    } finally {
      setIsLoadingPaymentLink(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6" role="presentation" onClick={onClose}>
      <div
        className="grid max-h-[90vh] w-full max-w-3xl gap-6 overflow-auto border border-slate-200 bg-white p-8 text-slate-950 shadow-2xl shadow-slate-950/20"
        role="dialog"
        aria-modal="true"
        aria-label="确认支付"
        onClick={event => event.stopPropagation()}
      >
        <strong className="text-4xl font-black tracking-normal text-slate-950">{`${order.totalPrice} ${order.orderCurrency}`}</strong>

        {paymentStep === 'selection' ? (
          <>
            {needsTravelerSelection ? (
              <div className="grid gap-3">
                <p className="m-0 text-base font-medium text-slate-500">选择本次出行人</p>
                {travelers.length > 0 ? (
                  <div className="grid gap-3">
                    {travelers.map(traveler => (
                      <SelectionCard
                        key={traveler.travelerId}
                        name="paymentTravelerId"
                        value={traveler.travelerId}
                        checked={selectedTravelerId === traveler.travelerId}
                        label={formatTravelerIdentity(travelers, traveler.travelerId)}
                        marker={traveler.fullName.slice(0, 1)}
                        onChange={() => setSelectedTravelerId(traveler.travelerId)}
                      />
                    ))}
                  </div>
                ) : (
                  <p className="m-0 border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-500">当前账号还没有出行人，请先到账号页添加出行人。</p>
                )}
              </div>
            ) : selectedPaymentTravelerId ? (
              <div className="flex items-center gap-3 border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-600">
                <span className="inline-flex h-10 w-10 items-center justify-center border border-slate-300 bg-white text-base font-black text-slate-700">{formatTravelerIdentity(travelers, selectedPaymentTravelerId).slice(0, 1)}</span>
                <span>{formatTravelerIdentity(travelers, selectedPaymentTravelerId)}</span>
              </div>
            ) : null}

            <div className="grid gap-3">
              <p className="m-0 text-base font-medium text-slate-500">选择支付方式</p>
              <div className="grid gap-3 md:grid-cols-3">
                {paymentMethodOptions.map(option => (
                  <SelectionCard
                    key={option.value}
                    name="paymentMethod"
                    value={option.value}
                    checked={selectedPaymentMethod === option.value}
                    label={option.label}
                    marker={option.label.slice(0, 1)}
                    onChange={() => setSelectedPaymentMethod(option.value)}
                  />
                ))}
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-end gap-3 border-t border-slate-200 pt-5">
              <button
                type="button"
                className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-6 py-2 text-base font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                disabled={isBusy || isLoadingPaymentLink}
                onClick={onClose}
              >
                取消支付
              </button>
              <button
                className="inline-flex min-h-12 items-center justify-center bg-pink-500 px-7 py-2 text-base font-black text-white shadow-none transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:bg-slate-300"
                type="button"
                disabled={isBusy || isLoadingPaymentLink || !canConfirmPayment}
                onClick={() => void showPaymentQr()}
              >
                {isLoadingPaymentLink ? '生成二维码中' : '确认支付'}
              </button>
            </div>
          </>
        ) : (
          <>
            <div className="grid place-items-center gap-4 border border-slate-200 bg-slate-50 p-6 text-center">
              <strong className="text-xl font-black text-slate-950">{paymentQrLabel}</strong>
              {paymentLink ? (
                <>
                  <div className="border border-slate-200 bg-white p-4">
                    <img
                      src={`https://api.qrserver.com/v1/create-qr-code/?size=320x320&margin=24&data=${encodeURIComponent(paymentLink.paymentUrl)}`}
                      alt="支付二维码"
                      className="h-64 w-64 object-contain"
                    />
                  </div>
                  <p className="m-0 text-sm font-medium text-slate-500">请扫码完成支付，完成后点击下方按钮确认。</p>
                  <a className="text-sm font-semibold text-blue-600 hover:text-blue-800" href={paymentLink.paymentUrl} target="_blank" rel="noreferrer">
                    打开支付链接
                  </a>
                  <small className="text-xs font-medium text-slate-400">有效期至 {paymentLink.expiresAt}</small>
                </>
              ) : (
                <p className="m-0 text-sm font-medium text-slate-500">二维码暂时没有生成出来，请返回重新选择支付方式。</p>
              )}
            </div>

            <div className="flex flex-wrap items-center justify-end gap-3 border-t border-slate-200 pt-5">
              <button
                type="button"
                className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-6 py-2 text-base font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                disabled={isBusy}
                onClick={() => {
                  setPaymentStep('selection')
                  setPaymentLink(null)
                }}
              >
                返回修改
              </button>
              <button
                type="button"
                className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-6 py-2 text-base font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                disabled={isBusy}
                onClick={onClose}
              >
                取消支付
              </button>
              <button
                className="inline-flex min-h-12 items-center justify-center bg-pink-500 px-7 py-2 text-base font-black text-white shadow-none transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:bg-slate-300"
                type="button"
                disabled={isBusy || !paymentLink || !selectedPaymentMethod}
                onClick={() =>
                  void (selectedPaymentMethod
                    ? onConfirmPayment({
                        orderId: order.orderId,
                        paymentMethod: selectedPaymentMethod,
                        travelerIds: selectedPaymentTravelerId ? [selectedPaymentTravelerId] : undefined,
                      })
                    : Promise.resolve())
                }
              >
                我已完成支付
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}

function SelectionCard({
  name,
  value,
  checked,
  label,
  marker,
  onChange,
}: {
  name: string
  value: string
  checked: boolean
  label: string
  marker: string
  onChange: () => void
}) {
  return (
    <label
      className={`flex cursor-pointer items-center gap-4 border px-4 py-4 text-base font-semibold transition ${
        checked ? 'border-sky-500 bg-sky-50 text-slate-950' : 'border-slate-200 bg-white text-slate-600 hover:border-slate-400'
      }`}
    >
      <input type="radio" name={name} value={value} checked={checked} onChange={onChange} />
      <span className="inline-flex h-11 w-11 items-center justify-center border border-slate-300 bg-slate-50 text-lg font-black text-slate-700">{marker}</span>
      <span className="truncate">{label}</span>
    </label>
  )
}

function isFlightOrder(order: OrderResponse) {
  return order.orderType.toLowerCase().includes('flight') || (order.orderLineItems ?? []).some(orderLineItem => orderLineItem.flightDetails || parseFlightSnapshot(orderLineItem.summaryLabel))
}

function isOrderPaid(status: string) {
  return status === 'Confirmed' || status === 'Paid' || status === 'Booked'
}

function getFlightOrderTravelerIds(order: OrderResponse) {
  const flightItem = (order.orderLineItems ?? []).find(orderLineItem => orderLineItem.flightDetails || parseFlightSnapshot(orderLineItem.summaryLabel))
  if (!flightItem) {
    return []
  }
  return flightItem.flightDetails?.travelerIds ?? parseFlightSnapshot(flightItem.summaryLabel)?.travelerIds ?? []
}

function parseFlightSnapshot(summaryLabel: string): { travelerIds: string[] } | null {
  if (!summaryLabel.trim().startsWith('{')) {
    return null
  }

  try {
    const parsed = JSON.parse(summaryLabel) as { travelerIds?: unknown }
    return {
      travelerIds: Array.isArray(parsed.travelerIds) ? parsed.travelerIds.filter((value): value is string => typeof value === 'string' && value.trim().length > 0) : [],
    }
  } catch {
    return null
  }
}
