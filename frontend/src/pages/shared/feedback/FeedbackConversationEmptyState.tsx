export function FeedbackConversationEmptyState({
  title,
  description,
}: {
  title: string
  description: string
}) {
  return (
    <section className="mx-auto grid min-h-[calc(100vh-10rem)] w-full max-w-4xl content-start gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div>
        <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{title}</h2>
      </div>
      <p className="text-sm leading-6 text-slate-500">{description}</p>
    </section>
  )
}

