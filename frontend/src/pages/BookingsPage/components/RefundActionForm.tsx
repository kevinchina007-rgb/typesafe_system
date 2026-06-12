// 退款申请表单参数，只承接禁用状态、翻译和提交回调。
type RefundActionFormProps = {
  disabled: boolean
  translate: (translationKey: string) => string
  onSubmit: (refundReason: string) => Promise<void>
}

// 退款动作表单，只负责输入原因和触发提交。
export function RefundActionForm({ disabled, translate, onSubmit }: RefundActionFormProps) {
  return (
    <form
      className="grid gap-4 md:grid-cols-2"
      onSubmit={async event => {
        event.preventDefault()
        const formData = new FormData(event.currentTarget)
        await onSubmit(String(formData.get('refundReason') ?? ''))
        event.currentTarget.reset()
      }}
    >
      <label>
        {translate('bookings.refund.reason')}
        <input name="refundReason" disabled={disabled} />
      </label>
      <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={disabled}>
        {translate('bookings.requestRefund')}
      </button>
    </form>
  )
}
