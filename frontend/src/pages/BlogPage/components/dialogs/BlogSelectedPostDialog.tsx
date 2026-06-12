import { X } from 'lucide-react'

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { BlogPageController } from '../../objects'
import { buildFallbackInitials, formatShortDate } from '../../functions'

// 帖子详情弹窗参数，包含 controller、登录用户和关闭回调。
type BlogSelectedPostDialogProps = {
  controller: BlogPageController
  signedInUser: { userId: string } | null
  onClose: () => void
}

// 帖子详情弹窗，展示大图、正文、评论和互动操作。
export function BlogSelectedPostDialog({ controller, signedInUser, onClose }: BlogSelectedPostDialogProps) {
  const {
    selectedPost,
    selectedImage,
    selectedImages,
    selectedImageIndex,
    setSelectedImageIndex,
    commentDraft,
    setCommentDraft,
    submitComment,
    likePost,
    favoritePost,
    likeComment,
    openProfile,
    followedAuthors,
  } = controller

  // 没有选中帖子时不渲染弹窗。
  if (!selectedPost) return null

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/70 px-5 py-6">
      <button className="absolute right-8 top-8 grid h-12 w-12 place-items-center rounded-full bg-black/70 text-white hover:bg-white hover:text-black" type="button" onClick={onClose} title="关闭">
        <X className="h-7 w-7" aria-hidden="true" />
      </button>
      <section className="grid h-[88vh] w-full max-w-6xl grid-cols-[minmax(0,1.1fr)_minmax(22rem,0.9fr)] overflow-hidden rounded-[18px] bg-zinc-950 text-white shadow-2xl">
        <div className="relative grid place-items-center bg-black">
          {selectedImage ? (
            <BackendAssetImage className="max-h-full max-w-full object-contain" assetUrl={selectedImage.publicUrl} alt={selectedPost.post.title} fallbackContent={selectedPost.post.title} />
          ) : (
            <div className="text-slate-400">这篇帖子还没有图片</div>
          )}
          {selectedImages.length > 1 ? (
            <>
              <button className="absolute left-4 grid h-11 w-11 place-items-center rounded-full border border-white/30 bg-white text-3xl text-black hover:bg-black hover:text-white" type="button" onClick={() => setSelectedImageIndex(current => (current - 1 + selectedImages.length) % selectedImages.length)}>
                ←
              </button>
              <button className="absolute right-4 grid h-11 w-11 place-items-center rounded-full border border-white/30 bg-white text-3xl text-black hover:bg-black hover:text-white" type="button" onClick={() => setSelectedImageIndex(current => (current + 1) % selectedImages.length)}>
                →
              </button>
              <span className="absolute right-5 top-5 rounded-full bg-black/50 px-3 py-1 text-sm font-black">{selectedImageIndex + 1}/{selectedImages.length}</span>
            </>
          ) : null}
        </div>
        <div className="grid min-h-0 grid-rows-[auto_minmax(0,1fr)_auto] border-l border-white/10 bg-zinc-950">
          <header className="flex items-center gap-4 border-b border-white/10 p-6">
            <button className="grid h-14 w-14 place-items-center overflow-hidden rounded-full border border-white/10 bg-zinc-800 text-xl font-black hover:border-rose-300" type="button" onClick={() => void openProfile(selectedPost.post.authorUserId)} title="查看主页">
              {selectedPost.post.authorAvatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={selectedPost.post.authorAvatarUrl} alt={selectedPost.post.authorDisplayName} fallbackContent={buildFallbackInitials(selectedPost.post.authorDisplayName)} /> : buildFallbackInitials(selectedPost.post.authorDisplayName)}
            </button>
            <button className="min-w-0 flex-1 truncate text-left text-xl font-black hover:text-rose-200" type="button" onClick={() => void openProfile(selectedPost.post.authorUserId)}>
              {selectedPost.post.authorDisplayName}
            </button>
            {selectedPost.post.authorUserId !== signedInUser?.userId ? (
              <button
                className="rounded-full bg-rose-500 px-8 py-3 text-lg font-black text-white disabled:bg-zinc-700"
                type="button"
                disabled={followedAuthors.has(selectedPost.post.authorUserId)}
                onClick={() => void controller.followAuthor(selectedPost.post)}
              >
                {followedAuthors.has(selectedPost.post.authorUserId) ? '已关注' : '关注'}
              </button>
            ) : null}
          </header>
          <div className="min-h-0 overflow-y-auto p-6">
            <h2 className="text-2xl font-black">{selectedPost.post.title}</h2>
            <p className="mt-4 whitespace-pre-wrap leading-8 text-zinc-100">{selectedPost.content}</p>
            <div className="mt-4 flex flex-wrap gap-2">
              {(selectedPost.post.travelCities ?? []).map(city => <span key={city} className="rounded-full bg-zinc-800 px-3 py-1 text-sm text-zinc-200">{city}</span>)}
              {(selectedPost.post.tags ?? []).map(tag => <span key={`${tag.tagType}-${tag.tagValue}`} className="rounded-full bg-rose-500/20 px-3 py-1 text-sm text-rose-200">#{tag.tagValue}</span>)}
            </div>
            <p className="mt-4 text-sm text-zinc-500">{formatShortDate(selectedPost.post.publishedAt ?? selectedPost.post.createdAt)}</p>
            <div className="mt-6 border-t border-white/10 pt-5">
              <p className="mb-4 font-black text-zinc-300">共 {selectedPost.comments.length} 条评论</p>
              <div className="grid gap-5">
                {selectedPost.comments.map(comment => (
                  <article key={comment.commentId} className="grid grid-cols-[2.5rem_minmax(0,1fr)] gap-3">
                    <button className="grid h-10 w-10 place-items-center overflow-hidden rounded-full bg-zinc-800 text-sm font-black hover:ring-2 hover:ring-rose-300" type="button" onClick={() => void openProfile(comment.authorUserId)} title="查看主页">
                      {comment.authorAvatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={comment.authorAvatarUrl} alt={comment.authorDisplayName} fallbackContent={buildFallbackInitials(comment.authorDisplayName)} /> : buildFallbackInitials(comment.authorDisplayName)}
                    </button>
                    <div>
                      <div className="flex items-center gap-2 text-sm text-zinc-400">
                        <button className="font-black hover:text-rose-200" type="button" onClick={() => void openProfile(comment.authorUserId)}>
                          {comment.authorDisplayName}
                        </button>
                        {comment.authorUserId === selectedPost.post.authorUserId ? <span className="rounded bg-zinc-800 px-2 py-0.5">作者</span> : null}
                      </div>
                      <p className="mt-1 text-zinc-100">{comment.content}</p>
                      <p className="mt-2 flex items-center gap-3 text-sm text-zinc-500">
                        <span>{formatShortDate(comment.createdAt)}</span>
                        <button className="font-black hover:text-rose-200 disabled:text-zinc-600" type="button" disabled={!signedInUser} onClick={() => void likeComment(comment)}>
                          {comment.likedByCurrentUser ? '已赞' : '赞'} {comment.likeCount}
                        </button>
                      </p>
                    </div>
                  </article>
                ))}
                {selectedPost.comments.length === 0 ? <p className="text-zinc-500">还没有评论，来坐第一个板凳。</p> : null}
              </div>
            </div>
          </div>
          <footer className="grid gap-3 border-t border-white/10 p-5">
            <div className="flex items-center gap-3">
              <input className="min-h-11 min-w-0 flex-1 rounded-full border border-white/10 bg-zinc-900 px-5 text-white outline-none focus:border-rose-400" value={commentDraft} onChange={event => setCommentDraft(event.target.value)} placeholder="说点什么..." />
              <button className="rounded-full bg-zinc-800 px-5 py-3 font-black hover:bg-zinc-700" type="button" disabled={!commentDraft.trim()} onClick={() => void submitComment()}>
                评论
              </button>
            </div>
            <div className="flex items-center gap-5 text-lg">
              <button className="font-black hover:text-rose-300" type="button" onClick={() => void likePost(selectedPost.post)}>
                {selectedPost.post.likedByCurrentUser ? '♥' : '♡'} {selectedPost.post.likeCount}
              </button>
              <button className="font-black hover:text-rose-300" type="button" onClick={() => void favoritePost(selectedPost.post)}>
                {selectedPost.post.favoritedByCurrentUser ? '★' : '☆'} {selectedPost.post.favoriteCount}
              </button>
              <span className="font-black">评论 {selectedPost.post.commentCount}</span>
            </div>
          </footer>
        </div>
      </section>
    </div>
  )
}
