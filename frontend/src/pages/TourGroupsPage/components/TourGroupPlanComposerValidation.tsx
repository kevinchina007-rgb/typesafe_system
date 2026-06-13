export function TourGroupPlanComposerValidation({ message }: { message: string }) {
  if (!message) return null
  return <p className="text-sm leading-6 text-slate-500">{message}</p>
}
