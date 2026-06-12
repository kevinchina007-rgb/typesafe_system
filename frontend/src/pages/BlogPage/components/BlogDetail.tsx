import { useState } from 'react'

import type { AppLanguage, BlogPostResponse } from '@/lib/mvp-types/index'
import { formatBlogMeta, localizeBlogStatus } from '@/lib/presenters/content-presenter'
import { formatIsoDateTime } from '@/lib/presenters/view-models'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { ContentImageGallery } from '@/pages/shared/content/ContentImageGallery'

// Blog 详情页参数，覆盖正文、评论和操作按钮。
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

// Blog 详情组件，展示文章正文、图片和评论区。
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
  // 当前帖子是否已经点赞。
  const liked = post.post.likedByCurrentUser
  // 帖子标题兜底文本。
  const postTitle = post.post.title?.trim() || '未命名文章'
  // 帖子摘要兜底文本。
  const postSummary = post.post.summary?.trim() || '这篇文章暂时还没有摘要。'
  // 帖子正文内容。
  const postContent = post.content ?? ''
  // 帖子图片列表。
  const postImages = post.post.images ?? []
  // 帖子评论列表。
  const postComments = post.comments ?? []
  // 作者名称兜底文本。
  const postAuthorName = post.post.authorDisplayName?.trim() || '匿名用户'
  // 作者头像首字母兜底。
  const postAuthorInitial = postAuthorName.slice(0, 1).toUpperCase()

  return (
    <article className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('blog.detailEyebrow')}</p>
            <h2 className="m-0 text-3xl font-bold text-slate-950">{postTitle}</h2>
            <div className="flex flex-wrap items-center gap-3 text-sm text-slate-500">
              <span className="inline-flex items-center gap-2">
                <BackendAssetImage
                  className="h-8 w-8 object-cover"
                  assetUrl={post.post.authorAvatarUrl}
                  alt={postAuthorName}
                  fallbackContent={postAuthorInitial}
                />
                <strong>{postAuthorName}</strong>
              </span>
              <span>{formatBlogMeta(post.post, translate('booking.notYet'))}</span>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-3 flex flex-wrap items-center gap-3">
            {post.post.canEdit ? (
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={onStartEdit}>
                {translate('blog.edit')}
              </button>
            ) : null}
            <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{localizeBlogStatus(post.post.status, currentLanguage)}</span>
            {post.post.canArchive ? (
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => void onArchive()}>
                {translate('blog.archive')}
              </button>
            ) : null}
          </div>
        </div>

        <p className="text-base leading-7 text-slate-600">{postSummary}</p>

        <div className="flex flex-wrap items-center gap-3">
          <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{`${translate('blog.likes')}: ${post.post.likeCount ?? 0}`}</span>
          <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{`${translate('blog.comments')}: ${post.post.commentCount ?? 0}`}</span>
          {isGuestMode ? (
            <span className="text-sm font-medium text-slate-500">{translate('blog.guestHint')}</span>
          ) : (
            <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => void (liked ? onUnlike() : onLike())}>
              {liked ? translate('blog.unlike') : translate('blog.like')}
            </button>
          )}
        </div>
      </section>

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
        <ContentImageGallery images={postImages} />
        <div className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 prose max-w-none text-slate-700">
          {postContent.split('\n').map((line, index) => (
            <p key={`${index}-${line}`}>{line}</p>
          ))}
        </div>
      </section>

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('blog.commentsEyebrow')}</p>
            <h3>{translate('blog.comments')}</h3>
          </div>
        </div>

        {postComments.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('blog.commentsEmpty')}</p> : null}

        {postComments.length > 0 ? (
          <div className="grid gap-3">
            {postComments.map(comment => {
              const commentAuthorName = comment.authorDisplayName?.trim() || '匿名用户'
              const commentAuthorInitial = commentAuthorName.slice(0, 1).toUpperCase()
              return (
              <article key={comment.commentId} className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-3">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <span className="inline-flex items-center gap-2">
                    <BackendAssetImage
                      className="h-8 w-8 object-cover"
                      assetUrl={comment.authorAvatarUrl}
                      alt={commentAuthorName}
                      fallbackContent={commentAuthorInitial}
                    />
                    <strong>{commentAuthorName}</strong>
                  </span>
                  <span>{formatIsoDateTime(comment.createdAt, translate('booking.notYet'))}</span>
                </div>
                <p>{comment.content}</p>
                <div className="flex flex-wrap items-center gap-3">
                  {comment.isMyComment ? <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{translate('blog.myComment')}</span> : null}
                  {comment.canDelete ? (
                    <button
                      type="button"
                      className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      disabled={isBusy}
                      onClick={() => void onDeleteComment(comment.commentId)}
                    >
                      {translate('blog.deleteComment')}
                    </button>
                  ) : null}
                </div>
              </article>
              )
            })}
          </div>
        ) : null}

        {!isGuestMode ? (
          <form
            className="grid gap-4 grid gap-4"
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
            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
              {translate('blog.submitComment')}
            </button>
          </form>
        ) : null}
      </section>
    </article>
  )
}
