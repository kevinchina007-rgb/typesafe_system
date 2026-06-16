// 本文件定义 attraction 子域自己的管理员会话返回体。
// 它对应 RegisterAttractionManagerPlanner 的返回数据，不再借用 overall 子域。
export type AttractionManagerSessionPlannerResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  logoAssetPath?: string | null
  createdAt: string
}

export const attractionManagerSessionPlannerResponseFromJson = (json: string): AttractionManagerSessionPlannerResponse =>
  JSON.parse(json) as AttractionManagerSessionPlannerResponse

export const attractionManagerSessionPlannerResponseToJson = (value: AttractionManagerSessionPlannerResponse): string =>
  JSON.stringify(value)
