import type { OrderPanelProps } from '@/pages/BookingsPage/objects'
import { AttractionOrderCard, FlightOrderCard, HotelOrderCard, TrainOrderCard } from '@/pages/BookingsPage/components/cards'
import { getOrderCategoryDescriptionKey, getOrderCategoryTitle, orderMatchesCategory } from '@/pages/BookingsPage/functions'

export function OrderPanel({
  currentLanguage,
  orderCategory,
  isBusy,
  isGuestMode,
  orders,
  reviews,
  travelers,
  translate,
  onRequireLogin,
  onReloadOrders,
  onOpenPayment,
  onCancelOrder,
  onDeleteReview,
  onOpenOrderCancellationFeedback,
}: OrderPanelProps) {
  const visibleOrders = orders.filter(order => orderMatchesCategory(order, orderCategory))

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="flex flex-wrap items-start justify-between gap-4 text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('topnav.orders')}</p>
          <h2>{getOrderCategoryTitle(orderCategory)}</h2>
        </div>
        <button type="button" disabled={isGuestMode || isBusy} className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={() => void onReloadOrders()}>
          {translate('bookings.refresh')}
        </button>
      </div>

      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate(getOrderCategoryDescriptionKey(orderCategory))}</p>

      <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
        {isGuestMode ? (
          <div className="grid gap-4">
            <p className="text-sm leading-6 text-slate-500">{translate('bookings.guest')}</p>
            <div className="flex flex-wrap items-center gap-3">
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" onClick={onRequireLogin}>
                {translate('bookings.loginToPay')}
              </button>
            </div>
          </div>
        ) : null}

        {!isGuestMode && visibleOrders.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('bookings.empty')}</p> : null}

        {visibleOrders.length > 0 ? (
          <ul className="grid gap-4">
            {visibleOrders.map(order =>
              orderCategory === 'flightOrders' ? (
                <FlightOrderCard
                  key={order.orderId}
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  order={order}
                  reviews={reviews}
                  travelers={travelers}
                  translate={translate}
                  onCancelOrder={onCancelOrder}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                  onOpenPayment={onOpenPayment}
                />
              ) : orderCategory === 'hotelOrders' ? (
                <HotelOrderCard
                  key={order.orderId}
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  order={order}
                  reviews={reviews}
                  travelers={travelers}
                  translate={translate}
                  onCancelOrder={onCancelOrder}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                  onOpenPayment={onOpenPayment}
                />
              ) : orderCategory === 'trainOrders' ? (
                <TrainOrderCard
                  key={order.orderId}
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  order={order}
                  reviews={reviews}
                  travelers={travelers}
                  translate={translate}
                  onCancelOrder={onCancelOrder}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                  onOpenPayment={onOpenPayment}
                />
              ) : (
                <AttractionOrderCard
                  key={order.orderId}
                  currentLanguage={currentLanguage}
                  isBusy={isBusy}
                  order={order}
                  reviews={reviews}
                  travelers={travelers}
                  translate={translate}
                  onCancelOrder={onCancelOrder}
                  onDeleteReview={onDeleteReview}
                  onOpenOrderCancellationFeedback={onOpenOrderCancellationFeedback}
                  onOpenPayment={onOpenPayment}
                />
              ),
            )}
          </ul>
        ) : null}
      </div>
    </section>
  )
}
