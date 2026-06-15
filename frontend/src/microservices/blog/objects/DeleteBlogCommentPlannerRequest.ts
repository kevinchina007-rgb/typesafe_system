// DeleteBlogCommentPlannerRequest：博客域删除博客评论请求对象。

export type DeleteBlogCommentPlannerRequest = {
  commentId: string
  userId: string
}

export const deleteBlogCommentPlannerRequestFromJson = (json: string): DeleteBlogCommentPlannerRequest =>
  JSON.parse(json) as DeleteBlogCommentPlannerRequest

export const deleteBlogCommentPlannerRequestToJson = (value: DeleteBlogCommentPlannerRequest): string =>
  JSON.stringify(value)