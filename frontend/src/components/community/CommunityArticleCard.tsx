import { BackendAssetImage } from '../BackendAssetImage'

type CommunityArticleCardProps = {
  title: string
  summary: string
  author: string
  authorAvatarUrl?: string | null
  time: string
  likes: number
  comments: number
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
  return (
    <button
      type="button"
      className={isActive ? 'community-article-card is-active' : 'community-article-card'}
      onClick={onSelect}
    >
      <div className="community-article-head">
        <div className="community-article-copy">
          <strong className="community-article-title">{title}</strong>
          <p className="community-article-summary">{summary}</p>
        </div>
        {badge ? <span className="tag-chip">{badge}</span> : null}
      </div>

      {snippet ? <p className="community-article-snippet">{snippet}</p> : null}

      <div className="community-article-meta">
        <span className="community-inline-identity">
          <BackendAssetImage
            className="community-inline-avatar"
            assetUrl={authorAvatarUrl}
            alt={author}
            fallbackContent={author.slice(0, 1).toUpperCase()}
          />
          <span>{author}</span>
        </span>
        <span>{time}</span>
        <span>{`${likesLabel} ${likes}`}</span>
        <span>{`${commentsLabel} ${comments}`}</span>
      </div>
    </button>
  )
}
