// FlightsPage 页面头图参数，只包含标题和说明。
type FlightPageHeroProps = {
  title: string
  description: string
}

// FlightsPage 头图区域，只负责展示页面标题和简介。
export function FlightPageHero({ title, description }: FlightPageHeroProps) {
  return (
    <section className="grid gap-4 border border-sky-200 bg-gradient-to-r from-sky-50 via-white to-amber-50 p-6 shadow-sm shadow-sky-100/50">
      <div className="flex flex-wrap items-center gap-3">
        <span className="inline-flex w-fit bg-sky-500 px-3 py-1 text-xs font-black uppercase tracking-[0.18em] text-white">Flight</span>
        <span className="inline-flex w-fit bg-amber-100 px-3 py-1 text-xs font-bold text-amber-700">航班搜索</span>
      </div>
      <h2 className="m-0 text-5xl font-black leading-none tracking-normal text-slate-950">{title}</h2>
      <p className="m-0 max-w-4xl text-lg leading-8 text-slate-600">{description}</p>
    </section>
  )
}
