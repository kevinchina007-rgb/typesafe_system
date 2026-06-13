import { useState, type FocusEvent } from 'react'

import { departureTimeWindows } from '@/pages/FlightsPage/functions'
import type { FlightSortMode } from '../../objects'
import { FlightResultsSorting } from './FlightResultsSorting'

export type FlightResultsFiltersProps = {
  airlineOptions: string[]
  departureAirportOptions: string[]
  arrivalAirportOptions: string[]
  cabinOptions: string[]
  selectedAirline: string
  selectedTimeRange: string
  selectedDepartureAirport: string
  selectedArrivalAirport: string
  selectedCabin: string
  sortMode: FlightSortMode
  onAirlineChange: (value: string) => void
  onTimeRangeChange: (value: string) => void
  onDepartureAirportChange: (value: string) => void
  onArrivalAirportChange: (value: string) => void
  onCabinChange: (value: string) => void
  onSortModeChange: (value: FlightSortMode) => void
  formatAirportName: (value: string) => string
  formatCabinLabel: (value: string) => string
}

export function FlightResultsFilters({
  airlineOptions,
  departureAirportOptions,
  arrivalAirportOptions,
  cabinOptions,
  selectedAirline,
  selectedTimeRange,
  selectedDepartureAirport,
  selectedArrivalAirport,
  selectedCabin,
  sortMode,
  onAirlineChange,
  onTimeRangeChange,
  onDepartureAirportChange,
  onArrivalAirportChange,
  onCabinChange,
  onSortModeChange,
  formatAirportName,
  formatCabinLabel,
}: FlightResultsFiltersProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-4 bg-white px-6 py-5">
      <div className="flex flex-wrap items-center gap-3">
        <FilterSelect label="航空公司" value={selectedAirline} onChange={onAirlineChange} options={airlineOptions} />
        <FilterSelect label="起降时间" value={selectedTimeRange} onChange={onTimeRangeChange} options={departureTimeWindows} />
        <FilterSelect label="出发机场" value={selectedDepartureAirport} onChange={onDepartureAirportChange} options={departureAirportOptions} renderOption={formatAirportName} />
        <FilterSelect label="到达机场" value={selectedArrivalAirport} onChange={onArrivalAirportChange} options={arrivalAirportOptions} renderOption={formatAirportName} />
        <FilterSelect label="舱位" value={selectedCabin} onChange={onCabinChange} options={cabinOptions} renderOption={formatCabinLabel} />
      </div>

      <FlightResultsSorting sortMode={sortMode} onSortModeChange={onSortModeChange} />
    </div>
  )
}

function FilterSelect({
  label,
  value,
  options,
  onChange,
  renderOption = option => option,
}: {
  label: string
  value: string
  options: string[]
  onChange: (value: string) => void
  renderOption?: (value: string) => string
}) {
  const [isOpen, setIsOpen] = useState(false)
  const selectedLabel = value === 'all' ? label : renderOption(value)
  const menuOptions = [{ value: 'all', label: '无要求' }, ...options.map(option => ({ value: option, label: renderOption(option) }))]

  function handleBlur(event: FocusEvent<HTMLDivElement>) {
    if (!event.currentTarget.contains(event.relatedTarget)) {
      setIsOpen(false)
    }
  }

  return (
    <div className="relative inline-flex min-w-40" onBlur={handleBlur}>
      <button
        type="button"
        className="flex h-14 w-full min-w-40 items-center justify-between border-2 border-slate-400 bg-white px-4 text-left text-lg text-slate-950 outline-none focus:border-sky-500"
        onClick={() => setIsOpen(open => !open)}
        aria-haspopup="listbox"
        aria-expanded={isOpen}
      >
        <span className="truncate">{selectedLabel}</span>
        <span className="ml-4 h-2.5 w-2.5 -translate-y-1 rotate-45 border-b-2 border-r-2 border-slate-950" />
      </button>
      {isOpen ? (
        <div className="absolute left-0 top-full z-30 w-max min-w-full border border-slate-300 bg-white shadow-lg" role="listbox">
          {menuOptions.map(option => (
            <button
              key={option.value}
              type="button"
              className={`block w-full px-4 py-2 text-left text-lg ${
                option.value === value ? 'bg-blue-600 text-white' : 'bg-white text-slate-950 hover:bg-slate-100'
              }`}
              onMouseDown={event => event.preventDefault()}
              onClick={() => {
                onChange(option.value)
                setIsOpen(false)
              }}
              role="option"
              aria-selected={option.value === value}
            >
              {option.label}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  )
}
