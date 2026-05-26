import { useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, BlogPostResponse, BlogPostSummaryResponse, ContentImageResponse, UserResponse } from '@/lib/mvp-types/index'
import { BlogDetail } from '@/pages/BlogPage/components/BlogDetail'
import { BlogEditor } from '@/pages/BlogPage/components/BlogEditor'
import { CommunityArticleCard } from '@/pages/shared/community/CommunityArticleCard'
import { CommunityHero } from '@/pages/shared/community/CommunityHero'
import { formatBlogMeta, localizeBlogStatus } from '@/lib/presenters/content-presenter'

type BlogScope = 'latest' | 'mine'
type CommunityFeedTab = 'latest' | 'hot' | 'recommended' | 'mine'

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

function scoreRecommended(post: BlogPostSummaryResponse) {
  return (post.likeCount ?? 0) * 2 + (post.commentCount ?? 0) + ((post.images ?? []).length > 0 ? 3 : 0)
}

function sortPosts(posts: BlogPostSummaryResponse[], tab: CommunityFeedTab) {
  const nextPosts = [...posts]
  if (tab === 'hot') {
    return nextPosts.sort((left, right) => (right.commentCount ?? 0) + (right.likeCount ?? 0) - ((left.commentCount ?? 0) + (left.likeCount ?? 0)))
  }
  if (tab === 'recommended') {
    return nextPosts.sort((left, right) => scoreRecommended(right) - scoreRecommended(left))
  }
  return nextPosts.sort((left, right) => (right.publishedAt ?? right.createdAt ?? '').localeCompare(left.publishedAt ?? left.createdAt ?? ''))
}

function hasPostId(post: BlogPostSummaryResponse | null | undefined): post is BlogPostSummaryResponse {
  return typeof post?.postId === 'string' && post.postId.length > 0
}

