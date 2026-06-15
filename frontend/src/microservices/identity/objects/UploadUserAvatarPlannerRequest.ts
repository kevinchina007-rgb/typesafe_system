// 本文件定义 identity 上传头像接口对应的请求对象，只包含用户标识和头像公共地址，并提供 JSON 编解码。
export type UploadUserAvatarPlannerRequest = {
  userId: string
  publicUrl: string
}

export const uploadUserAvatarPlannerRequestFromJson = (json: string): UploadUserAvatarPlannerRequest =>
  JSON.parse(json) as UploadUserAvatarPlannerRequest

export const uploadUserAvatarPlannerRequestToJson = (value: UploadUserAvatarPlannerRequest): string =>
  JSON.stringify(value)
