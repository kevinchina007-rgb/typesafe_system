import type { AppLanguage, BlogPostSummaryResponse, ResourceReviewSummaryResponse, ReviewResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime } from '@/lib/presenters/view-models'

export function localizeBlogStatus(status: string, language: AppLanguage): string {
  void language
  const labels: Record<string, string> = {
    Published: '已发布',
    Draft: '草稿',
    Archived: '已归档',
  }
  return labels[status] ?? status
}

export function localizeReviewStatus(status: string, language: AppLanguage): string {
  void language
  const labels: Record<string, string> = {
    Published: '已发布',
    Deleted: '已删除',
  }
  return labels[status] ?? status
}

export function localizeReviewResourceType(resourceType: string, language: AppLanguage): string {
  void language
  const labels: Record<string, string> = {
    Flight: '航班',
    Hotel: '酒店',
    Train: '火车',
    Attraction: '景点',
  }
  return labels[resourceType] ?? resourceType
}

export function localizeBlogScope(scope: 'latest' | 'mine', language: AppLanguage): string {
  void language
  return scope === 'mine' ? '我的文章' : '最新文章'
}

export function formatBlogMeta(post: BlogPostSummaryResponse, fallbackLabel: string): string {
  return formatIsoDateTime(post.publishedAt ?? post.createdAt, fallbackLabel)
}

export function formatReviewMeta(review: ReviewResponse, fallbackLabel: string): string {
  return formatIsoDateTime(review.updatedAt || review.createdAt, fallbackLabel)
}

export function summarizeRating(rating: number): string {
  return '★'.repeat(Math.max(0, Math.min(5, rating)))
}

export function summarizeReviewAggregate(summary: ResourceReviewSummaryResponse, language: AppLanguage): string {
  void language
  if (summary.reviewCount === 0) {
    return '暂无评价'
  }
  return `评分 ${summary.averageRating} / 5，共 ${summary.reviewCount} 条评价`
}