function getDetailedPostId(post: BlogPostResponse | null | undefined) {
  return post?.post?.postId
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
  const [activeTab, setActiveTab] = useState<CommunityFeedTab>('latest')
  const [searchDraft, setSearchDraft] = useState('')
  const [searchText, setSearchText] = useState('')
  const [rawPosts, setRawPosts] = useState<BlogPostSummaryResponse[]>([])
  const [searchSuggestions, setSearchSuggestions] = useState<Array<{ value: string; title: string; subtitle: string }>>([])
  const [selectedPost, setSelectedPost] = useState<BlogPostResponse | null>(null)
  const [editingPost, setEditingPost] = useState<BlogPostResponse | null>(null)
  const [isComposerOpen, setIsComposerOpen] = useState(false)

  const effectiveScope: BlogScope = activeTab === 'mine' ? 'mine' : 'latest'

  const visiblePosts = useMemo(() => sortPosts(rawPosts, activeTab), [activeTab, rawPosts])

  useEffect(() => {
    if (!signedInUser && activeTab === 'mine') {
      setActiveTab('latest')
    }
  }, [activeTab, signedInUser])

  useEffect(() => {
    void reloadPosts()
  }, [signedInUser?.userId, effectiveScope, searchText])

  useEffect(() => {
    let cancelled = false
    const normalizedDraft = searchDraft.trim()
    if (normalizedDraft.length < 2) {
      setSearchSuggestions([])
      return () => {
        cancelled = true
      }
    }

    const timeoutId = window.setTimeout(async () => {
      try {
        const response = await travelMvpApiClient.listBlogSuggestions(normalizedDraft)
        if (!cancelled) {
          setSearchSuggestions(response.suggestions ?? [])
        }
      } catch {
        if (!cancelled) {
          setSearchSuggestions([])
        }
      }
    }, 180)

    return () => {
      cancelled = true
      window.clearTimeout(timeoutId)
    }
  }, [searchDraft])

  async function reloadPosts(nextSelectedPostId?: string) {
    const nextPosts = (await onListPosts(effectiveScope, searchText)).filter(hasPostId)
    setRawPosts(nextPosts)
    const sortedPosts = sortPosts(nextPosts, activeTab)
    const selectedPostId = nextSelectedPostId ?? getDetailedPostId(selectedPost) ?? sortedPosts[0]?.postId
    if (!selectedPostId) {
      setSelectedPost(null)
      setEditingPost(null)
      return
    }
    let detailedPost: BlogPostResponse
    try {
      detailedPost = await onLoadPost(selectedPostId)
    } catch {
      setSelectedPost(null)
      setEditingPost(null)
      return
    }
    const detailedPostId = getDetailedPostId(detailedPost)
    if (!detailedPostId) {
      setSelectedPost(null)
      setEditingPost(null)
      return
    }
    setSelectedPost(detailedPost)
    setEditingPost(currentEditingPost => (getDetailedPostId(currentEditingPost) === detailedPostId ? detailedPost : currentEditingPost))
  }

  const tabs = [
    { key: 'latest', label: translate('community.tabs.latest') },
    { key: 'hot', label: translate('community.tabs.hot') },
    { key: 'recommended', label: translate('community.tabs.recommended') },
    ...(signedInUser ? [{ key: 'mine', label: translate('community.tabs.mine') }] : []),
  ]

  return (
    <section className="grid gap-5 grid gap-5">
      <CommunityHero
        eyebrow={translate('community.blogEyebrow')}
        title={translate('blog.title')}
        searchValue={searchDraft}
        searchButtonLabel={translate('search.confirm')}
        tabs={tabs}
        activeTab={activeTab}
        primaryActionLabel={signedInUser ? translate('blog.publish') : undefined}
        secondaryActionLabel={translate('blog.refresh')}
        isBusy={isBusy}
        onSearchChange={setSearchDraft}
        onSearchSubmit={() => setSearchText(searchDraft)}
        onSelectTab={tabKey => setActiveTab(tabKey as CommunityFeedTab)}
        onPrimaryAction={signedInUser ? () => setIsComposerOpen(true) : undefined}
        onSecondaryAction={() => void reloadPosts()}
      />

      {searchSuggestions.length > 0 ? (
        <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 ">
          <div className="grid gap-3">
            {searchSuggestions.map(suggestion => (
              <button
                key={`${suggestion.value}-${suggestion.title}`}
                type="button"
                className="grid gap-2 border border-slate-200 bg-white p-4 text-left"
                onClick={() => {
                  setSearchDraft(suggestion.value)
                  setSearchText(suggestion.value)
                }}
              >
                <strong>{suggestion.title}</strong>
                <span>{suggestion.subtitle}</span>
              </button>
            ))}
          </div>
        </section>
      ) : null}

      {!signedInUser ? <p className="text-sm leading-6 text-slate-500">{translate('blog.guestHint')}</p> : null}

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('community.listEyebrow')}</p>
            <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('community.blogListTitle')}</h2>
          </div>
        </div>

        {visiblePosts.length === 0 ? (
          <div className="grid place-items-center gap-3 border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
            <p className="text-sm leading-6 text-slate-500">{translate(activeTab === 'mine' ? 'blog.mineEmpty' : 'blog.empty')}</p>
          </div>
        ) : (
          <div className="grid gap-4 lg:grid-cols-2">
            {visiblePosts.map(post => (
              <CommunityArticleCard
                key={post.postId}
                title={post.title}
                summary={post.summary}
                author={post.authorDisplayName}
                authorAvatarUrl={post.authorAvatarUrl}
                time={formatBlogMeta(post, translate('booking.notYet'))}
                likes={post.likeCount}
                comments={post.commentCount}
                likesLabel={translate('blog.likes')}
                commentsLabel={translate('blog.comments')}
                badge={localizeBlogStatus(post.status, currentLanguage)}
                snippet={post.searchResultSnippet}
                isActive={selectedPost?.post.postId === post.postId}
                onSelect={() => void reloadPosts(post.postId)}
              />
            ))}
          </div>
        )}
      </section>

      {signedInUser && isComposerOpen ? (
        <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-sm font-bold text-slate-500">{translate('community.detailEyebrow')}</p>
              <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('blog.editorTitle')}</h2>
            </div>
          </div>
          <BlogEditor
            isBusy={isBusy}
            translate={translate}
            onUploadImage={onUploadImage}
            onSubmit={async payload => {
              const created = await onCreatePost(payload)
              setIsComposerOpen(false)
              setEditingPost(null)
              await reloadPosts(created.post.postId)
            }}
            onCancel={() => setIsComposerOpen(false)}
          />
        </section>
      ) : editingPost ? (
        <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-sm font-bold text-slate-500">{translate('community.detailEyebrow')}</p>
              <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('blog.editorEditTitle')}</h2>
            </div>
          </div>
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
              await reloadPosts(updated.post.postId)
            }}
            onCancel={() => setEditingPost(null)}
          />
        </section>
      ) : selectedPost ? (
        <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-sm font-bold text-slate-500">{translate('community.detailEyebrow')}</p>
              <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('community.blogDetailTitle')}</h2>
            </div>
          </div>
          <BlogDetail
            currentLanguage={currentLanguage}
            isBusy={isBusy}
            isGuestMode={signedInUser === null}
            post={selectedPost}
            translate={translate}
            onComment={async content => {
              const updated = await onCommentPost(selectedPost.post.postId, content)
              setSelectedPost(updated)
              setRawPosts(currentPosts =>
                currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
              )
            }}
            onDeleteComment={async commentId => {
              const updated = await onDeleteComment(commentId)
              setSelectedPost(updated)
              setRawPosts(currentPosts =>
                currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
              )
            }}
            onLike={async () => {
              const updated = await onLikePost(selectedPost.post.postId)
              setSelectedPost(updated)
              setRawPosts(currentPosts =>
                currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
              )
            }}
            onUnlike={async () => {
              const updated = await onUnlikePost(selectedPost.post.postId)
              setSelectedPost(updated)
              setRawPosts(currentPosts =>
                currentPosts.map(postSummary => (postSummary.postId === updated.post.postId ? updated.post : postSummary)),
              )
            }}
            onStartEdit={() => setEditingPost(selectedPost)}
            onArchive={async () => {
              const updated = await onArchivePost(selectedPost.post.postId)
              setSelectedPost(updated)
              await reloadPosts(updated.post.postId)
            }}
          />
        </section>
      ) : null}
    </section>
  )
}
