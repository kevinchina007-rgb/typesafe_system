// ReviewDeletedPlannerResponse：content 域删除评论返回对象。

export type ReviewDeletedPlannerResponse = {
  deleted: boolean
}
export const reviewDeletedPlannerResponseFromJson = (json: string): ReviewDeletedPlannerResponse =>
  JSON.parse(json) as ReviewDeletedPlannerResponse
export const reviewDeletedPlannerResponseToJson = (value: ReviewDeletedPlannerResponse): string =>
  JSON.stringify(value)
