import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import type { BookingsPageProps } from './objects'
import { useBookingsPageController } from './hooks'
import { OrderPanel, PaymentModal } from './components'

// BookingsPage 页面入口，只负责把控制器结果交给各个子组件。
export function BookingsPage(props: BookingsPageProps) {
  const controller = useBookingsPageController(props)
  const { currentLanguage, orderCategory, isSessionReady, translate } = props

  return (
    <>
      <OrderPanel
        currentLanguage={currentLanguage}
        orderCategory={orderCategory}
        isBusy={controller.isBusy}
        isGuestMode={isSessionReady && controller.isGuestMode}
        orders={controller.orders}
        reviews={controller.reviews}
        travelers={controller.travelers}
        translate={translate}
        onRequireLogin={controller.onRequireLogin}
        onReloadOrders={controller.onReloadOrders}
        onOpenPayment={controller.onOpenPayment}
        onCancelOrder={controller.onCancelOrder}
        onDeleteReview={controller.onDeleteReview}
        onOpenOrderCancellationFeedback={controller.onOpenOrderCancellationFeedback}
      />

      <PaymentModal
        isOpen={controller.pendingPaymentOrder !== null}
        order={controller.pendingPaymentOrder}
        travelers={controller.travelers}
        isBusy={controller.isBusy}
        onClose={() => controller.setPendingPaymentOrder(null)}
        onCreatePaymentLink={controller.onCreatePaymentLink}
        onConfirmPayment={controller.onConfirmPayment}
      />

      <AuthRequiredDialog
        isOpen={controller.isAuthDialogOpen}
        title={translate('authRequired.paymentTitle')}
        description={translate('authRequired.paymentDescription')}
        translate={translate}
        onClose={controller.onAuthDialogClose}
        onConfirm={controller.onAuthDialogConfirm}
      />
    </>
  )
}
