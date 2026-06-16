// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerRefundTaskListResponse`锛屼綔涓哄垪琛ㄥ搷搴旀暟鎹苟鎻愪緵 JSON 缂栬В鐮併€?

import type { ManagerRefundTaskResponse } from './ManagerRefundTaskResponse'

export type ManagerRefundTaskListResponse = {
  tasks: ManagerRefundTaskResponse[]
}
export const managerRefundTaskListResponseFromJson = (json: string): ManagerRefundTaskListResponse =>
  JSON.parse(json) as ManagerRefundTaskListResponse

export const managerRefundTaskListResponseToJson = (value: ManagerRefundTaskListResponse): string =>
  JSON.stringify(value)

