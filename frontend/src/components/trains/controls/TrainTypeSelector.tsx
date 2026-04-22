import { trainTypePreferences, type TrainTypePreference } from '../trainBookingModel'

type TrainTypeSelectorProps = {
  value: TrainTypePreference
  translate: (translationKey: string) => string
  onChange: (value: TrainTypePreference) => void
}

export function TrainTypeSelector({ value, translate, onChange }: TrainTypeSelectorProps) {
  const translationKeyByOption: Record<TrainTypePreference, string> = {
    HighSpeed: 'trains.trainType.highSpeed',
    Bullet: 'trains.trainType.emu',
    Regular: 'trains.trainType.regular',
  }

  return (
    <label className="resource-search-label resource-inline-field">
      <span>{translate('trains.trainTypePreference')}</span>
      <select value={value} onChange={event => onChange(event.target.value as TrainTypePreference)}>
        {trainTypePreferences.map(option => (
          <option key={option} value={option}>
            {translate(translationKeyByOption[option])}
          </option>
        ))}
      </select>
    </label>
  )
}
