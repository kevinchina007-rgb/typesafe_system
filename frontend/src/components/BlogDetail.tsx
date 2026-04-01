import { useState } from 'react'

import type { AppLanguage, BlogPostResponse } from '../lib/mvp-types'
import { formatBlogMeta, localizeBlogStatus } from '../lib/content-presenter'
import { formatIsoDateTime } from '../lib/view-models'
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
    <article className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('blog.detailEyebrow')}</p>
          <h2>{post.post.title}</h2>
          <p>{`${post.post.authorDisplayName} · ${formatBlogMeta(post.post, translate('booking.notYet'))}`}</p>
        </div>
        <div className="compact-action-block">
          <span className="tag-chip">{localizeBlogStatus(post.post.status, currentLanguage)}</span>
          {post.post.canEdit ? (
            <button type="button" className="secondary-button" disabled={isBusy} onClick={onStartEdit}>
              {translate('blog.edit')}
            </button>
          ) : null}
          {post.post.canArchive ? (
            <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onArchive()}>
              {translate('blog.archive')}
            </button>
          ) : null}
        </div>
      </div>

      <p className="hero-copy">{post.post.summary}</p>
      <ContentImageGallery images={post.post.images} />
      <div className="panel-card blog-content-body">
        {post.content.split('\n').map((line, index) => (
          <p key={`${index}-${line}`}>{line}</p>
        ))}
      </div>

      <div className="manager-task-actions">
        <span className="tag-chip">{`${translate('blog.likes')}: ${post.post.likeCount}`}</span>
        <span className="tag-chip">{`${translate('blog.comments')}: ${post.post.commentCount}`}</span>
        {isGuestMode ? (
          <span className="detail-label">{translate('blog.guestHint')}</span>
        ) : (
          <button type="button" disabled={isBusy} onClick={() => void (liked ? onUnlike() : onLike())}>
            {liked ? translate('blog.unlike') : translate('blog.like')}
          </button>
        )}
      </div>

      <section className="panel-card">
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('blog.commentsEyebrow')}</p>
            <h3>{translate('blog.comments')}</h3>
          </div>
        </div>

        {post.comments.length === 0 ? <p className="empty-state">{translate('blog.commentsEmpty')}</p> : null}

        {post.comments.length > 0 ? (
          <ul className="entity-list">
            {post.comments.map(comment => (
              <li key={comment.commentId}>
                <div>
                  <strong>{comment.authorDisplayName}</strong>
                  <p>{comment.content}</p>
                  <p>{formatIsoDateTime(comment.createdAt, translate('booking.notYet'))}</p>
                </div>
                <div className="compact-action-block">
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
              </li>
            ))}
          </ul>
        ) : null}

        {!isGuestMode ? (
          <form
            className="stack-form"
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
