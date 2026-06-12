// 本文件作为当前目录的入口导出文件。

import { SmartTripPlannerPageShell } from './components/SmartTripPlannerPageShell'
import { SMART_TRIP_PLANNER_PAGE_REGIONS, type SmartTripPlannerPageProps } from './objects'

export function SmartTripPlannerPage({ translate, onSelectView }: SmartTripPlannerPageProps) {
  return (
    <>
      {/* 页面区域总览：hero / search / empty */}
      <div className="sr-only">
        {SMART_TRIP_PLANNER_PAGE_REGIONS.map(region => (
          <span key={region}>{region}</span>
        ))}
      </div>
      <SmartTripPlannerPageShell translate={translate} onSelectView={onSelectView} />
    </>
  )
}

