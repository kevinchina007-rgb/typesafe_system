type RefundActionFormProps = {
  disabled: boolean
  translate: (translationKey: string) => string
  onSubmit: (refundReason: string) => Promise<void>
}

export function RefundActionForm({ disabled, translate, onSubmit }: RefundActionFormProps) {
  return (
    <form
      className="inline-form"
      onSubmit={async event => {
        event.preventDefault()
        const formData = new FormData(event.currentTarget)
        await onSubmit(String(formData.get('refundReason') ?? ''))
        event.currentTarget.reset()
      }}
    >
      <label>
        {translate('bookings.refund.reason')}
        <input name="refundReason" placeholder={translate('bookings.refund.reasonPlaceholder')} disabled={disabled} />
      </label>
      <button type="submit" disabled={disabled}>
        {translate('bookings.requestRefund')}
      </button>
    </form>
  )
}
