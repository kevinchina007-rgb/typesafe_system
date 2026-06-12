import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { BlogPostSummaryResponse } from '@/microservices/content/objects/BlogPostSummaryResponse'

import {
  buildFallbackInitials,
  getAuthorDisplayName,
  getPostCities,
  getPostCoverImage,
  getPostCoverText,
  getPostTags,
} from '@/pages/BlogPage/functions'

// 单个 Blog 帖子卡片的参数定义。
type BlogPostCardProps = {
  post: BlogPostSummaryResponse
  compact?: boolean
  isActive?: boolean
  onOpenPost: (postId: string) => void
  onOpenProfile: (profileUserId: string) => void
  onLike: (post: BlogPostSummaryResponse) => void
}

// 帖子卡片，负责显示封面、作者、标签和点赞入口。
export function BlogPostCard({ post, compact = false, isActive = false, onOpenPost, onOpenProfile, onLike }: BlogPostCardProps) {
  // 帖子封面图地址。
  const coverImage = getPostCoverImage(post)
  // 帖子标签列表。
  const tags = getPostTags(post)
  // 帖子城市列表。
  const cities = getPostCities(post)

  return (
    <article className={`overflow-hidden border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md ${isActive ? 'ring-2 ring-pink-400' : ''}`}>
      <button className="block w-full text-left" type="button" onClick={() => onOpenPost(post.postId)}>
        <div className={compact ? 'aspect-[5/3] bg-slate-100' : 'aspect-[4/5] bg-slate-100'}>
          {coverImage ? (
            <BackendAssetImage className="h-full w-full object-cover" assetUrl={coverImage} alt={post.title} fallbackContent={null} />
          ) : (
            <span className="grid h-full w-full place-items-center bg-slate-100 text-4xl font-black text-slate-400">图</span>
          )}
        </div>
        <div className="grid gap-3 p-4">
          <h3 className="line-clamp-2 text-xl font-black">{post.title}</h3>
          <p className="line-clamp-2 text-slate-500">{getPostCoverText(post)}</p>
        </div>
      </button>
      <div className="grid gap-3 px-4 pb-4">
        <div className="flex items-center gap-2 text-sm font-bold text-slate-500">
          <button className="grid h-7 w-7 place-items-center overflow-hidden border border-slate-200 bg-slate-100" type="button" onClick={() => onOpenProfile(post.authorUserId)} title={`查看${getAuthorDisplayName(post)}的主页`}>
            {post.authorAvatarUrl ? (
              <BackendAssetImage className="h-full w-full object-cover" assetUrl={post.authorAvatarUrl} alt={getAuthorDisplayName(post)} fallbackContent={buildFallbackInitials(getAuthorDisplayName(post))} />
            ) : buildFallbackInitials(getAuthorDisplayName(post))}
          </button>
          <button className="truncate text-left hover:text-pink-500" type="button" onClick={() => onOpenProfile(post.authorUserId)}>
            {getAuthorDisplayName(post)}
          </button>
          <button className="ml-auto text-pink-500" type="button" onClick={() => onLike(post)}>
            {post.likedByCurrentUser ? '已赞' : '点赞'} {post.likeCount}
          </button>
        </div>
        <div className="flex flex-wrap gap-2">
          {cities.map(city => <span key={city} className="bg-slate-100 px-2 py-1 text-xs font-bold">{city}</span>)}
          {tags.slice(0, 3).map(tag => <span key={`${tag.tagType}-${tag.tagValue}`} className="bg-pink-50 px-2 py-1 text-xs font-bold text-pink-600">{tag.tagValue}</span>)}
        </div>
      </div>
    </article>
  )
}
