// 本文件定义出行人选择面板，负责展示可选旅客并回传当前选择结果。

import type { TravelerResponse } from '@/lib/mvp-types/index'

// 出行人选择面板的输入参数。
type TravelerSelectionPanelProps = {
  title: string
  hint: string
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  onToggleTravelerSelection: (travelerId: string) => void
  renderTravelerLabel: (traveler: TravelerResponse) => string
  emptySelectionMessage: string
}

// 出行人选择面板，负责展示可选出行人和当前选中状态。
export function TravelerSelectionPanel({
  title,
  hint,
  travelers,
  selectedTravelerIds,
  onToggleTravelerSelection,
  renderTravelerLabel,
  emptySelectionMessage,
}: TravelerSelectionPanelProps) {
  if (travelers.length === 0) {
    return null
  }

  return (
    <section className="grid gap-4 border border-slate-200 bg-white px-6 py-5">
      {/* 面板头部展示标题和辅助说明。 */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="m-0 text-2xl font-bold text-slate-950">{title}</h3>
        <p className="m-0 text-sm font-medium text-slate-500">{hint}</p>
      </div>

      {/* 中间区域按卡片形式列出所有出行人。 */}
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {travelers.map(traveler => {
          const isSelected = selectedTravelerIds.includes(traveler.travelerId)
          return (
            <label
              key={traveler.travelerId}
              className={`flex cursor-pointer items-center gap-4 border px-4 py-4 text-base font-semibold transition ${
                isSelected ? 'border-sky-500 bg-sky-50 text-slate-950' : 'border-slate-200 bg-white text-slate-600 hover:border-slate-400'
              }`}
            >
              <input type="checkbox" checked={isSelected} onChange={() => onToggleTravelerSelection(traveler.travelerId)} />
              <span className="inline-flex h-11 w-11 items-center justify-center border border-slate-300 bg-slate-50 text-lg font-black text-slate-700">
                {renderTravelerLabel(traveler).slice(0, 1)}
              </span>
              <span className="truncate">{renderTravelerLabel(traveler)}</span>
            </label>
          )
        })}
      </div>

      {/* 没有选中任何出行人时，底部给出提示。 */}
      {selectedTravelerIds.length === 0 ? <p className="m-0 text-sm font-medium text-rose-600">{emptySelectionMessage}</p> : null}
    </section>
  )
}
