import type { HomePageController } from '../objects'
import type { HomePageProps } from '../objects'

type HomePageShellProps = {
  controller: HomePageController
} & HomePageProps

export function HomePageShell({ controller, onNavigate }: HomePageShellProps) {
  return (
    <div className="bg-slate-50">
      <section className="relative min-h-screen overflow-hidden bg-slate-950">
        {controller.homeHeroSlides.map((slide, index) => (
          <img
            key={slide.image}
            className={`absolute inset-0 h-full w-full object-cover opacity-0 transition-opacity duration-[1200ms] ${
              index === controller.activeIndex ? 'opacity-100 [animation:home-hero-zoom_7.2s_linear_forwards]' : ''
            }`}
            src={slide.image}
            alt={slide.location}
          />
        ))}

        <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(3,8,18,0.12),rgba(3,8,18,0.72)),linear-gradient(90deg,rgba(3,8,18,0.58),transparent_68%)]" />

        <div className="absolute bottom-12 left-8 z-10 max-w-[min(80rem,calc(100vw-4rem))] text-left md:bottom-16 md:left-14">
          <p className="mb-4 whitespace-nowrap font-serif text-3xl font-medium tracking-[0] text-white md:text-5xl">
            {controller.activeSlide.location}
            <span className="ml-4 text-xl font-normal text-white/80 md:text-3xl">{controller.activeSlide.nativeLocation}</span>
          </p>
          <h1 className="whitespace-nowrap font-['STKaiti','KaiTi','Noto_Serif_SC',serif] text-4xl font-normal tracking-[0] text-white md:text-6xl">
            {controller.typedTagline}
            <span aria-hidden="true" className="[animation:home-hero-cursor_1s_steps(1)_infinite]">
              |
            </span>
          </h1>
        </div>

        <div className="absolute bottom-10 right-8 z-10 flex gap-2 md:right-14">
          {controller.homeHeroSlides.map((slide, index) => (
            <span
              key={slide.image}
              className={`h-px w-8 transition-colors ${index === controller.activeIndex ? 'bg-white' : 'bg-white/35'}`}
            />
          ))}
        </div>
      </section>

      <section className="mx-auto grid max-w-7xl gap-8 px-8 py-20 md:px-14">
        <div className="text-left">
          <p className="mb-3 text-sm tracking-[0.24em] text-slate-500">TRAVEL WORKBENCH</p>
          <h2 className="text-3xl font-medium tracking-[0] text-slate-900">把旅程慢慢展开</h2>
        </div>

        <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-4">
          {controller.homeFeaturePlaceholders.map(feature => (
            <button
              key={feature.targetViewKey}
              type="button"
              className="grid gap-5 border border-slate-200 bg-white p-5 text-left transition duration-200 hover:-translate-y-1 hover:border-sky-400 hover:shadow-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500 focus-visible:ring-offset-2"
              onClick={() => onNavigate(feature.targetViewKey)}
              aria-label={`前往${feature.label}预订界面`}
            >
              <div className="aspect-[16/10] bg-slate-100" />
              <div className="text-left">
                <h3 className="text-xl font-medium tracking-[0] text-slate-900">{feature.label}</h3>
                <p className="mt-2 text-sm text-slate-500">内容待定</p>
              </div>
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}
