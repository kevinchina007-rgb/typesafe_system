import type { CustomerFeedbackPageProps } from './objects'
import { useCustomerFeedbackPageController } from './hooks'
import { CustomerFeedbackWorkspace } from './components'

export function CustomerFeedbackPage(props: CustomerFeedbackPageProps) {
  const controller = useCustomerFeedbackPageController(props)

  if (!props.signedInUser) {
    return (
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <p className="text-sm leading-6 text-slate-500">{props.translate('feedback.userGuest')}</p>
      </section>
    )
  }

  return (
    <CustomerFeedbackWorkspace
      signedInUser={props.signedInUser}
      orderedThreads={controller.orderedThreads}
      cancellationOrders={controller.cancellationOrders}
      translate={props.translate}
    />
  )
}
