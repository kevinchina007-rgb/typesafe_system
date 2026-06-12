import { HomePageShell } from './components/HomePageShell'
import { useHomePageController } from './hooks'
import { HOME_PAGE_REGIONS, type HomePageProps } from './objects'

// 首页入口，只负责把壳组件和可访问性区域组合起来。
export function HomePage({ onNavigate }: HomePageProps) {
  const controller = useHomePageController()

  return (
    <>
      {/* 页面区域总览：hero / features */}
      <div className="sr-only">
        {HOME_PAGE_REGIONS.map(region => (
          <span key={region}>{region}</span>
        ))}
      </div>
      <HomePageShell controller={controller} onNavigate={onNavigate} />
    </>
  )
}
