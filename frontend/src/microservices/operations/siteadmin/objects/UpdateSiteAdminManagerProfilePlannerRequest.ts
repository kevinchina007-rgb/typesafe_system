// 本文件定义 UpdateSiteAdminManagerProfilePlanner 的站点管理员资料更新请求。

export type UpdateSiteAdminManagerProfilePlannerRequest = {
  managerId: string
  displayName: string
  logoAssetPath?: string | null
}

export const updateSiteAdminManagerProfilePlannerRequestFromJson = (json: string): UpdateSiteAdminManagerProfilePlannerRequest =>
  JSON.parse(json) as UpdateSiteAdminManagerProfilePlannerRequest

export const updateSiteAdminManagerProfilePlannerRequestToJson = (value: UpdateSiteAdminManagerProfilePlannerRequest): string =>
  JSON.stringify(value)
