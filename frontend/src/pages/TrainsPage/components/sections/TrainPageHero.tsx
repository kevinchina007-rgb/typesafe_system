type TrainPageHeroProps = {
  title: string
  description: string
}

export function TrainPageHero({ title, description }: TrainPageHeroProps) {
  return (
    <>
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{title}</p>
          <h2>{title}</h2>
        </div>
      </div>

      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{description}</p>
    </>
  )
}
