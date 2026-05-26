import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

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
      <div className="grid gap-2">
        <div className="grid gap-2">
          <strong className="m-0 text-xl font-bold text-slate-950">{displayTitle}</strong>
          <p className="text-sm leading-6 text-slate-600">{displaySummary}</p>
        </div>
        {badge ? <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{badge}</span> : null}
      </div>

      {snippet ? <p className="text-sm leading-6 text-slate-500">{snippet}</p> : null}

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
