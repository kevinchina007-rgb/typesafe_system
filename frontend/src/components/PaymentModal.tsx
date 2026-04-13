import { useEffect, useState } from 'react'

import type { OrderResponse, PaymentLinkResponse } from '../lib/mvp-types'

type PaymentMethodValue = 'alipay' | 'wechat-pay' | 'nailong-pay'

type PaymentModalProps = {
  isOpen: boolean
  order: OrderResponse | null
  isBusy: boolean
  translate: (translationKey: string) => string
  onClose: () => void
  onCreatePaymentLink: (payload: { orderId: string; paymentMethod: PaymentMethodValue }) => Promise<PaymentLinkResponse>
}

export function PaymentModal({ isOpen, order, isBusy, translate, onClose, onCreatePaymentLink }: PaymentModalProps) {
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethodValue | null>(null)
  const [paymentLink, setPaymentLink] = useState<PaymentLinkResponse | null>(null)
  const [isLoadingPaymentLink, setIsLoadingPaymentLink] = useState(false)

  useEffect(() => {
    if (!isOpen) {
      setSelectedPaymentMethod(null)
      setPaymentLink(null)
      setIsLoadingPaymentLink(false)
    }
  }, [isOpen])

  useEffect(() => {
    if (!isOpen || !order || !selectedPaymentMethod) {
      return
    }

    let cancelled = false
    setIsLoadingPaymentLink(true)
    setPaymentLink(null)

    void onCreatePaymentLink({ orderId: order.orderId, paymentMethod: selectedPaymentMethod })
      .then(response => {
        if (!cancelled) {
          setPaymentLink(response)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoadingPaymentLink(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [isOpen, order?.orderId, selectedPaymentMethod, onCreatePaymentLink])

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
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="modal-card"
        role="dialog"
        aria-modal="true"
        aria-label={translate('payment.title')}
        onClick={event => event.stopPropagation()}
      >
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('payment.title')}</p>
            <h3>{`${order.totalPrice} ${order.orderCurrency}`}</h3>
          </div>
          <button type="button" className="secondary-button modal-close-button" disabled={isBusy} onClick={onClose}>
            ×
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
              {isLoadingPaymentLink ? (
                <p className="detail-label">{translate('payment.loadingLink')}</p>
              ) : paymentLink ? (
                <>
                  <div className="payment-qr-frame">
                    <img
                      src={`https://api.qrserver.com/v1/create-qr-code/?size=320x320&margin=24&data=${encodeURIComponent(paymentLink.paymentUrl)}`}
                      alt={translate('payment.qrAlt')}
                      className="payment-qr-image"
                    />
                  </div>
                  <p className="detail-label">{translate('payment.scanHint')}</p>
                  <a href={paymentLink.paymentUrl} target="_blank" rel="noreferrer">
                    {translate('payment.openLink')}
                  </a>
                  <small>{paymentLink.expiresAt}</small>
                </>
              ) : (
                <p className="detail-label">{translate('payment.linkUnavailable')}</p>
              )}
            </div>
            <div className="action-row">
              <button type="button" className="secondary-button" disabled={isBusy} onClick={() => setSelectedPaymentMethod(null)}>
                {translate('payment.changeMethod')}
              </button>
              <button type="button" className="secondary-button" disabled={isBusy} onClick={onClose}>
                {translate('payment.close')}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
