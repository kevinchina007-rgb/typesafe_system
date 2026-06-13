export function FeedbackConversationInputArea({
  draftMessage,
  onDraftMessageChange,
  onSubmit,
  sendLabel,
}: {
  draftMessage: string
  onDraftMessageChange: (value: string) => void
  onSubmit: () => void
  sendLabel: string
}) {
  return (
    <form
      className="mt-3 grid gap-3"
      onSubmit={event => {
        event.preventDefault()
        onSubmit()
      }}
    >
      <textarea
        className="min-h-24 resize-none border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
        rows={3}
        value={draftMessage}
        onChange={event => onDraftMessageChange(event.target.value)}
      />
      <button className="justify-self-end min-h-10 border border-pink-500 bg-pink-500 px-6 py-2 text-sm font-bold text-white hover:bg-pink-600" type="submit">
        {sendLabel}
      </button>
    </form>
  )
}

