import { useEffect, useState } from 'react'

import type { PaymentLinkResponse } from '@/lib/mvp-types/index'
import { formatTravelerIdentity, getFlightDetailsPlannerOrderTravelerIds, isFlightOrder, paymentMethodOptions, type PaymentMethodValue } from '@/pages/BookingsPage/functions'
import type { PaymentModalProps } from '@/pages/BookingsPage/objects'
import { SelectionCard } from '@/pages/BookingsPage/components/payment/SelectionCard'

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
  const [paymentLink, setPaymentLink] = useState<PaymentLinkResponse | null>(null)
  const [paymentStep, setPaymentStep] = useState<'selection' | 'qr'>('selection')
  const [isLoadingPaymentLink, setIsLoadingPaymentLink] = useState(false)

  useEffect(() => {
    if (!isOpen) {
      setSelectedPaymentMethod(null)
      setPaymentLink(null)
      setPaymentStep('selection')
      setIsLoadingPaymentLink(false)
    }
  }, [isOpen])

  if (!isOpen || !order) {
    return null
  }

  const activeOrder = order
  const isFlightPaymentOrder = isFlightOrder(order)
  const flightTravelerIds = getFlightDetailsPlannerOrderTravelerIds(order)
  const canConfirmPayment = !!selectedPaymentMethod && (!isFlightPaymentOrder || flightTravelerIds.length > 0)
  const paymentQrLabel = selectedPaymentMethod
    ? paymentMethodOptions.find(option => option.value === selectedPaymentMethod)?.label ?? '支付二维码'
    : '支付二维码'

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
            {isFlightPaymentOrder ? (
              <div className="grid gap-3">
                <p className="m-0 text-base font-medium text-slate-500">本次航班订单已使用下单时选择的出行人</p>
                {flightTravelerIds.length > 0 ? (
                  <div className="flex flex-wrap gap-3 border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-600">
                    {flightTravelerIds.map(travelerId => {
                      const displayName = formatTravelerIdentity(travelers, travelerId)
                      return (
                        <span key={travelerId} className="inline-flex items-center gap-2 border border-slate-300 bg-white px-3 py-2">
                          <span className="inline-flex h-8 w-8 items-center justify-center border border-slate-300 bg-slate-50 text-base font-black text-slate-700">
                            {displayName.slice(0, 1)}
                          </span>
                          <span>{displayName}</span>
                        </span>
                      )
                    })}
                  </div>
                ) : (
                  <p className="m-0 border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-700">
                    该航班订单没有记录出行人，请返回重新下单。
                  </p>
                )}
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
                        travelerIds: isFlightPaymentOrder ? flightTravelerIds : undefined,
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
