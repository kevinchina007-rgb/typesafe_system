import { useEffect, useState } from 'react'

import type { OrderResponse } from '../lib/mvp-types'

type PaymentMethodValue = 'alipay' | 'wechat-pay' | 'nailong-pay'

type PaymentModalProps = {
  isOpen: boolean
  order: OrderResponse | null
  isBusy: boolean
  translate: (translationKey: string) => string
  onClose: () => void
  onConfirmPayment: (payload: { orderId: string; paymentMethod: PaymentMethodValue; paymentSucceeded: boolean }) => Promise<void>
}

export function PaymentModal({ isOpen, order, isBusy, translate, onClose, onConfirmPayment }: PaymentModalProps) {
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethodValue | null>(null)

  useEffect(() => {
    if (!isOpen) {
      setSelectedPaymentMethod(null)
    }
  }, [isOpen])

  if (!isOpen || !order) {
    return null
  }

  const paymentQrLabel =
    selectedPaymentMethod === 'alipay'
      ? translate('payment.method.alipay')
      : selectedPaymentMethod === 'wechat-pay'
        ? translate('payment.method.wechat')
        : translate('payment.method.nailong')

  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal-card" role="dialog" aria-modal="true" aria-label={translate('payment.title')}>
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('payment.title')}</p>
            <h3>{`${order.totalPrice} ${order.orderCurrency}`}</h3>
          </div>
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onClose}>
            {translate('payment.close')}
          </button>
        </div>

        {!selectedPaymentMethod ? (
          <div>
            <p className="detail-label">{translate('payment.methodLabel')}</p>
            <div className="payment-method-grid">
              <button type="button" disabled={isBusy} onClick={() => setSelectedPaymentMethod('alipay')}>
                {translate('payment.method.alipay')}
              </button>
              <button type="button" disabled={isBusy} onClick={() => setSelectedPaymentMethod('wechat-pay')}>
                {translate('payment.method.wechat')}
              </button>
              <button type="button" disabled={isBusy} onClick={() => setSelectedPaymentMethod('nailong-pay')}>
                {translate('payment.method.nailong')}
              </button>
            </div>
          </div>
        ) : (
          <div className="stack-form">
            <div className="fake-qr-card">
              <strong>{paymentQrLabel}</strong>
              <div className="fake-qr-grid" aria-hidden="true">
                {Array.from({ length: 36 }).map((_, index) => (
                  <span key={index} className={index % 2 === 0 ? 'is-filled' : ''} />
                ))}
              </div>
            </div>

            <div className="action-cluster">
              <span className="detail-label">{translate('payment.confirmLabel')}</span>
              <button
                type="button"
                disabled={isBusy}
                onClick={() => void onConfirmPayment({ orderId: order.orderId, paymentMethod: selectedPaymentMethod, paymentSucceeded: true })}
              >
                {translate('payment.confirmYes')}
              </button>
              <button
                type="button"
                className="secondary-button"
                disabled={isBusy}
                onClick={() => void onConfirmPayment({ orderId: order.orderId, paymentMethod: selectedPaymentMethod, paymentSucceeded: false })}
              >
                {translate('payment.confirmNo')}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
