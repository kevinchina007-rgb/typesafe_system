import type { AppLanguage, BlogPostSummaryResponse, ResourceReviewSummaryResponse, ReviewResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime } from '@/lib/presenters/view-models'

// 杩欎竴灞傚彧璐熻矗鎶婂悗绔繑鍥炵殑鏁版嵁鏁寸悊鎴愭洿閫傚悎闃呰鐨勬枃妗堛€?
// 瀹冧笉鍋氱綉缁?IO锛屼篃涓嶄繚瀛樼姸鎬侊紝灞炰簬绾睍绀?helper銆?
export function localizeBlogStatus(status: string, language: AppLanguage): string {
  const labels =
    language === 'zh'
      ? {
          Published: '???',
          Draft: '鑽夌',
          Archived: '???',
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
          Published: '???',
          Deleted: '???',
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
          Flight: '鑸彮',
          Hotel: '閰掑簵',
          Train: '鐏溅',
          Attraction: '鏅偣',
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
    return scope === 'mine' ? '????' : '??'
  }
  return scope === 'mine' ? 'My posts' : 'Latest'
}

export function formatBlogMeta(post: BlogPostSummaryResponse, fallbackLabel: string): string {
  // 鍚庣缁欑殑鏄師濮嬫椂闂村瓧娈碉紝杩欓噷鍐冲畾鍒楄〃閲屼紭鍏堟樉绀哄彂甯冩椂闂磋繕鏄垱寤烘椂闂淬€?
  return formatIsoDateTime(post.publishedAt ?? post.createdAt, fallbackLabel)
}

export function formatReviewMeta(review: ReviewResponse, fallbackLabel: string): string {
  return formatIsoDateTime(review.updatedAt || review.createdAt, fallbackLabel)
}

export function summarizeRating(rating: number): string {
  return '?'.repeat(Math.max(0, Math.min(5, rating)))
}

export function summarizeReviewAggregate(summary: ResourceReviewSummaryResponse, language: AppLanguage): string {
  // averageRating / reviewCount 鏄悗绔仛鍚堢粨鏋滐紝杩欓噷鍙妸瀹冭浆鎴愪竴鏉＄敤鎴峰彲璇绘憳瑕併€?
  if (summary.reviewCount === 0) {
    return language === 'zh' ? '鏆傛棤璇勪环' : 'No reviews yet'
  }
  return language === 'zh'
    ? `?? ${summary.averageRating} / 5 ? ${summary.reviewCount} ???`
    : `${summary.averageRating} / 5 路 ${summary.reviewCount} reviews`
}
