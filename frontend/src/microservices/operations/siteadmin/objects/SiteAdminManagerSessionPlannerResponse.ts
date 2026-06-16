// 本文件定义 siteadmin 子域自己的管理员会话返回体。
// 它对应 RegisterSiteAdminPlanner / UpdateSiteAdminManagerProfilePlanner 的返回数据，不再借用 overall 子域。
export type SiteAdminManagerSessionPlannerResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  logoAssetPath?: string | null
  createdAt: string
}

export const siteAdminManagerSessionPlannerResponseFromJson = (json: string): SiteAdminManagerSessionPlannerResponse =>
  JSON.parse(json) as SiteAdminManagerSessionPlannerResponse

export const siteAdminManagerSessionPlannerResponseToJson = (value: SiteAdminManagerSessionPlannerResponse): string =>
  JSON.stringify(value)
