import type { TripType } from '@/app/stores/models/flights'

// 航班搜索的行程类型切换参数。
type TripTypeSelectorProps = {
  value: TripType
  translate: (translationKey: string) => string
  onChange: (value: TripType) => void
}

// 可选的行程类型列表，顺序与页面按钮一致。
const tripTypeOptions: TripType[] = ['oneWay', 'roundTrip', 'multiCity']

// 行程类型切换器，把当前值和翻译函数组合成按钮组。
export function TripTypeSelector({ value, translate, onChange }: TripTypeSelectorProps) {
  return (
    <div className="flex flex-wrap items-center gap-3" role="tablist" aria-label={translate('flights.tripType')}>
      {tripTypeOptions.map(option => (
        <button
          key={option}
          type="button"
          className={`inline-flex min-h-12 items-center justify-center border-2 px-5 py-2 text-lg font-semibold transition ${
            value === option
              ? 'border-sky-500 bg-sky-500 text-white'
              : 'border-slate-300 bg-white text-slate-950 hover:border-sky-500'
          }`}
          onClick={() => onChange(option)}
        >
          {translate(`flights.tripType.${option}`)}
        </button>
      ))}
    </div>
  )
}

// 让外部文件可以继续复用 TripType 的类型定义。
export type { TripType }
