// ContentImagePlannerResponse：content 域评论图片上传返回对象。

export type ContentImagePlannerResponse = {
  imageId: string
  publicUrl: string
  originalFileName: string
  sortOrder: number
  createdAt: string
}

export const contentImagePlannerResponseFromJson = (json: string): ContentImagePlannerResponse =>
  JSON.parse(json) as ContentImagePlannerResponse

export const contentImagePlannerResponseToJson = (value: ContentImagePlannerResponse): string =>
  JSON.stringify(value)
