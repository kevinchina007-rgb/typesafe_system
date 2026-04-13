import { useState } from 'react'

import type { AppLanguage, BlogPostResponse } from '../lib/mvp-types'
import { formatBlogMeta, localizeBlogStatus } from '../lib/content-presenter'
import { formatIsoDateTime } from '../lib/view-models'
import { BackendAssetImage } from './BackendAssetImage'
import { ContentImageGallery } from './ContentImageGallery'

type BlogDetailProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  post: BlogPostResponse
  translate: (translationKey: string) => string
  onComment: (content: string) => Promise<void>
  onDeleteComment: (commentId: string) => Promise<void>
  onLike: () => Promise<void>
  onUnlike: () => Promise<void>
  onStartEdit: () => void
  onArchive: () => Promise<void>
}

export function BlogDetail({
  currentLanguage,
  isBusy,
  isGuestMode,
  post,
  translate,
  onComment,
  onDeleteComment,
  onLike,
  onUnlike,
  onStartEdit,
  onArchive,
}: BlogDetailProps) {
  const [commentDraft, setCommentDraft] = useState('')
  const liked = post.post.likedByCurrentUser

  return (
    <article className="page-stack">
      <section className="page-card community-detail-hero">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('blog.detailEyebrow')}</p>
            <h2 className="community-detail-title">{post.post.title}</h2>
            <div className="community-detail-meta">
              <span className="community-inline-identity">
                <BackendAssetImage
                  className="community-inline-avatar"
                  assetUrl={post.post.authorAvatarUrl}
                  alt={post.post.authorDisplayName}
                  fallbackContent={post.post.authorDisplayName.slice(0, 1).toUpperCase()}
                />
                <strong>{post.post.authorDisplayName}</strong>
              </span>
              <span>{formatBlogMeta(post.post, translate('booking.notYet'))}</span>
            </div>
          </div>
          <div className="action-row community-detail-actions">
            {post.post.canEdit ? (
              <button type="button" disabled={isBusy} onClick={onStartEdit}>
                {translate('blog.edit')}
              </button>
            ) : null}
            <span className="tag-chip">{localizeBlogStatus(post.post.status, currentLanguage)}</span>
            {post.post.canArchive ? (
              <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onArchive()}>
                {translate('blog.archive')}
              </button>
            ) : null}
          </div>
        </div>

        <p className="community-detail-summary">{post.post.summary}</p>

        <div className="community-detail-stats">
          <span className="tag-chip">{`${translate('blog.likes')}: ${post.post.likeCount}`}</span>
          <span className="tag-chip">{`${translate('blog.comments')}: ${post.post.commentCount}`}</span>
          {isGuestMode ? (
            <span className="detail-label">{translate('blog.guestHint')}</span>
          ) : (
            <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void (liked ? onUnlike() : onLike())}>
              {liked ? translate('blog.unlike') : translate('blog.like')}
            </button>
          )}
        </div>
      </section>

      <section className="page-card community-detail-body">
        <ContentImageGallery images={post.post.images} />
        <div className="panel-card blog-content-body">
          {post.content.split('\n').map((line, index) => (
            <p key={`${index}-${line}`}>{line}</p>
          ))}
        </div>
      </section>

      <section className="page-card community-comment-section">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('blog.commentsEyebrow')}</p>
            <h3>{translate('blog.comments')}</h3>
          </div>
        </div>

        {post.comments.length === 0 ? <p className="empty-state">{translate('blog.commentsEmpty')}</p> : null}

        {post.comments.length > 0 ? (
          <div className="community-comment-list">
            {post.comments.map(comment => (
              <article key={comment.commentId} className="list-card community-comment-card">
                <div className="community-comment-head">
                  <span className="community-inline-identity">
                    <BackendAssetImage
                      className="community-inline-avatar"
                      assetUrl={comment.authorAvatarUrl}
                      alt={comment.authorDisplayName}
                      fallbackContent={comment.authorDisplayName.slice(0, 1).toUpperCase()}
                    />
                    <strong>{comment.authorDisplayName}</strong>
                  </span>
                  <span>{formatIsoDateTime(comment.createdAt, translate('booking.notYet'))}</span>
                </div>
                <p>{comment.content}</p>
                <div className="action-row">
                  {comment.isMyComment ? <span className="tag-chip">{translate('blog.myComment')}</span> : null}
                  {comment.canDelete ? (
                    <button
                      type="button"
                      className="secondary-button"
                      disabled={isBusy}
                      onClick={() => void onDeleteComment(comment.commentId)}
                    >
                      {translate('blog.deleteComment')}
                    </button>
                  ) : null}
                </div>
              </article>
            ))}
          </div>
        ) : null}

        {!isGuestMode ? (
          <form
            className="stack-form community-comment-form"
            onSubmit={async event => {
              event.preventDefault()
              await onComment(commentDraft)
              setCommentDraft('')
            }}
          >
            <label>
              {translate('blog.addComment')}
              <textarea value={commentDraft} onChange={event => setCommentDraft(event.target.value)} disabled={isBusy} rows={3} />
            </label>
            <button type="submit" disabled={isBusy}>
              {translate('blog.submitComment')}
            </button>
          </form>
        ) : null}
      </section>
    </article>
  )
}
