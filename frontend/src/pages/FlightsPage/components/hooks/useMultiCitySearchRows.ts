import type { FlightSearchSegment } from '@/app/stores/models/flights/flightTypes'


// 多程搜索行里单个字段的可编辑范围，只允许这四个输入项。
type SegmentField = 'departureAirport' | 'arrivalAirport' | 'departureDate' | 'arrivalDate'

// 多程搜索行的联动逻辑，负责把相邻航段的机场字段同步好。
export function useMultiCitySearchRows({
  segments,
  onSegmentChange,
}: {
  segments: FlightSearchSegment[]
  onSegmentChange: (segmentId: string, key: SegmentField, value: string) => void
}) {
  function updateSegment(segmentId: string, key: SegmentField, value: string) {
    const segmentIndex = segments.findIndex(segment => segment.id === segmentId)
    onSegmentChange(segmentId, key, value)

    if (segmentIndex < 0) {
      return
    }

    if (key === 'arrivalAirport') {
      const nextSegment = segments[segmentIndex + 1]
      if (nextSegment) {
        onSegmentChange(nextSegment.id, 'departureAirport', value)
      }
    }

    if (key === 'departureAirport') {
      const previousSegment = segments[segmentIndex - 1]
      if (previousSegment) {
        onSegmentChange(previousSegment.id, 'arrivalAirport', value)
      }
    }
  }

  return { updateSegment }
}

