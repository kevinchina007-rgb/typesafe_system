import type { OrderResponse, ReviewResponse } from '@/lib/mvp-types/index'

export function OrderItemFeedbackActions({
  isBusy,
  order,
  bookedAmount,
  bookedCurrency,
  existingReview,
  translate,
  onDeleteReview,
  onOpenOrderCancellationFeedback,
}: {
  isBusy: boolean
  order: OrderResponse
  bookedAmount: string | number
  bookedCurrency: string
  existingReview: ReviewResponse | null
  translate: (translationKey: string) => string
  onDeleteReview: (reviewId: string) => Promise<void>
  onOpenOrderCancellationFeedback: (orderId: string) => Promise<void>
}) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{`${bookedAmount} ${bookedCurrency}`}</span>
      <button
        type="button"
        className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
        disabled={isBusy}
        onClick={() => void onOpenOrderCancellationFeedback(order.orderId)}
      >
        申请退款及向客服反馈
      </button>
      {existingReview?.canDelete ? (
        <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => void onDeleteReview(existingReview.reviewId)}>
          {translate('reviews.delete')}
        </button>
      ) : null}
    </div>
  )
}
