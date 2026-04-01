import { useEffect, useState } from 'react'

import type { AppLanguage, BlogPostResponse, BlogPostSummaryResponse, ContentImageResponse, UserResponse } from '../lib/mvp-types'
import { BlogDetail } from './BlogDetail'
import { BlogEditor } from './BlogEditor'
import { formatBlogMeta, localizeBlogScope } from '../lib/content-presenter'

type BlogScope = 'latest' | 'mine'

type BlogPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onListPosts: (scope: BlogScope, query?: string) => Promise<BlogPostSummaryResponse[]>
  onLoadPost: (postId: string) => Promise<BlogPostResponse>
  onUploadImage: (imageFile: File) => Promise<ContentImageResponse>
  onCreatePost: (payload: { title: string; summary: string; content: string; images: ContentImageResponse[] }) => Promise<BlogPostResponse>
  onUpdatePost: (postId: string, payload: { title: string; summary: string; content: string; images: ContentImageResponse[] }) => Promise<BlogPostResponse>
  onArchivePost: (postId: string) => Promise<BlogPostResponse>
  onCommentPost: (postId: string, content: string) => Promise<BlogPostResponse>
  onDeleteComment: (commentId: string) => Promise<BlogPostResponse>
  onLikePost: (postId: string) => Promise<BlogPostResponse>
  onUnlikePost: (postId: string) => Promise<BlogPostResponse>
}

export function BlogPanel({
  currentLanguage,
  isBusy,
  signedInUser,
  translate,
  onListPosts,
  onLoadPost,
  onUploadImage,
  onCreatePost,
  onUpdatePost,
  onArchivePost,
  onCommentPost,
  onDeleteComment,
  onLikePost,
  onUnlikePost,
}: BlogPanelProps) {
  const [scope, setScope] = useState<BlogScope>('latest')
  const [searchText, setSearchText] = useState('')
  const [postSummaries, setPostSummaries] = useState<BlogPostSummaryResponse[]>([])
  const [selectedPost, setSelectedPost] = useState<BlogPostResponse | null>(null)
  const [editingPost, setEditingPost] = useState<BlogPostResponse | null>(null)

  useEffect(() => {
    const nextScope = signedInUser ? scope : 'latest'
    if (!signedInUser && scope !== 'latest') {
      setScope('latest')
    }
    void reloadPosts(nextScope)
  }, [signedInUser?.userId, scope, searchText])

  async function reloadPosts(nextScope: BlogScope = scope, nextSelectedPostId?: string) {
    const nextPosts = await onListPosts(nextScope, searchText)
    setPostSummaries(nextPosts)
    const selectedPostId = nextSelectedPostId ?? selectedPost?.post.postId ?? nextPosts[0]?.postId
    if (selectedPostId) {
      const detailedPost = await onLoadPost(selectedPostId)
      setSelectedPost(detailedPost)
      setEditingPost(currentEditingPost => (currentEditingPost?.post.postId === detailedPost.post.postId ? detailedPost : currentEditingPost))
    } else {
      setSelectedPost(null)
      setEditingPost(null)
    }
  }

  return (
    <section className="content-grid blog-layout">
      <div className="page-card">
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('nav.blog')}</p>
            <h2>{translate('blog.title')}</h2>
          </div>
          <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void reloadPosts()}>
            {translate('blog.refresh')}
          </button>
        </div>

        <p className="hero-copy">{translate('blog.description')}</p>

        <div className="manager-task-actions">
          <button type="button" className={scope === 'latest' ? '' : 'secondary-button'} onClick={() => setScope('latest')}>
            {localizeBlogScope('latest', currentLanguage)}
          </button>
          {signedInUser ? (
            <button type="button" className={scope === 'mine' ? '' : 'secondary-button'} onClick={() => setScope('mine')}>
              {localizeBlogScope('mine', currentLanguage)}
            </button>
          ) : null}
        </div>

        <label>
          {translate('blog.search')}
          <input
            value={searchText}
            onChange={event => setSearchText(event.target.value)}
            placeholder={translate('blog.searchHint')}
          />
        </label>

        {signedInUser ? (
          <BlogEditor
            isBusy={isBusy}
            translate={translate}
            onUploadImage={onUploadImage}
            onSubmit={async payload => {
              const created = await onCreatePost(payload)
              setEditingPost(null)
              await reloadPosts(scope, created.post.postId)
            }}
          />
        ) : (
          <p className="empty-state">{translate('blog.guestHint')}</p>
        )}

        <div className="list-surface">
          {postSummaries.length === 0 ? <p className="empty-state">{translate(scope === 'mine' ? 'blog.mineEmpty' : 'blog.empty')}</p> : null}

          {postSummaries.length > 0 ? (
            <ul className="entity-list">
              {postSummaries.map(post => (
                <li key={post.postId}>
                  <button type="button" className="tour-group-link-button" onClick={() => void reloadPosts(scope, post.postId)}>
                    <strong>{post.title}</strong>
                  </button>
                  <p>{post.summary}</p>
                  {post.searchResultSnippet ? <p>{post.searchResultSnippet}</p> : null}
                  <p>{`${post.authorDisplayName} · ${formatBlogMeta(post, translate('booking.notYet'))}`}</p>
                  <p>{`${translate('blog.likes')}: ${post.likeCount} · ${translate('blog.comments')}: ${post.commentCount}`}</p>
                </li>
              ))}
            </ul>
          ) : null}
        </div>
      </div>

      {editingPost ? (
        <BlogEditor
          isBusy={isBusy}
          mode="edit"
          initialValue={{
            title: editingPost.post.title,
            summary: editingPost.post.summary,
            content: editingPost.content,
            images: editingPost.post.images,
          }}
          translate={translate}
          onUploadImage={onUploadImage}
          onSubmit={async payload => {
            const updated = await onUpdatePost(editingPost.post.postId, payload)
            setEditingPost(null)
            setSelectedPost(updated)
            await reloadPosts(scope, updated.post.postId)
          }}
          onCancel={() => setEditingPost(null)}
        />
      ) : selectedPost ? (
        <BlogDetail
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          isGuestMode={signedInUser === null}
          post={selectedPost}
          translate={translate}
          onComment={async content => {
            const updated = await onCommentPost(selectedPost.post.postId, content)
            setSelectedPost(updated)
            setPostSummaries(currentPosts =>
              currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
            )
          }}
          onDeleteComment={async commentId => {
            const updated = await onDeleteComment(commentId)
            setSelectedPost(updated)
            setPostSummaries(currentPosts =>
              currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
            )
          }}
          onLike={async () => {
            const updated = await onLikePost(selectedPost.post.postId)
            setSelectedPost(updated)
            setPostSummaries(currentPosts =>
              currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
            )
          }}
          onUnlike={async () => {
            const updated = await onUnlikePost(selectedPost.post.postId)
            setSelectedPost(updated)
            setPostSummaries(currentPosts =>
              currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
            )
          }}
          onStartEdit={() => setEditingPost(selectedPost)}
          onArchive={async () => {
            const updated = await onArchivePost(selectedPost.post.postId)
            setSelectedPost(updated)
            await reloadPosts(scope, updated.post.postId)
          }}
        />
      ) : (
        <section className="page-card">
          <p className="empty-state">{translate('blog.empty')}</p>
        </section>
      )}
    </section>
  )
}
