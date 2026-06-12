// 本文件定义共享页面组件或工具，负责页面间复用逻辑。

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

// 社区文章卡片的输入参数。
type CommunityArticleCardProps = {
  title: string | null | undefined
  summary: string | null | undefined
  author: string | null | undefined
  authorAvatarUrl?: string | null
  time: string
  likes: number | null | undefined
  comments: number | null | undefined
  likesLabel: string
  commentsLabel: string
  isActive?: boolean
  badge?: string
  snippet?: string | null
  onSelect: () => void
}

// 社区文章卡片负责展示标题、摘要、作者和互动统计。
export function CommunityArticleCard({
  title,
  summary,
  author,
  authorAvatarUrl,
  time,
  likes,
  comments,
  likesLabel,
  commentsLabel,
  isActive = false,
  badge,
  snippet,
  onSelect,
}: CommunityArticleCardProps) {
  // 列表展示文本都做空值兜底，避免卡片出现空白。
  const displayTitle = title?.trim() || '未命名文章'
  const displaySummary = summary?.trim() || '这篇文章暂时还没有摘要。'
  const displayAuthor = author?.trim() || '匿名用户'
  const authorInitial = displayAuthor.slice(0, 1).toUpperCase()
  const displayLikes = likes ?? 0
  const displayComments = comments ?? 0

  return (
    <button
      type="button"
      className={isActive ? 'grid gap-3 border border-slate-200 bg-white p-4 border-black bg-black text-white' : 'grid gap-3 border border-slate-200 bg-white p-4'}
      onClick={onSelect}
    >
      {/* 标题区展示文章标题和摘要。 */}
      <div className="grid gap-2">
        <div className="grid gap-2">
          <strong className="m-0 text-xl font-bold text-slate-950">{displayTitle}</strong>
          <p className="text-sm leading-6 text-slate-600">{displaySummary}</p>
        </div>
        {badge ? <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{badge}</span> : null}
      </div>

      {snippet ? <p className="text-sm leading-6 text-slate-500">{snippet}</p> : null}

      {/* 底部区域展示作者、时间和互动统计。 */}
      <div className="text-sm text-slate-500">
        <span className="inline-flex items-center gap-2">
          <BackendAssetImage
            className="h-8 w-8 object-cover"
            assetUrl={authorAvatarUrl}
            alt={displayAuthor}
            fallbackContent={authorInitial}
          />
          <span>{displayAuthor}</span>
        </span>
        <span>{time}</span>
        <span>{`${likesLabel} ${displayLikes}`}</span>
        <span>{`${commentsLabel} ${displayComments}`}</span>
      </div>
    </button>
  )
}
