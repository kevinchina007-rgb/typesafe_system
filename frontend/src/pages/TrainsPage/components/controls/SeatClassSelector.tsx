// 火车座位偏好选择器参数，只负责座位类型下拉框。
import { trainSeatPreferences, type TrainSeatPreference } from '@/app/stores/models/train-booking-model'

type SeatClassSelectorProps = {
  value: TrainSeatPreference
  translate: (translationKey: string) => string
  onChange: (value: TrainSeatPreference) => void
}

// 座位偏好选择器，把枚举值映射成页面文案。
export function SeatClassSelector({ value, translate, onChange }: SeatClassSelectorProps) {
  const translationKeyByOption: Record<TrainSeatPreference, string> = {
    Business: 'trains.seatClass.business',
    FirstClass: 'trains.seatClass.first',
    SecondClass: 'trains.seatClass.second',
    SoftSleeper: 'trains.seatClass.softSleeper',
    HardSleeper: 'trains.seatClass.hardSleeper',
    NoSeat: 'trains.seatClass.noSeat',
  }

  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('trains.seatPreference')}</span>
      <select value={value} onChange={event => onChange(event.target.value as TrainSeatPreference)}>
        {trainSeatPreferences.map(option => (
          <option key={option} value={option}>
            {translate(translationKeyByOption[option])}
          </option>
        ))}
      </select>
    </label>
  )
}
