import type { ForwardedRef } from 'react'
import { forwardRef } from 'react'

import type { HotelSearchNotice as HotelSearchNoticeState, HotelSearchNoticeProps } from '@/pages/HotelsPage/objects'

// 根据提示类型返回对应样式。
function getNoticeClassName(notice: HotelSearchNoticeState) {
  return notice.kind === 'error'
    ? 'border border-rose-200 bg-rose-50 text-rose-700'
    : 'border border-amber-200 bg-amber-50 text-amber-800'
}

// 酒店搜索提示区，只有存在提示时才渲染。
export const HotelSearchNotice = forwardRef(function HotelSearchNoticeView(
  { notice }: HotelSearchNoticeProps,
  ref: ForwardedRef<HTMLDivElement>,
) {
  if (!notice) {
    return null
  }

  return (
    <div ref={ref} className={`px-4 py-3 text-sm leading-6 ${getNoticeClassName(notice)}`}>
      {notice.message}
    </div>
  )
})
