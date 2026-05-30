import { HomePageShell } from './components/HomePageShell'
import { useHomePageController } from './hooks'
import { HOME_PAGE_REGIONS } from './objects'

export function HomePage() {
  const controller = useHomePageController()

  return (
    <>
      {/* 页面区域总览：hero / features */}
      <div className="sr-only">
        {HOME_PAGE_REGIONS.map(region => (
          <span key={region}>{region}</span>
        ))}
      </div>
      <HomePageShell controller={controller} />
    </>
  )
}

