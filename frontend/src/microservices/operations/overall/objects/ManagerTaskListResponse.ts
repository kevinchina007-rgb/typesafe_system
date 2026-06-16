// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerTaskListResponse`锛屼綔涓哄垪琛ㄥ搷搴旀暟鎹苟鎻愪緵 JSON 缂栬В鐮併€?

import type { ManagerTaskResponse } from './ManagerTaskResponse'

export type ManagerTaskListResponse = {
  tasks: ManagerTaskResponse[]
}
export const managerTaskListResponseFromJson = (json: string): ManagerTaskListResponse =>
  JSON.parse(json) as ManagerTaskListResponse

export const managerTaskListResponseToJson = (value: ManagerTaskListResponse): string =>
  JSON.stringify(value)

