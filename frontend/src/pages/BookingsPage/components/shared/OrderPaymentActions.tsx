import type { OrderResponse } from '@/lib/mvp-types/index'
import { isOrderPayable } from '@/pages/BookingsPage/functions'

export function OrderPaymentActions({
  isBusy,
  order,
  translate,
  onCancelOrder,
  onOpenPayment,
}: {
  isBusy: boolean
  order: OrderResponse
  translate: (translationKey: string) => string
  onCancelOrder: (orderId: string) => Promise<void>
  onOpenPayment: (order: OrderResponse) => void
}) {
  return (
    <div className="mt-4 flex flex-wrap items-center gap-3">
      {isOrderPayable(order.status) ? (
        <>
          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={() => onOpenPayment(order)}>
            {translate('bookings.pay')}
          </button>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => void onCancelOrder(order.orderId)}>
            {translate('bookings.cancel')}
          </button>
        </>
      ) : null}
    </div>
  )
}
