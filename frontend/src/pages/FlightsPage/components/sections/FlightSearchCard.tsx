import type { ChangeEventHandler, ReactNode } from 'react'
import { ArrowLeftRight } from 'lucide-react'

import { flightCityOptions } from '@/app/stores/models/flights'
import { TripTypeSelector } from '@/pages/FlightsPage/components/controls/TripTypeSelector'
import { useMultiCitySearchRows } from '@/pages/FlightsPage/components/hooks/useMultiCitySearchRows'
import { useRoundTripSearchRows } from '@/pages/FlightsPage/components/hooks/useRoundTripSearchRows'
import type { FlightSearchCardProps } from '../../objects'

// FlightsPage 的搜索卡片，负责切换行程类型并填写航线与日期条件。
export function FlightSearchCard({
  tripType,
  departureAirport,
  arrivalAirport,
  departureDate,
  returnDate,
  multiCitySegments,
  translate,
  onTripTypeChange,
  onDepartureAirportChange,
  onArrivalAirportChange,
  onDepartureDateChange,
  onReturnDateChange,
  onMultiCitySegmentChange,
  onAddMultiCitySegment,
  onRemoveMultiCitySegment,
  onSwapRoute,
  showSubmitButton,
  onSubmit,
}: FlightSearchCardProps) {
  const roundTripRows = useRoundTripSearchRows({
    departureAirport,
    arrivalAirport,
    departureDate,
    returnDate,
    onDepartureAirportChange,
    onArrivalAirportChange,
    onDepartureDateChange,
    onReturnDateChange,
  })
  const multiCityRows = useMultiCitySearchRows({
    segments: multiCitySegments,
    onSegmentChange: onMultiCitySegmentChange,
  })

  return (
    <section className="relative z-20 grid gap-6 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/70">
      <TripTypeSelector value={tripType} translate={translate} onChange={onTripTypeChange} />

      {tripType === 'multiCity' ? (
        <div className="grid gap-4">
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-lg font-semibold text-slate-700">{translate('flights.multiCitySegments')}</span>
            <button
              type="button"
              className="inline-flex min-h-12 items-center justify-center border-2 border-slate-300 bg-white px-5 py-2 text-base font-semibold text-slate-950 transition hover:border-slate-950 hover:bg-slate-950 hover:text-white"
              onClick={onAddMultiCitySegment}
            >
              {translate('flights.addSegment')}
            </button>
          </div>

          <div className="grid gap-4">
            {multiCitySegments.map((segment, index) => (
              <div key={segment.id} className="grid gap-4 lg:grid-cols-[1fr_1fr_1fr_auto]">
                <Field label={`${translate('flights.segment')} ${index + 1} ${translate('flights.departureAirport')}`}>
                  <CitySelect
                    value={segment.departureAirport}
                    placeholder={translate('flights.departureAirport')}
                    onChange={event => multiCityRows.updateSegment(segment.id, 'departureAirport', event.target.value)}
                  />
                </Field>
                <Field label={translate('flights.arrivalAirport')}>
                  <CitySelect
                    value={segment.arrivalAirport}
                    placeholder={translate('flights.arrivalAirport')}
                    onChange={event => multiCityRows.updateSegment(segment.id, 'arrivalAirport', event.target.value)}
                  />
                </Field>
                <Field label={translate('flights.date')}>
                  <DateInput
                    value={segment.departureDate}
                    onChange={event => onMultiCitySegmentChange(segment.id, 'departureDate', event.target.value)}
                  />
                </Field>
                <div className="flex items-end">
                  <button
                    type="button"
                    className="inline-flex min-h-14 items-center justify-center border-2 border-slate-300 bg-white px-5 py-2 text-base font-semibold text-slate-950 transition hover:border-slate-950 hover:bg-slate-950 hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                    onClick={() => onRemoveMultiCitySegment(segment.id)}
                    disabled={multiCitySegments.length <= 2}
                  >
                    {translate('flights.removeSegment')}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : tripType === 'roundTrip' ? (
        <div className="grid gap-5">
          <SearchRouteRow
            departureLabel="去程出发地"
            arrivalLabel="去程目的地"
            dateLabel="去程日期"
            departurePlaceholder="出发地"
            arrivalPlaceholder="目的地"
            row={roundTripRows.outboundRow}
            onSwapRoute={onSwapRoute}
          />
          <SearchRouteRow
            departureLabel="返程出发地"
            arrivalLabel="返程目的地"
            dateLabel="返程日期"
            departurePlaceholder="出发地"
            arrivalPlaceholder="目的地"
            row={roundTripRows.returnRow}
            onSwapRoute={onSwapRoute}
          />
        </div>
      ) : (
        <SearchRouteRow
          departureLabel={translate('flights.departureAirport')}
          arrivalLabel={translate('flights.arrivalAirport')}
          dateLabel={translate('flights.date')}
          departurePlaceholder={translate('flights.departureAirport')}
          arrivalPlaceholder={translate('flights.arrivalAirport')}
          row={{
            departureAirport,
            arrivalAirport,
            date: departureDate,
            onDepartureChange: event => onDepartureAirportChange(event.target.value),
            onArrivalChange: event => onArrivalAirportChange(event.target.value),
            onDateChange: onDepartureDateChange,
          }}
          onSwapRoute={onSwapRoute}
        />
      )}

      {showSubmitButton ? (
        <div className="relative z-30 -mb-16 flex justify-center">
          <button
            type="button"
            className="inline-flex min-h-16 min-w-72 items-center justify-center bg-gradient-to-r from-amber-400 to-orange-500 px-10 py-4 text-xl font-bold text-white shadow-xl shadow-orange-200/70 transition hover:from-amber-500 hover:to-orange-600"
            onClick={onSubmit}
          >
            {translate('flights.search')}
          </button>
        </div>
      ) : null}
    </section>
  )
}

// 单条搜索航线行的布局组件，负责渲染出发地、目的地和日期。
function SearchRouteRow({
  departureLabel,
  arrivalLabel,
  dateLabel,
  departurePlaceholder,
  arrivalPlaceholder,
  row,
  onSwapRoute,
}: {
  departureLabel: string
  arrivalLabel: string
  dateLabel: string
  departurePlaceholder: string
  arrivalPlaceholder: string
  row: {
    departureAirport: string
    arrivalAirport: string
    date: string
    onDepartureChange: ChangeEventHandler<HTMLSelectElement>
    onArrivalChange: ChangeEventHandler<HTMLSelectElement>
    onDateChange: (value: string) => void
  }
  onSwapRoute: () => void
}) {
  return (
    <div className="grid gap-4 xl:grid-cols-[1.35fr_auto_1.35fr_1.15fr]">
      <Field label={departureLabel}>
        <CitySelect value={row.departureAirport} placeholder={departurePlaceholder} onChange={row.onDepartureChange} />
      </Field>

      <div className="flex items-end justify-center">
        <button
          type="button"
          aria-label="交换出发地和目的地"
          className="mb-1 inline-flex h-14 w-14 items-center justify-center border-2 border-slate-300 bg-white text-slate-600 transition hover:border-slate-950 hover:text-slate-950"
          onClick={onSwapRoute}
        >
          <ArrowLeftRight className="h-6 w-6" />
        </button>
      </div>

      <Field label={arrivalLabel}>
        <CitySelect value={row.arrivalAirport} placeholder={arrivalPlaceholder} onChange={row.onArrivalChange} />
      </Field>

      <Field label={dateLabel}>
        <DateInput value={row.date} onChange={event => row.onDateChange(event.target.value)} />
      </Field>
    </div>
  )
}

// 表单字段容器，只负责把标签和输入控件放在一起。
function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="grid gap-2">
      <span className="text-base font-medium text-slate-500">{label}</span>
      {children}
    </label>
  )
}

// 日期输入框的统一样式封装。
function DateInput({ value, onChange }: { value: string; onChange: ChangeEventHandler<HTMLInputElement> }) {
  return (
    <input
      type="date"
      value={value}
      onChange={onChange}
      className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
    />
  )
}

// 机场下拉选择框，只负责从城市列表里选出一个值。
function CitySelect({
  value,
  placeholder,
  onChange,
}: {
  value: string
  placeholder: string
  onChange: ChangeEventHandler<HTMLSelectElement>
}) {
  return (
    <select
      value={value}
      onChange={onChange}
      className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
    >
      <option value="">{placeholder}</option>
      {flightCityOptions.map(city => (
        <option key={city} value={city}>
          {city}
        </option>
      ))}
    </select>
  )
}
