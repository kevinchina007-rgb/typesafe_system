import { formatIsoDateTime, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import type { ManagerFlightPlannerResponse } from '@/lib/mvp-types/manager'
import type { ManagerFlightOrderResponse } from '@/lib/mvp-types/manager'
import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { formatFlightAirportLabel } from '@/app/stores/models/flights/flightConstants'
import { getFlightDetailsPlannerAirlineDisplayNameByCode } from '@/app/stores/models/flights/flightAirlineCatalog'
import { formatFlightClock, formatManagerFlightTravelers, formatTravelerPreferenceSummary, formatTravelerRequirementSummary, normalizeCabinKey, statusBadgeClassName, type AirlineProfileDraft } from './manager-panel-workspace-profile'
import { TimeBlock } from './manager-panel-workspace-flight'

export function ManagerFlightOrdersSection({
  currentLanguage,
  flight,
  isLoading,
  orders,
  profile,
  onBack,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  flight: ManagerFlightPlannerResponse
  isLoading: boolean
  orders: ManagerFlightOrderResponse[]
  profile: AirlineProfileDraft
  onBack: () => void
}) {
  const cabinSections = [
    { key: 'ECONOMY', label: '经济舱' },
    { key: 'PREMIUM_ECONOMY', label: '超级经济舱' },
    { key: 'BUSINESS', label: '商务舱' },
    { key: 'FIRST', label: '头等舱' },
  ]
  const airlineName = profile.companyName || getFlightDetailsPlannerAirlineDisplayNameByCode(flight.airlineCode, flight.airlineName)

  return (
    <section className="grid gap-6 bg-slate-100 px-6 pb-8 pt-8">
      <div className="grid gap-5 bg-white p-7">
        <button
          type="button"
          className="inline-flex min-h-11 w-fit items-center justify-center border border-slate-300 bg-white px-5 py-2 text-sm font-bold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
          onClick={onBack}
        >
          返回航班管理
        </button>

        <div className="grid gap-5 xl:grid-cols-[minmax(260px,1fr)_minmax(360px,1.2fr)]">
          <div>
            <p className="m-0 text-sm font-bold text-slate-500">航班订单</p>
            <h3 className="m-0 text-3xl font-black text-slate-950">{airlineName}</h3>
            <p className="m-0 text-base font-semibold text-sky-600">{`${flight.flightNumber} ${flight.aircraftModel ?? ''}`}</p>
          </div>
          <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-5">
            <TimeBlock time={formatFlightClock(flight.departureTime)} airport={formatFlightAirportLabel(flight.departureAirport)} />
            <div className="h-px min-w-24 bg-slate-200" />
            <TimeBlock time={formatFlightClock(flight.arrivalTime)} airport={formatFlightAirportLabel(flight.arrivalAirport)} />
          </div>
        </div>
      </div>

      {isLoading ? <p className="m-0 bg-white px-7 py-10 text-lg text-slate-500">正在读取订单...</p> : null}

      {!isLoading ? (
        <div className="grid gap-5">
          {cabinSections.map(section => {
            const cabinOrders = orders.filter(order => normalizeCabinKey(order.cabinClass) === section.key)
            return (
              <section key={section.key} className="grid gap-3 bg-white p-6">
                <div className="flex flex-wrap items-baseline justify-between gap-3">
                  <h4 className="m-0 text-2xl font-black text-slate-950">{section.label}</h4>
                  <span className="text-sm font-medium text-slate-500">{`${cabinOrders.length} 个订单`}</span>
                </div>
                {cabinOrders.length > 0 ? (
                  <ul className="grid gap-3">
                    {cabinOrders.map(order => (
                      <ManagerFlightOrderCard key={order.orderItemId} currentLanguage={currentLanguage} order={order} />
                    ))}
                  </ul>
                ) : (
                  <p className="m-0 border border-dashed border-slate-200 px-5 py-8 text-base text-slate-500">这个舱位暂时没有订单。</p>
                )}
              </section>
            )
          })}
        </div>
      ) : null}
    </section>
  )
}

// 航班订单卡片，负责展示买家、旅客和订单状态。
function ManagerFlightOrderCard({
  currentLanguage,
  order,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  order: ManagerFlightOrderResponse
}) {
  return (
    <li className="grid gap-4 border border-slate-200 bg-white p-5 md:grid-cols-[1fr_1fr_auto]">
      <div className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">用户昵称</span>
        <strong className="text-xl font-black text-slate-950">{order.buyerNickname || order.buyerUserId}</strong>
      </div>
      <div className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">出行人</span>
        {order.travelers.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {order.travelers.map(traveler => (
              <ManagerTravelerHoverCard key={traveler.travelerId} traveler={traveler} />
            ))}
          </div>
        ) : (
          <strong className="text-base font-bold text-slate-950">{formatManagerFlightTravelers(order)}</strong>
        )}
      </div>
      <div className="grid gap-1 text-left md:text-right">
        <span className="text-sm font-medium text-slate-500">{formatIsoDateTime(order.orderCreatedAt, '-')}</span>
        <span className={statusBadgeClassName(order.orderStatus)}>{mapBackendStatusToProductLabel(order.orderStatus, currentLanguage)}</span>
      </div>
    </li>
  )
}

// 旅客悬浮卡片，负责展示单个旅客的完整信息摘要。
function ManagerTravelerHoverCard({ traveler }: { traveler: ManagerFlightOrderResponse['travelers'][number] }) {
  return (
    <span className="group relative inline-flex">
      <button
        type="button"
        className="inline-flex min-h-9 items-center border border-slate-300 bg-white px-3 text-sm font-black text-slate-950 transition hover:border-pink-500 hover:text-pink-600"
      >
        {traveler.fullName}
      </button>
      <span className="pointer-events-none absolute left-0 top-11 z-20 hidden w-80 border border-slate-300 bg-white p-4 text-left shadow-xl group-hover:grid">
        <strong className="text-lg font-black text-slate-950">{traveler.fullName}</strong>
        <span className="mt-1 text-sm text-slate-600">{`${traveler.serviceSummary.age ?? '-'}岁 / ${traveler.basicInfo.gender} / ${traveler.basicInfo.nationality}`}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">证件</span>
        <span className="text-sm text-slate-950">{`${traveler.documentInfo.documentType} ${traveler.documentInfo.documentNumber}`}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">联系</span>
        <span className="text-sm text-slate-950">{traveler.serviceSummary.contactLabel || traveler.contactInfo.phone}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">偏好</span>
        <span className="text-sm text-slate-950">{formatTravelerPreferenceSummary(traveler)}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">特殊要求</span>
        <span className={traveler.serviceSummary.warningLevel === 'attention' ? 'text-sm font-bold text-pink-600' : 'text-sm text-slate-950'}>
          {formatTravelerRequirementSummary(traveler)}
        </span>
      </span>
    </span>
  )
}
// 把 HH:mm 转成分钟数，方便做时间比较。
