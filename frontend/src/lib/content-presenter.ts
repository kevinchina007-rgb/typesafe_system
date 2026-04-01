import type { AppLanguage, BlogPostSummaryResponse, ResourceReviewSummaryResponse, ReviewResponse } from './mvp-types'
import { formatIsoDateTime } from './view-models'

export function localizeBlogStatus(status: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Published: '已发布',
          Draft: '草稿',
          Archived: '已归档',
        }
      : {
          Published: 'Published',
          Draft: 'Draft',
          Archived: 'Archived',
        }
  return labels[status as keyof typeof labels] ?? status
}

export function localizeReviewStatus(status: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Published: '已发布',
          Deleted: '已删除',
        }
      : {
          Published: 'Published',
          Deleted: 'Deleted',
        }
  return labels[status as keyof typeof labels] ?? status
}

export function localizeReviewResourceType(resourceType: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Flight: '航班',
          Hotel: '酒店',
          Train: '火车',
          Attraction: '景点',
        }
      : {
          Flight: 'Flight',
          Hotel: 'Hotel',
          Train: 'Train',
          Attraction: 'Attraction',
        }
  return labels[resourceType as keyof typeof labels] ?? resourceType
}

export function localizeBlogScope(scope: 'latest' | 'mine', language: AppLanguage): string {
  if (language === 'zh') {
    return scope === 'mine' ? '我的文章' : '最新'
  }
  return scope === 'mine' ? 'My posts' : 'Latest'
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
  if (summary.reviewCount === 0) {
    return language === 'zh' ? '暂无评价' : 'No reviews yet'
  }
  return language === 'zh'
    ? `评分 ${summary.averageRating} / 5 · ${summary.reviewCount} 条评价`
    : `${summary.averageRating} / 5 · ${summary.reviewCount} reviews`
}
