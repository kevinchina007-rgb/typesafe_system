import type { BlogPostSummaryResponse } from './BlogPostSummaryResponse'

export type BlogPostListResponse = {
  posts: BlogPostSummaryResponse[]
}
export const blogPostListResponseFromJson = (json: string): BlogPostListResponse =>
  JSON.parse(json) as BlogPostListResponse

export const blogPostListResponseToJson = (value: BlogPostListResponse): string =>
  JSON.stringify(value)
