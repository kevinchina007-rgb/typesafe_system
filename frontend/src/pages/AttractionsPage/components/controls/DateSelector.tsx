import type { DateSelectorProps } from '../../objects'

// 景点搜索的日期选择器，只负责日期输入。
export function DateSelector({ value, translate, onChange }: DateSelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('attractions.useDate')}</span>
      <input type="date" value={value} onChange={event => onChange(event.target.value)} />
    </label>
  )
}
