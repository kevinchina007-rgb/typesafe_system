import { attractionTypeOptions } from '@/app/stores/models/attraction-booking-model'
import type { AttractionTypeSelectorProps } from '../../objects'

// 景点类型切换器，只负责展示分类按钮。
export function AttractionTypeSelector({ value, translate, onChange }: AttractionTypeSelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('attractions.attractionType')}</span>
      <select value={value} onChange={event => onChange(event.target.value as AttractionTypeSelectorProps['value'])}>
        {attractionTypeOptions.map(option => (
          <option key={option} value={option}>
            {translate(`attractions.attractionType.${option}`)}
          </option>
        ))}
      </select>
    </label>
  )
}
