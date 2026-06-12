// 本文件定义日期窗口条，负责展示当前日期范围并切换上一页和下一页。

import { ChevronLeft, ChevronRight } from 'lucide-react'

import { addHotelDays, formatHotelDateLabel, formatHotelWeekdayLabel } from '@/app/stores/models/hotel-booking-model'

// 日期窗口条的输入参数。
type DateWindowStripProps = {
  dateWindowStart: string
  selectedDate: string
  isBusy?: boolean
  onPrevious: () => void
  onNext: () => void
  onDateSelect: (date: string) => void
}

// 日期窗口条，负责左右翻页和选择具体日期。
export function DateWindowStrip({ dateWindowStart, selectedDate, isBusy = false, onPrevious, onNext, onDateSelect }: DateWindowStripProps) {
  // 从起始日期开始展开 7 天的可选日期。
  const dates = Array.from({ length: 7 }, (_, index) => addHotelDays(dateWindowStart, index))

  return (
    <section className="relative border border-slate-200 bg-gradient-to-r from-slate-950 via-sky-950 to-indigo-950 p-1 shadow-lg shadow-sky-200/40">
      <WindowButton direction="left" disabled={isBusy} onClick={onPrevious} />
      <div className="grid grid-cols-2 overflow-hidden bg-white sm:grid-cols-4 lg:grid-cols-7">
        {dates.map(date => {
          const isSelected = date === selectedDate
          return (
            <button
              key={date}
              type="button"
              className={`grid gap-1 border-r border-slate-200 px-4 py-5 text-center transition last:border-r-0 hover:bg-sky-50 ${
                isSelected ? 'bg-gradient-to-br from-sky-500 via-cyan-500 to-blue-600 text-white' : 'text-slate-700'
              }`}
              disabled={isBusy}
              onClick={() => onDateSelect(date)}
            >
              <span className={`text-lg font-black ${isSelected ? 'text-white' : 'text-slate-600'}`}>{formatHotelDateLabel(date)}</span>
              <span className={`text-sm font-bold uppercase tracking-[0.14em] ${isSelected ? 'text-white/90' : 'text-cyan-700'}`}>
                {formatHotelWeekdayLabel(date)}
              </span>
            </button>
          )
        })}
      </div>
      <WindowButton direction="right" disabled={isBusy} onClick={onNext} />
    </section>
  )
}

// 左右翻页按钮，放在日期窗口的两侧。
function WindowButton({
  direction,
  disabled,
  onClick,
}: {
  direction: 'left' | 'right'
  disabled: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      className={`group absolute top-1/2 z-10 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-sky-200 bg-white transition hover:bg-sky-500 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-white ${
        direction === 'left' ? '-left-5' : '-right-5'
      }`}
      disabled={disabled}
      onClick={onClick}
      aria-label={direction === 'left' ? '查看前一页' : '查看后一页'}
    >
      {direction === 'left' ? (
        <ChevronLeft className="h-5 w-5 text-sky-700 transition group-hover:text-white" />
      ) : (
        <ChevronRight className="h-5 w-5 text-sky-700 transition group-hover:text-white" />
      )}
    </button>
  )
}
