import type { AttractionKeywordInputProps } from '../../objects'

// 景点搜索的关键字输入框，只负责输入文本。
export function AttractionKeywordInput({ value, translate, onChange }: AttractionKeywordInputProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('attractions.keyword')}</span>
      <input value={value} placeholder={translate('attractions.keywordPlaceholder')} onChange={event => onChange(event.target.value)} />
    </label>
  )
}
