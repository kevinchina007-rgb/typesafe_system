import type { ReactNode } from 'react'
import { useMemo, useState } from 'react'

import { formatFlightAirportLabel, getFlightDetailsPlannerCityAirportCodes } from '@/app/stores/models/flights/flightConstants'
import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { normalizeDateTimeInput } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import {
  buildLocalDateTime,
  bumpClockInsideWindow,
  calculateCabinActualPrice,
  CitySelect,
  createFlightTimeWindows,
  defaultCreateFlightDraft,
  isClockInsideWindow,
  type CreateFlightDraft,
} from './manager-panel-workspace-flight'

export function CreateFlightSection({
  isBusy,
  onCreateManagerFlight,
  onValidationError,
}: {
  isBusy: boolean
  translate: (translationKey: string) => string
  onCreateManagerFlight: ManagerPanelProps['onCreateManagerFlight']
  onValidationError: ManagerPanelProps['onValidationError']
}) {
  const [draft, setDraft] = useState<CreateFlightDraft>(defaultCreateFlightDraft)
  const departureAirportOptions = useMemo(() => getFlightDetailsPlannerCityAirportCodes(draft.departureCity), [draft.departureCity])
  const arrivalAirportOptions = useMemo(() => getFlightDetailsPlannerCityAirportCodes(draft.arrivalCity), [draft.arrivalCity])
  const selectedWindow = createFlightTimeWindows.find(option => option.value === draft.timeRange) ?? createFlightTimeWindows[2]

  function validateDraft(): string | null {
    if (!draft.flightNumber.trim()) return '请填写航班号。'
    if (!draft.departureCity || !draft.arrivalCity) return '请先选择出发地点和目的地。'
    if (draft.departureCity === draft.arrivalCity) return '出发地点和目的地不能相同。'
    if (!draft.departureAirport || !draft.arrivalAirport) return '请根据城市选择对应机场。'
    if (!draft.departureDate) return '请填写出发日期。'
    if (!isClockInsideWindow(draft.departureClock, selectedWindow.start, selectedWindow.end) || !isClockInsideWindow(draft.arrivalClock, selectedWindow.start, selectedWindow.end)) {
      return '具体出发时间和到达时间必须落在所选时段内。'
    }
    if (buildLocalDateTime(draft.departureDate, draft.arrivalClock) <= buildLocalDateTime(draft.departureDate, draft.departureClock)) return '到达时间必须晚于出发时间。'
    if ([draft.economySeatCount, draft.premiumEconomySeatCount, draft.businessSeatCount, draft.firstSeatCount].some(value => value <= 0)) return '每个舱位的数量都要大于 0。'
    if ([draft.economyPrice, draft.premiumEconomyPrice, draft.businessPrice, draft.firstPrice].some(value => Number(value) <= 0)) return '每个舱位的原价都要大于 0。'
    const discountRates = [draft.economyDiscountRate, draft.premiumEconomyDiscountRate, draft.businessDiscountRate, draft.firstDiscountRate].map(Number)
    if (discountRates.some(value => !Number.isFinite(value) || value <= 0 || value > 10)) return '折扣要填写 0 到 10 之间的数字，例如 8.5 表示八五折。'
    return null
  }

  return (
    <form
      className="mx-auto grid w-full max-w-[75%] gap-7 border border-slate-200 bg-white p-7 text-slate-950 shadow-sm shadow-slate-200/50 max-xl:max-w-full"
      onSubmit={async event => {
        event.preventDefault()
        const validationMessage = validateDraft()
        if (validationMessage) {
          onValidationError(validationMessage)
          return
        }
        await onCreateManagerFlight({
          flightNumber: draft.flightNumber.trim(),
          departureAirport: draft.departureAirport,
          arrivalAirport: draft.arrivalAirport,
          departureTime: normalizeDateTimeInput(buildLocalDateTime(draft.departureDate, draft.departureClock)),
          arrivalTime: normalizeDateTimeInput(buildLocalDateTime(draft.departureDate, draft.arrivalClock)),
          economyCabin: {
            seatCount: draft.economySeatCount,
            originalPrice: draft.economyPrice,
            discounted: draft.economyDiscounted,
            discountRate: draft.economyDiscountRate,
          },
          premiumEconomyCabin: {
            seatCount: draft.premiumEconomySeatCount,
            originalPrice: draft.premiumEconomyPrice,
            discounted: draft.premiumEconomyDiscounted,
            discountRate: draft.premiumEconomyDiscountRate,
          },
          businessCabin: {
            seatCount: draft.businessSeatCount,
            originalPrice: draft.businessPrice,
            discounted: draft.businessDiscounted,
            discountRate: draft.businessDiscountRate,
          },
          firstCabin: {
            seatCount: draft.firstSeatCount,
            originalPrice: draft.firstPrice,
            discounted: draft.firstDiscounted,
            discountRate: draft.firstDiscountRate,
          },
          currency: 'CNY',
        })
        setDraft(defaultCreateFlightDraft)
      }}
    >
      <div>
        <p className="m-0 text-base font-bold text-slate-500">创建航班</p>
        <h3 className="m-0 text-3xl font-black text-slate-950">填写核心航班信息</h3>
      </div>

      <div className="grid gap-5">
        <div className="grid gap-4 xl:grid-cols-2">
          <CreateFlightField label="出发地点" important>
            <CitySelect value={draft.departureCity} placeholder="请选择出发地点" onChange={event => setDraft({ ...draft, departureCity: event.target.value, departureAirport: '' })} />
          </CreateFlightField>
          <CreateFlightField label="出发机场" important>
            <select value={draft.departureAirport} onChange={event => setDraft({ ...draft, departureAirport: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950">
              <option value="">请先选择出发机场</option>
              {departureAirportOptions.map(airport => <option key={airport} value={airport}>{formatFlightAirportLabel(airport)}</option>)}
            </select>
          </CreateFlightField>
        </div>

        <div className="grid gap-4 xl:grid-cols-2">
          <CreateFlightField label="目的地" important>
            <CitySelect value={draft.arrivalCity} placeholder="请选择目的地" onChange={event => setDraft({ ...draft, arrivalCity: event.target.value, arrivalAirport: '' })} />
          </CreateFlightField>
          <CreateFlightField label="到达机场" important>
            <select value={draft.arrivalAirport} onChange={event => setDraft({ ...draft, arrivalAirport: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950">
              <option value="">请先选择到达机场</option>
              {arrivalAirportOptions.map(airport => <option key={airport} value={airport}>{formatFlightAirportLabel(airport)}</option>)}
            </select>
          </CreateFlightField>
        </div>

        <div className="grid gap-4 xl:grid-cols-4">
          <CreateFlightField label="日期" important>
            <input type="date" value={draft.departureDate} onChange={event => setDraft({ ...draft, departureDate: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950" />
          </CreateFlightField>
          <CreateFlightField label="时段" important>
            <select
              value={draft.timeRange}
              onChange={event => {
                const nextWindow = createFlightTimeWindows.find(option => option.value === event.target.value) ?? createFlightTimeWindows[2]
                setDraft({ ...draft, timeRange: nextWindow.value, departureClock: nextWindow.start, arrivalClock: bumpClockInsideWindow(nextWindow.start, nextWindow.end) })
              }}
              className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
            >
              {createFlightTimeWindows.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
          </CreateFlightField>
          <CreateFlightField label="出发时间" important>
            <input type="time" min={selectedWindow.start} max={selectedWindow.end} value={draft.departureClock} onChange={event => setDraft({ ...draft, departureClock: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950" />
          </CreateFlightField>
          <CreateFlightField label="到达时间" important>
            <input type="time" min={selectedWindow.start} max={selectedWindow.end} value={draft.arrivalClock} onChange={event => setDraft({ ...draft, arrivalClock: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950" />
          </CreateFlightField>
        </div>

        <CreateFlightField label="航班号">
          <input value={draft.flightNumber} onChange={event => setDraft({ ...draft, flightNumber: event.target.value })} className="min-h-12 w-full border-2 border-slate-300 bg-white px-4 text-lg font-medium text-slate-950 outline-none transition focus:border-slate-950" />
        </CreateFlightField>
      </div>

      <div className="grid gap-4">
        <h4 className="m-0 text-2xl font-black text-slate-950">舱位价格与数量</h4>
        <div className="grid gap-4 xl:grid-cols-2 2xl:grid-cols-4">
          <CabinDraftCard title="经济舱" seats={draft.economySeatCount} price={draft.economyPrice} discounted={draft.economyDiscounted} discountRate={draft.economyDiscountRate} onSeatsChange={economySeatCount => setDraft({ ...draft, economySeatCount })} onPriceChange={economyPrice => setDraft({ ...draft, economyPrice })} onDiscountedChange={economyDiscounted => setDraft({ ...draft, economyDiscounted })} onDiscountRateChange={economyDiscountRate => setDraft({ ...draft, economyDiscountRate })} />
          <CabinDraftCard title="超级经济舱" seats={draft.premiumEconomySeatCount} price={draft.premiumEconomyPrice} discounted={draft.premiumEconomyDiscounted} discountRate={draft.premiumEconomyDiscountRate} onSeatsChange={premiumEconomySeatCount => setDraft({ ...draft, premiumEconomySeatCount })} onPriceChange={premiumEconomyPrice => setDraft({ ...draft, premiumEconomyPrice })} onDiscountedChange={premiumEconomyDiscounted => setDraft({ ...draft, premiumEconomyDiscounted })} onDiscountRateChange={premiumEconomyDiscountRate => setDraft({ ...draft, premiumEconomyDiscountRate })} />
          <CabinDraftCard title="商务舱" seats={draft.businessSeatCount} price={draft.businessPrice} discounted={draft.businessDiscounted} discountRate={draft.businessDiscountRate} onSeatsChange={businessSeatCount => setDraft({ ...draft, businessSeatCount })} onPriceChange={businessPrice => setDraft({ ...draft, businessPrice })} onDiscountedChange={businessDiscounted => setDraft({ ...draft, businessDiscounted })} onDiscountRateChange={businessDiscountRate => setDraft({ ...draft, businessDiscountRate })} />
          <CabinDraftCard title="头等舱" seats={draft.firstSeatCount} price={draft.firstPrice} discounted={draft.firstDiscounted} discountRate={draft.firstDiscountRate} onSeatsChange={firstSeatCount => setDraft({ ...draft, firstSeatCount })} onPriceChange={firstPrice => setDraft({ ...draft, firstPrice })} onDiscountedChange={firstDiscounted => setDraft({ ...draft, firstDiscounted })} onDiscountRateChange={firstDiscountRate => setDraft({ ...draft, firstDiscountRate })} />
        </div>
      </div>

      <button className="inline-flex min-h-12 w-fit items-center justify-center bg-pink-500 px-8 py-3 text-base font-bold text-white transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
        创建航班
      </button>
    </form>
  )
}

// 创建航班表单字段包装器，统一标签和强调样式。
function CreateFlightField({ label, important = false, children }: { label: string; important?: boolean; children: ReactNode }) {
  return (
    <label className="grid gap-2">
      <span className={important ? 'text-xl font-black text-slate-950' : 'text-base font-bold text-slate-500'}>{label}</span>
      {children}
    </label>
  )
}

// 舱位草稿卡片，负责配置单个舱位的票数、价格和折扣。
function CabinDraftCard({
  title,
  seats,
  price,
  discounted,
  discountRate,
  onSeatsChange,
  onPriceChange,
  onDiscountedChange,
  onDiscountRateChange,
}: {
  title: string
  seats: number
  price: string
  discounted: boolean
  discountRate: string
  onSeatsChange: (value: number) => void
  onPriceChange: (value: string) => void
  onDiscountedChange: (value: boolean) => void
  onDiscountRateChange: (value: string) => void
}) {
  const actualPrice = calculateCabinActualPrice(price, discounted, discountRate)
  return (
    <div className="grid gap-3 border border-slate-200 bg-slate-50 p-4">
      <strong className="text-lg font-black text-slate-950">{title}</strong>
      <label className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">数量</span>
        <input type="number" min={1} value={seats} onChange={event => onSeatsChange(Number(event.target.value))} className="min-h-11 border-2 border-slate-300 bg-white px-3 text-base outline-none focus:border-slate-950" />
      </label>
      <label className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">原价</span>
        <input type="number" min={1} value={price} onChange={event => onPriceChange(event.target.value)} className="min-h-11 border-2 border-slate-300 bg-white px-3 text-base outline-none focus:border-slate-950" />
      </label>
      <label className="flex items-center gap-2 text-sm font-bold text-slate-600">
        <input type="checkbox" checked={discounted} onChange={event => onDiscountedChange(event.target.checked)} className="h-4 w-4 accent-pink-500" />
        是否打折
      </label>
      <label className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">折扣</span>
        <input type="number" min={0.1} max={10} step={0.1} value={discountRate} disabled={!discounted} onChange={event => onDiscountRateChange(event.target.value)} className="min-h-11 border-2 border-slate-300 bg-white px-3 text-base outline-none focus:border-slate-950 disabled:bg-slate-100 disabled:text-slate-400" />
      </label>
      <p className="m-0 text-base font-black text-orange-600">{`实际价格：¥${actualPrice}`}</p>
    </div>
  )
}

// 航班查询卡片，负责填写查询条件和触发搜索。
