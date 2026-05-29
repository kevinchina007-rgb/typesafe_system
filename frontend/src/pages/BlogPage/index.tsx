import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import { useEffect, useState } from 'react'
import { ArrowLeft, ImagePlus, Search, Settings, Trash2, X } from 'lucide-react'

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { ContentImageResponse } from '@/microservices/content/objects/ContentImageResponse'
import type { BlogNotificationResponse } from '@/microservices/content/objects/BlogNotificationResponse'
import type { BlogPostResponse } from '@/microservices/content/objects/BlogPostResponse'
import type { BlogPostSummaryResponse, BlogTagResponse } from '@/microservices/content/objects/BlogPostSummaryResponse'
import type { BlogCommentResponse } from '@/microservices/content/objects/BlogCommentResponse'
import type { BlogProfileResponse, BlogProfileUserResponse } from '@/microservices/content/objects/BlogProfileResponse'
import type { AppLanguage, UserResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'

type BlogPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

type BlogTab = 'home' | 'publish' | 'notifications' | 'mine'
type MineTab = 'published' | 'favorites'
type NotificationFilter = 'comments' | 'likes' | 'followers'
type ProfileRelationTab = 'followers' | 'following'

const sidebarItems: Array<{ key: BlogTab; label: string }> = [
  { key: 'home', label: '主页' },
  { key: 'publish', label: '发布' },
  { key: 'notifications', label: '通知' },
  { key: 'mine', label: '我的' },
]

const tagGroups = [
  { type: 'days', label: '行程天数', values: ['1-2天', '3-5天', '6天以上'] },
  { type: 'season', label: '出发时间', values: ['春夏', '秋冬', '节假日'] },
  { type: 'companion', label: '和谁出行', values: ['一个人', '朋友', '亲子/情侣'] },
  { type: 'style', label: '旅行玩法', values: ['美食', '省钱', '拍照打卡'] },
]

const travelCities = ['北京', '上海', '武汉', '南京', '杭州', '深圳', '重庆', '广州', '成都', '长沙', '厦门', '西安']

const notificationFilters: Array<{ key: NotificationFilter; label: string; types: string[] }> = [
  { key: 'comments', label: '回复与评论', types: ['comment', 'reply', 'postCommented'] },
  { key: 'likes', label: '收到喜欢', types: ['like', 'favorite', 'postLiked', 'postFavorited'] },
  { key: 'followers', label: '新增粉丝', types: ['follow', 'userFollowed'] },
]

const emptyDraft = {
  postId: '',
  title: '',
  summary: '',
  coverText: '',
  content: '',
  travelCities: [] as string[],
  images: [] as ContentImageResponse[],
  tags: [] as BlogTagResponse[],
}

function buildFallbackInitials(label?: string | null) {
  return (label ?? '').trim().slice(0, 1).toUpperCase() || '旅'
}

function getAuthorDisplayName(post: BlogPostSummaryResponse) {
  return post.authorDisplayName || '旅行用户'
}

function getPostCoverImage(post: BlogPostSummaryResponse) {
  return post.coverImageUrl ?? post.images?.[0]?.publicUrl ?? null
}

function getPostCoverText(post: BlogPostSummaryResponse) {
  return post.coverText || post.summary || post.title || '这趟旅行还没写封面语'
}

function getPostTags(post: BlogPostSummaryResponse) {
  return Array.isArray(post.tags) ? post.tags : []
}

function getPostCities(post: BlogPostSummaryResponse) {
  const cities = Array.isArray(post.travelCities) ? post.travelCities : []
  return cities.length > 0 ? cities : post.travelCity ? [post.travelCity] : []
}

function formatShortDate(value?: string | null) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

async function fileToContentImage(file: File, sortOrder: number): Promise<ContentImageResponse> {
  const publicUrl = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error ?? new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
  return {
    imageId: `local-blog-image-${Date.now()}-${sortOrder}`,
    publicUrl,
    originalFileName: file.name,
    contentType: file.type,
    byteSize: file.size,
    sortOrder,
  }
}

export function BlogPage({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: BlogPageProps) {
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)
  const [activeTab, setActiveTab] = useState<BlogTab>('home')
  const [mineTab, setMineTab] = useState<MineTab>('published')
  const [notificationFilter, setNotificationFilter] = useState<NotificationFilter>('comments')
  const [posts, setPosts] = useState<BlogPostSummaryResponse[]>([])
  const [myPosts, setMyPosts] = useState<BlogPostSummaryResponse[]>([])
  const [favoritePosts, setFavoritePosts] = useState<BlogPostSummaryResponse[]>([])
  const [drafts, setDrafts] = useState<BlogPostSummaryResponse[]>([])
  const [notifications, setNotifications] = useState<BlogNotificationResponse[]>([])
  const [profile, setProfile] = useState<BlogProfileResponse | null>(null)
  const [viewedProfile, setViewedProfile] = useState<BlogProfileResponse | null>(null)
  const [viewedProfilePosts, setViewedProfilePosts] = useState<BlogPostSummaryResponse[]>([])
  const [profileRelationTab, setProfileRelationTab] = useState<ProfileRelationTab | null>(null)
  const [profileRelationUsers, setProfileRelationUsers] = useState<BlogProfileUserResponse[]>([])
  const [profileOverlayOpen, setProfileOverlayOpen] = useState(false)
  const [profileSettingsOpen, setProfileSettingsOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [selectedTag, setSelectedTag] = useState<BlogTagResponse | null>(null)
  const [selectedCities, setSelectedCities] = useState<string[]>([])
  const [draft, setDraft] = useState(emptyDraft)
  const [selectedPost, setSelectedPost] = useState<BlogPostResponse | null>(null)
  const [selectedImageIndex, setSelectedImageIndex] = useState(0)
  const [commentDraft, setCommentDraft] = useState('')
  const [followedAuthors, setFollowedAuthors] = useState<Set<string>>(new Set())

  const selectedTagLabel = selectedTag ? `${selectedTag.tagType}:${selectedTag.tagValue}` : '全部'
  const selectedCityLabel = selectedCities.join('|') || '全部城市'
  const selectedImages = selectedPost?.post.images ?? []
  const selectedImage = selectedImages[selectedImageIndex] ?? selectedImages[0] ?? null
  const selectedPostCities = selectedPost ? getPostCities(selectedPost.post) : []
  const selectedPostTags = selectedPost ? getPostTags(selectedPost.post) : []
  const currentProfile = viewedProfile ?? profile
  const isOwnProfile = Boolean(signedInUser && currentProfile?.userId === signedInUser.userId)
  const profileRelationsHidden = Boolean(currentProfile?.relationListHidden && !isOwnProfile)
  const displayedMinePosts = mineTab === 'favorites' && isOwnProfile ? favoritePosts : viewedProfile ? viewedProfilePosts : myPosts
  const activeNotificationTypes = notificationFilters.find(filter => filter.key === notificationFilter)?.types ?? []
  const filteredNotifications = notifications.filter(notification => activeNotificationTypes.includes(notification.notificationType))

  async function reloadHome() {
    const response = await travelMvpApiClient.listShortBlogPosts({
      scope: 'home',
      userId: signedInUser?.userId,
      q: query,
      tagType: selectedTag?.tagType,
      tagValue: selectedTag?.tagValue,
      travelCities: selectedCities.length > 0 ? selectedCities : undefined,
    })
    setPosts(response.posts ?? [])
  }

  async function reloadMine() {
    if (!signedInUser) {
      setDrafts([])
      setMyPosts([])
      setFavoritePosts([])
      setProfile(null)
      setNotifications([])
      return
    }
    const [mineResponse, favoritesResponse, draftResponse, profileResponse, notificationResponse] = await Promise.all([
      travelMvpApiClient.listShortBlogPosts({ scope: 'mine', userId: signedInUser.userId }),
      travelMvpApiClient.listShortBlogPosts({ scope: 'favorites', userId: signedInUser.userId }),
      travelMvpApiClient.listShortBlogPosts({ scope: 'drafts', userId: signedInUser.userId }),
      travelMvpApiClient.getBlogProfile(signedInUser.userId, signedInUser.userId),
      travelMvpApiClient.listBlogNotifications(signedInUser.userId),
    ])
    setMyPosts(mineResponse.posts ?? [])
    setFavoritePosts(favoritesResponse.posts ?? [])
    setDrafts(draftResponse.posts ?? [])
    setProfile(profileResponse)
    if (!viewedProfile || viewedProfile.userId === signedInUser.userId) {
      setViewedProfile(null)
      setViewedProfilePosts([])
    }
    setNotifications(notificationResponse.notifications ?? [])
  }

  useEffect(() => {
    void reloadHome().catch(() => setPosts([]))
  }, [query, selectedTagLabel, selectedCityLabel, signedInUser?.userId])

  useEffect(() => {
    void reloadMine().catch(() => undefined)
  }, [signedInUser?.userId])

  function requireUser() {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUser
  }

  function replacePost(nextPost: BlogPostSummaryResponse) {
    setPosts(current => current.map(item => (item.postId === nextPost.postId ? nextPost : item)))
    setMyPosts(current => current.map(item => (item.postId === nextPost.postId ? nextPost : item)))
    setFavoritePosts(current => {
      const withoutPost = current.filter(item => item.postId !== nextPost.postId)
      return nextPost.favoritedByCurrentUser ? [nextPost, ...withoutPost] : withoutPost
    })
    setSelectedPost(current => current && current.post.postId === nextPost.postId ? { ...current, post: nextPost } : current)
  }

  function toggleCity(city: string) {
    setSelectedCities(current => current.includes(city) ? current.filter(item => item !== city) : [...current, city])
  }

  function toggleDraftCity(city: string) {
    setDraft(current => ({
      ...current,
      travelCities: current.travelCities.includes(city)
        ? current.travelCities.filter(item => item !== city)
        : [...current.travelCities, city],
    }))
  }

  function toggleDraftTag(nextTag: BlogTagResponse) {
    setDraft(current => {
      const exists = current.tags.some(tag => tag.tagType === nextTag.tagType && tag.tagValue === nextTag.tagValue)
      return {
        ...current,
        tags: exists
          ? current.tags.filter(tag => !(tag.tagType === nextTag.tagType && tag.tagValue === nextTag.tagValue))
          : [...current.tags.filter(tag => tag.tagType !== nextTag.tagType), nextTag],
      }
    })
  }

  async function openPost(postId: string) {
    const response = await travelMvpApiClient.getBlogPost(postId, signedInUser?.userId)
    setSelectedPost(response)
    setSelectedImageIndex(0)
    setCommentDraft('')
    if (signedInUser && response.post.authorUserId !== signedInUser.userId) {
      const authorProfile = await travelMvpApiClient.getBlogProfile(response.post.authorUserId, signedInUser.userId)
      if (authorProfile.isFollowing) {
        setFollowedAuthors(current => new Set(current).add(response.post.authorUserId))
      }
    }
  }

  async function openProfile(profileUserId: string) {
    const viewerUserId = signedInUser?.userId
    if (viewerUserId && profileUserId === viewerUserId) {
      const [profileResponse, postsResponse] = await Promise.all([
        travelMvpApiClient.getBlogProfile(profileUserId, viewerUserId),
        travelMvpApiClient.listShortBlogPosts({ scope: 'mine', userId: profileUserId }),
      ])
      setProfile(profileResponse)
      setViewedProfile(null)
      setViewedProfilePosts(postsResponse.posts ?? [])
      setProfileRelationTab(null)
      setProfileRelationUsers([])
      setProfileSettingsOpen(false)
      setMineTab('published')
      setProfileOverlayOpen(true)
      return
    }
    const [profileResponse, postsResponse] = await Promise.all([
      travelMvpApiClient.getBlogProfile(profileUserId, viewerUserId),
      travelMvpApiClient.listShortBlogPosts({ scope: 'profile', userId: profileUserId }),
    ])
    setViewedProfile(profileResponse)
    setViewedProfilePosts(postsResponse.posts ?? [])
    setProfileRelationTab(null)
    setProfileRelationUsers([])
    setProfileSettingsOpen(false)
    setMineTab('published')
    setProfileOverlayOpen(true)
  }

  function closeProfileOverlay() {
    setProfileOverlayOpen(false)
    setViewedProfile(null)
    setViewedProfilePosts([])
    setProfileRelationTab(null)
    setProfileRelationUsers([])
    setProfileSettingsOpen(false)
  }

  async function openProfileRelation(nextTab: ProfileRelationTab) {
    if (!currentProfile) return
    if (currentProfile.relationListHidden && !isOwnProfile) {
      setProfileRelationTab(nextTab)
      setProfileRelationUsers([])
      return
    }
    const response = nextTab === 'followers'
      ? await travelMvpApiClient.listBlogFollowers(currentProfile.userId, signedInUser?.userId)
      : await travelMvpApiClient.listBlogFollowing(currentProfile.userId, signedInUser?.userId)
    setProfileRelationTab(nextTab)
    setProfileRelationUsers(response.users ?? [])
  }

  async function updateProfilePrivacy(hideRelations: boolean) {
    const user = requireUser()
    const response = await runPageActionWithResult(
      () => travelMvpApiClient.updateBlogProfilePrivacy(user.userId, hideRelations),
      '更新社区设置',
      translate('notice.actionSuccess'),
    )
    setProfile(response)
    if (!viewedProfile) {
      setProfileRelationTab(null)
      setProfileRelationUsers([])
    }
  }

  async function saveDraft(status: 'draft' | 'publish') {
    const user = requireUser()
    await runPageAction(async () => {
      const payload = {
        userId: user.userId,
        postId: draft.postId || null,
        title: draft.title,
        summary: draft.summary || draft.coverText,
        coverText: draft.coverText || draft.summary,
        content: draft.content,
        images: draft.images,
        tags: draft.tags,
        travelCity: draft.travelCities[0] ?? null,
        travelCities: draft.travelCities,
      }
      if (status === 'draft') {
        const response = await travelMvpApiClient.saveBlogDraft(payload)
        setDraft(current => ({ ...current, postId: response.post.postId }))
      } else {
        await travelMvpApiClient.createBlogPost(payload)
        setDraft(emptyDraft)
      }
      await reloadHome()
      await reloadMine()
    }, status === 'draft' ? '保存草稿' : '发布帖子', translate('notice.actionSuccess'))
  }

  async function likePost(post: BlogPostSummaryResponse) {
    const user = requireUser()
    const nextPost = await runPageActionWithResult(
      () => post.likedByCurrentUser
        ? travelMvpApiClient.unlikeBlogPost(post.postId, { userId: user.userId })
        : travelMvpApiClient.likeBlogPost(post.postId, { userId: user.userId }),
      post.likedByCurrentUser ? '取消点赞' : '点赞帖子',
      translate('notice.actionSuccess'),
    )
    replacePost(nextPost.post)
  }

  async function favoritePost(post: BlogPostSummaryResponse) {
    const user = requireUser()
    const nextPost = await runPageActionWithResult(
      () => post.favoritedByCurrentUser
        ? travelMvpApiClient.unfavoriteBlogPost(post.postId, { userId: user.userId })
        : travelMvpApiClient.favoriteBlogPost(post.postId, { userId: user.userId }),
      post.favoritedByCurrentUser ? '取消收藏' : '收藏帖子',
      translate('notice.actionSuccess'),
    )
    replacePost(nextPost.post)
  }

  async function followAuthor(post: BlogPostSummaryResponse) {
    const user = requireUser()
    if (post.authorUserId === user.userId) return
    await runPageAction(
      async () => {
        await travelMvpApiClient.followBlogUser(user.userId, post.authorUserId)
        setFollowedAuthors(current => new Set(current).add(post.authorUserId))
      },
      '关注用户',
      translate('notice.actionSuccess'),
    )
  }

  async function submitComment() {
    const user = requireUser()
    if (!selectedPost || !commentDraft.trim()) return
    const updated = await runPageActionWithResult(
      () => travelMvpApiClient.addBlogComment(selectedPost.post.postId, { userId: user.userId, content: commentDraft.trim() }),
      '发表评论',
      translate('notice.actionSuccess'),
    )
    setSelectedPost(updated)
    replacePost(updated.post)
    setCommentDraft('')
  }

  async function likeComment(comment: BlogCommentResponse) {
    const user = requireUser()
    const updated = await runPageActionWithResult(
      () => comment.likedByCurrentUser
        ? travelMvpApiClient.unlikeBlogComment(comment.commentId, { userId: user.userId })
        : travelMvpApiClient.likeBlogComment(comment.commentId, { userId: user.userId }),
      comment.likedByCurrentUser ? '取消评论点赞' : '点赞评论',
      translate('notice.actionSuccess'),
    )
    setSelectedPost(updated)
    replacePost(updated.post)
  }

  async function handleImages(files: FileList | null) {
    if (!files || files.length === 0) return
    const nextImages = await Promise.all(Array.from(files).slice(0, 9).map((file, index) => fileToContentImage(file, index)))
    setDraft(current => ({ ...current, images: nextImages }))
  }

  function removeDraftImage(imageId: string) {
    setDraft(current => ({ ...current, images: current.images.filter(image => image.imageId !== imageId) }))
  }

  function renderPostCard(post: BlogPostSummaryResponse, compact = false) {
    const coverImage = getPostCoverImage(post)
    const tags = getPostTags(post)
    const cities = getPostCities(post)
    return (
      <article key={post.postId} className="overflow-hidden border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
        <button className="block w-full text-left" type="button" onClick={() => void openPost(post.postId)}>
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
            <button className="grid h-7 w-7 place-items-center overflow-hidden border border-slate-200 bg-slate-100" type="button" onClick={() => void openProfile(post.authorUserId)} title={`查看${getAuthorDisplayName(post)}的主页`}>
              {post.authorAvatarUrl ? (
                <BackendAssetImage className="h-full w-full object-cover" assetUrl={post.authorAvatarUrl} alt={getAuthorDisplayName(post)} fallbackContent={buildFallbackInitials(getAuthorDisplayName(post))} />
              ) : buildFallbackInitials(getAuthorDisplayName(post))}
            </button>
            <button className="truncate text-left hover:text-pink-500" type="button" onClick={() => void openProfile(post.authorUserId)}>{getAuthorDisplayName(post)}</button>
            <button className="ml-auto text-pink-500" type="button" onClick={() => void likePost(post)}>
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

  return (
    <section className="grid min-h-[calc(100vh-10rem)] grid-cols-[13rem_minmax(0,1fr)] gap-8 bg-white p-6 text-slate-950">
      <aside className="sticky top-6 grid h-fit gap-3 border-r border-slate-200 pr-5">
        <strong className="mb-2 text-3xl font-black text-pink-500">flybara 社区</strong>
        {sidebarItems.map(item => (
          <button
            key={item.key}
            type="button"
            className={`min-h-12 border px-4 text-left text-lg font-black transition ${
              activeTab === item.key ? 'border-pink-500 bg-pink-500 text-white' : 'border-transparent bg-white hover:border-slate-300'
            }`}
            onClick={() => setActiveTab(item.key)}
          >
            {item.label}
          </button>
        ))}
      </aside>

      <main className="min-w-0">
        {activeTab === 'home' ? (
          <section className="grid gap-6">
            <div className="grid gap-4">
              <label className="grid gap-2">
                <span className="text-sm font-black text-slate-500">搜索帖子</span>
                <span className="flex min-h-14 items-center border-2 border-slate-300 bg-white px-4 focus-within:border-pink-500">
                  <Search className="mr-3 h-6 w-6 text-slate-500" aria-hidden="true" />
                  <input
                    className="min-w-0 flex-1 appearance-none !border-0 !bg-transparent !p-0 text-lg font-bold !shadow-none outline-none focus:ring-0"
                    ref={element => {
                      element?.style.setProperty('border', '0', 'important')
                      element?.style.setProperty('padding', '0', 'important')
                      element?.style.setProperty('background', 'transparent', 'important')
                      element?.style.setProperty('box-shadow', 'none', 'important')
                      element?.style.setProperty('outline', '0', 'important')
                    }}
                    value={query}
                    onChange={event => setQuery(event.target.value)}
                    placeholder="输入城市、标题、攻略关键词或旅行灵感"
                  />
                </span>
              </label>
              <div className="grid gap-3 bg-slate-50 p-4">
                {tagGroups.map(group => (
                  <div key={group.type} className="flex flex-wrap items-center gap-3">
                    <span className="w-24 text-sm font-black text-slate-500">{group.label}</span>
                    <button className="border border-slate-200 bg-white px-4 py-2 font-bold" type="button" onClick={() => setSelectedTag(null)}>全部</button>
                    {group.values.map(value => (
                      <button
                        key={value}
                        className={`border px-4 py-2 font-bold ${selectedTag?.tagType === group.type && selectedTag.tagValue === value ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`}
                        type="button"
                        onClick={() => setSelectedTag({ tagType: group.type, tagValue: value })}
                      >
                        {value}
                      </button>
                    ))}
                  </div>
                ))}
                <div className="flex flex-wrap items-center gap-3">
                  <span className="w-24 text-sm font-black text-slate-500">旅行城市</span>
                  <button className="border border-slate-200 bg-white px-4 py-2 font-bold" type="button" onClick={() => setSelectedCities([])}>全部</button>
                  {travelCities.map(city => (
                    <button key={city} className={`border px-4 py-2 font-bold ${selectedCities.includes(city) ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => toggleCity(city)}>
                      {city}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 gap-5 md:grid-cols-3 xl:grid-cols-4">
              {posts.map(post => renderPostCard(post))}
              {posts.length === 0 ? <p className="text-slate-500">暂时没有帖子，先去发布一篇旅行灵感吧。</p> : null}
            </div>
          </section>
        ) : null}

        {activeTab === 'publish' ? (
          <section className="grid gap-6">
            <div className="flex flex-wrap gap-3">
              <button className="border border-pink-500 bg-pink-500 px-5 py-3 font-black text-white" type="button" onClick={() => setDraft(emptyDraft)}>新建草稿</button>
              {drafts.map(item => (
                <button
                  key={item.postId}
                  className="border border-slate-300 bg-white px-4 py-3 font-bold"
                  type="button"
                  onClick={() => setDraft({
                    postId: item.postId,
                    title: item.title,
                    summary: item.summary,
                    coverText: item.coverText || item.summary,
                    content: '',
                    travelCities: getPostCities(item),
                    images: item.images ?? [],
                    tags: getPostTags(item),
                  })}
                >
                  {item.title || '未命名草稿'}
                </button>
              ))}
            </div>

            <div className="grid gap-5 border border-slate-200 bg-white p-5">
              <label className="grid gap-2">
                <span className="text-sm font-black text-slate-500">帖子标题</span>
                <input className="min-h-12 border-2 border-slate-300 px-4 font-bold" value={draft.title} onChange={event => setDraft(current => ({ ...current, title: event.target.value }))} placeholder="例如：在武汉过一个会发光的周末" />
              </label>
              <label className="grid gap-2">
                <span className="text-sm font-black text-slate-500">封面语</span>
                <input className="min-h-12 border-2 border-slate-300 px-4 font-bold" value={draft.coverText} onChange={event => setDraft(current => ({ ...current, coverText: event.target.value, summary: event.target.value }))} placeholder="显示在主页封面图上的一句话" />
              </label>
              <label className="grid gap-2">
                <span className="text-sm font-black text-slate-500">正文</span>
                <textarea className="min-h-40 border-2 border-slate-300 p-4 font-bold" value={draft.content} onChange={event => setDraft(current => ({ ...current, content: event.target.value }))} placeholder="写下路线、感受、避坑和推荐。这里是点进帖子后右侧能看到的正文。" />
              </label>
              <div className="grid gap-3">
                <div>
                  <span className="text-sm font-black text-slate-500">图片集</span>
                  <p className="mt-1 text-sm text-slate-500">上传 3-6 张更像小红书短帖。第一张会作为标题封面，其余图片放进正文图集。</p>
                </div>
                <div className="grid gap-3 lg:grid-cols-[16rem_minmax(0,1fr)]">
                  <label className="grid min-h-48 cursor-pointer place-items-center border-2 border-dashed border-slate-300 bg-slate-50 p-5 text-center transition hover:border-pink-400 hover:bg-pink-50">
                    <input className="sr-only" type="file" accept="image/*" multiple onChange={event => void handleImages(event.target.files)} />
                    <span className="grid gap-3 justify-items-center">
                      <span className="grid h-12 w-12 place-items-center border border-pink-200 bg-white text-pink-500">
                        <ImagePlus className="h-6 w-6" aria-hidden="true" />
                      </span>
                      <strong className="text-lg">选择图片</strong>
                      <span className="text-sm text-slate-500">支持多选，重新选择会替换当前图片</span>
                    </span>
                  </label>
                  <div className="grid min-h-48 grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-4">
                    {draft.images.map((image, index) => (
                      <figure key={image.imageId} className="group relative overflow-hidden border border-slate-200 bg-slate-100">
                        <img src={image.publicUrl} alt={image.originalFileName} className="aspect-square w-full object-cover" />
                        <figcaption className="absolute inset-x-0 bottom-0 flex items-center justify-between bg-black/65 px-3 py-2 text-xs font-black text-white">
                          <span>{index === 0 ? '封面' : `正文图 ${index}`}</span>
                          <button className="grid h-7 w-7 place-items-center bg-white/15 text-white hover:bg-white hover:text-slate-950" type="button" onClick={() => removeDraftImage(image.imageId)} title="移除图片">
                            <Trash2 className="h-4 w-4" aria-hidden="true" />
                          </button>
                        </figcaption>
                      </figure>
                    ))}
                    {draft.images.length === 0 ? (
                      <div className="col-span-full grid place-items-center border border-slate-200 bg-white p-6 text-center text-sm font-bold text-slate-400">
                        还没有图片，左侧选择后会在这里预览。
                      </div>
                    ) : null}
                  </div>
                </div>
              </div>
              <div className="grid gap-2">
                <span className="text-sm font-black text-slate-500">旅行城市，可多选</span>
                <div className="flex flex-wrap gap-3">
                  {travelCities.map(city => (
                    <button key={city} className={`border px-4 py-2 font-bold ${draft.travelCities.includes(city) ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => toggleDraftCity(city)}>
                      {city}
                    </button>
                  ))}
                </div>
              </div>
              <div className="grid gap-3">
                {tagGroups.map(group => (
                  <div key={group.type} className="flex flex-wrap items-center gap-3">
                    <span className="w-24 font-black text-slate-500">{group.label}</span>
                    {group.values.map(value => {
                      const active = draft.tags.some(tag => tag.tagType === group.type && tag.tagValue === value)
                      return (
                        <button key={value} className={`border px-4 py-2 font-bold ${active ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => toggleDraftTag({ tagType: group.type, tagValue: value })}>
                          {value}
                        </button>
                      )
                    })}
                  </div>
                ))}
              </div>
              <div className="flex gap-3">
                <button className="border border-slate-300 bg-white px-5 py-3 font-black" disabled={isBusy || !signedInUser} type="button" onClick={() => void saveDraft('draft')}>保存草稿</button>
                <button className="border border-pink-500 bg-pink-500 px-5 py-3 font-black text-white" disabled={isBusy || !signedInUser} type="button" onClick={() => void saveDraft('publish')}>发布</button>
              </div>
            </div>
          </section>
        ) : null}

        {activeTab === 'notifications' ? (
          <section className="grid gap-5">
            <div className="grid grid-cols-3 gap-4">
              {notificationFilters.map(filter => {
                const count = notifications.filter(notification => filter.types.includes(notification.notificationType)).length
                const active = notificationFilter === filter.key
                return (
                  <button
                    key={filter.key}
                    className={`border p-5 text-center text-xl font-black transition ${
                      active ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-pink-50 text-pink-500 hover:border-pink-400'
                    }`}
                    type="button"
                    onClick={() => setNotificationFilter(filter.key)}
                  >
                    {filter.label}
                    <span className={`ml-2 text-sm ${active ? 'text-white/80' : 'text-slate-400'}`}>{count}</span>
                  </button>
                )
              })}
            </div>
            {filteredNotifications.map(notification => (
              <article key={notification.notificationId} className="flex items-center gap-4 border-b border-slate-100 py-4">
                <button className="grid h-12 w-12 place-items-center overflow-hidden border border-slate-200 bg-slate-100" type="button" disabled={!notification.actorUserId} onClick={() => notification.actorUserId ? void openProfile(notification.actorUserId) : undefined}>
                  {notification.actorAvatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={notification.actorAvatarUrl} alt={notification.actorDisplayName ?? '系统'} fallbackContent={buildFallbackInitials(notification.actorDisplayName ?? '系统')} /> : buildFallbackInitials(notification.actorDisplayName ?? '系统')}
                </button>
                <div className="min-w-0">
                  <button className="block truncate text-left text-lg font-black hover:text-pink-500" type="button" disabled={!notification.actorUserId} onClick={() => notification.actorUserId ? void openProfile(notification.actorUserId) : undefined}>{notification.actorDisplayName ?? '系统通知'}</button>
                  <p className="truncate text-slate-500">{notification.content}</p>
                </div>
                <span className="ml-auto text-sm text-slate-400">{formatShortDate(notification.createdAt)}</span>
              </article>
            ))}
            {filteredNotifications.length === 0 ? <p className="text-slate-500">这一类暂时没有通知。</p> : null}
          </section>
        ) : null}

        {activeTab === 'mine' ? (
          <section className="grid gap-6">
            <div className="flex items-center gap-5 border border-slate-200 p-5">
              <span className="grid h-20 w-20 place-items-center overflow-hidden border border-slate-200 bg-slate-100 text-3xl font-black">
                {currentProfile?.avatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={currentProfile.avatarUrl} alt={currentProfile.nickname} fallbackContent={buildFallbackInitials(currentProfile.nickname)} /> : buildFallbackInitials(currentProfile?.nickname ?? signedInUser?.nickname ?? '旅')}
              </span>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-3">
                  <h2 className="text-3xl font-black">{currentProfile?.nickname ?? signedInUser?.nickname ?? '未登录用户'}</h2>
                  {!isOwnProfile && currentProfile ? (
                    <button className="border border-pink-500 bg-pink-500 px-4 py-2 font-black text-white disabled:border-slate-200 disabled:bg-slate-100 disabled:text-slate-500" type="button" disabled={currentProfile.isFollowing || followedAuthors.has(currentProfile.userId) || !signedInUser} onClick={() => void followAuthor({ authorUserId: currentProfile.userId } as BlogPostSummaryResponse)}>
                      {currentProfile.isFollowing || followedAuthors.has(currentProfile.userId) ? '已关注' : '关注'}
                    </button>
                  ) : null}
                  {isOwnProfile && currentProfile ? (
                    <span className="relative ml-auto">
                      <button className="grid h-10 w-10 place-items-center border border-slate-200 bg-white text-slate-700 hover:border-pink-400 hover:text-pink-500" type="button" onClick={() => setProfileSettingsOpen(open => !open)} title="社区设置">
                        <Settings className="h-5 w-5" aria-hidden="true" />
                      </button>
                      {profileSettingsOpen ? (
                        <span className="absolute right-0 top-12 z-20 grid w-72 gap-3 border border-slate-200 bg-white p-4 text-sm shadow-xl">
                          <strong className="text-base">社区设置</strong>
                          <label className="flex items-center justify-between gap-4">
                            <span className="font-bold text-slate-600">隐藏我的关注和粉丝列表</span>
                            <input type="checkbox" checked={currentProfile.hideRelations} onChange={event => void updateProfilePrivacy(event.target.checked)} />
                          </label>
                          <span className="text-slate-500">开启后，其他用户无法查看你的粉丝和关注列表。</span>
                        </span>
                      ) : null}
                    </span>
                  ) : null}
                </div>
                <div className="mt-2 flex flex-wrap gap-4 text-slate-500">
                  <button className="font-bold hover:text-pink-500 disabled:text-slate-400" type="button" disabled={!currentProfile || profileRelationsHidden} onClick={() => void openProfileRelation('followers')}>粉丝 {currentProfile?.followerCount ?? 0}</button>
                  <button className="font-bold hover:text-pink-500 disabled:text-slate-400" type="button" disabled={!currentProfile || profileRelationsHidden} onClick={() => void openProfileRelation('following')}>关注 {currentProfile?.followingCount ?? 0}</button>
                  <span className="font-bold">获赞 {currentProfile?.receivedLikeCount ?? 0}</span>
                </div>
                {profileRelationsHidden ? <p className="mt-2 text-sm font-bold text-slate-400">该用户已隐藏关注和粉丝列表。</p> : null}
                {viewedProfile && signedInUser ? <button className="mt-3 border border-slate-200 bg-white px-3 py-2 text-sm font-black hover:border-pink-400" type="button" onClick={() => void openProfile(signedInUser.userId)}>回到我的主页</button> : null}
              </div>
            </div>
            {profileRelationTab ? (
              <div className="grid gap-3 border border-slate-200 p-4">
                <div className="flex items-center justify-between">
                  <strong className="text-xl font-black">{profileRelationTab === 'followers' ? '粉丝列表' : '关注列表'}</strong>
                  <button className="border border-slate-200 px-3 py-1 font-bold" type="button" onClick={() => { setProfileRelationTab(null); setProfileRelationUsers([]) }}>收起</button>
                </div>
                <div className="grid gap-3 md:grid-cols-2">
                  {profileRelationUsers.map(user => (
                    <button key={user.userId} className="flex items-center gap-3 border border-slate-200 p-3 text-left hover:border-pink-400" type="button" onClick={() => void openProfile(user.userId)}>
                      <span className="grid h-12 w-12 place-items-center overflow-hidden border border-slate-200 bg-slate-100 font-black">
                        {user.avatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={user.avatarUrl} alt={user.nickname} fallbackContent={buildFallbackInitials(user.nickname)} /> : buildFallbackInitials(user.nickname)}
                      </span>
                      <span className="min-w-0">
                        <strong className="block truncate">{user.nickname}</strong>
                        <span className="text-sm text-slate-500">粉丝 {user.followerCount} / 关注 {user.followingCount} / 获赞 {user.receivedLikeCount}</span>
                      </span>
                    </button>
                  ))}
                </div>
                {profileRelationUsers.length === 0 ? <p className="text-slate-500">这里暂时还没有人。</p> : null}
              </div>
            ) : null}
            <div className="flex gap-3">
              <button className={`border px-5 py-3 font-black ${mineTab === 'published' ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => setMineTab('published')}>发布栏</button>
              {isOwnProfile ? <button className={`border px-5 py-3 font-black ${mineTab === 'favorites' ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => setMineTab('favorites')}>收藏栏</button> : null}
            </div>
            <div className="grid grid-cols-1 gap-5 md:grid-cols-3">
              {displayedMinePosts.map(post => renderPostCard(post, true))}
              {displayedMinePosts.length === 0 ? <p className="text-slate-500">{mineTab === 'published' ? '你还没有发布内容。' : '你还没有收藏帖子。'}</p> : null}
            </div>
          </section>
        ) : null}
      </main>

      {profileOverlayOpen ? (
        <div className="fixed inset-0 z-[60] overflow-y-auto bg-white/95 px-6 py-6 text-slate-950 backdrop-blur-sm">
          <div className="mx-auto grid max-w-6xl gap-6">
            <div className="flex items-center justify-between">
              <button className="inline-flex items-center gap-2 border border-slate-200 bg-white px-4 py-2 font-black hover:border-pink-400" type="button" onClick={closeProfileOverlay}>
                <ArrowLeft className="h-5 w-5" aria-hidden="true" />
                返回
              </button>
              <button className="grid h-10 w-10 place-items-center border border-slate-200 bg-white hover:border-pink-400" type="button" onClick={closeProfileOverlay} title="关闭">
                <X className="h-5 w-5" aria-hidden="true" />
              </button>
            </div>
            <section className="grid gap-6">
              <div className="flex items-center gap-5 border border-slate-200 bg-white p-5">
                <span className="grid h-20 w-20 place-items-center overflow-hidden border border-slate-200 bg-slate-100 text-3xl font-black">
                  {currentProfile?.avatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={currentProfile.avatarUrl} alt={currentProfile.nickname} fallbackContent={buildFallbackInitials(currentProfile.nickname)} /> : buildFallbackInitials(currentProfile?.nickname ?? signedInUser?.nickname ?? '旅')}
                </span>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-3">
                    <h2 className="text-3xl font-black">{currentProfile?.nickname ?? signedInUser?.nickname ?? '未登录用户'}</h2>
                    {!isOwnProfile && currentProfile ? (
                      <button className="border border-pink-500 bg-pink-500 px-4 py-2 font-black text-white disabled:border-slate-200 disabled:bg-slate-100 disabled:text-slate-500" type="button" disabled={currentProfile.isFollowing || followedAuthors.has(currentProfile.userId) || !signedInUser} onClick={() => void followAuthor({ authorUserId: currentProfile.userId } as BlogPostSummaryResponse)}>
                        {currentProfile.isFollowing || followedAuthors.has(currentProfile.userId) ? '已关注' : '关注'}
                      </button>
                    ) : null}
                    {isOwnProfile && currentProfile ? (
                      <span className="relative ml-auto">
                        <button className="grid h-10 w-10 place-items-center border border-slate-200 bg-white text-slate-700 hover:border-pink-400 hover:text-pink-500" type="button" onClick={() => setProfileSettingsOpen(open => !open)} title="社区设置">
                          <Settings className="h-5 w-5" aria-hidden="true" />
                        </button>
                        {profileSettingsOpen ? (
                          <span className="absolute right-0 top-12 z-20 grid w-72 gap-3 border border-slate-200 bg-white p-4 text-sm shadow-xl">
                            <strong className="text-base">社区设置</strong>
                            <label className="flex items-center justify-between gap-4">
                              <span className="font-bold text-slate-600">隐藏我的关注和粉丝列表</span>
                              <input type="checkbox" checked={currentProfile.hideRelations} onChange={event => void updateProfilePrivacy(event.target.checked)} />
                            </label>
                            <span className="text-slate-500">开启后，其他用户无法查看你的粉丝和关注列表。</span>
                          </span>
                        ) : null}
                      </span>
                    ) : null}
                  </div>
                  <div className="mt-2 flex flex-wrap gap-4 text-slate-500">
                    <button className="font-bold hover:text-pink-500 disabled:text-slate-400" type="button" disabled={!currentProfile || profileRelationsHidden} onClick={() => void openProfileRelation('followers')}>粉丝 {currentProfile?.followerCount ?? 0}</button>
                    <button className="font-bold hover:text-pink-500 disabled:text-slate-400" type="button" disabled={!currentProfile || profileRelationsHidden} onClick={() => void openProfileRelation('following')}>关注 {currentProfile?.followingCount ?? 0}</button>
                    <span className="font-bold">获赞 {currentProfile?.receivedLikeCount ?? 0}</span>
                  </div>
                  {profileRelationsHidden ? <p className="mt-2 text-sm font-bold text-slate-400">该用户已隐藏关注和粉丝列表。</p> : null}
                </div>
              </div>
              {profileRelationTab ? (
                <div className="grid gap-3 border border-slate-200 bg-white p-4">
                  <div className="flex items-center justify-between">
                    <strong className="text-xl font-black">{profileRelationTab === 'followers' ? '粉丝列表' : '关注列表'}</strong>
                    <button className="border border-slate-200 px-3 py-1 font-bold" type="button" onClick={() => { setProfileRelationTab(null); setProfileRelationUsers([]) }}>收起</button>
                  </div>
                  {profileRelationsHidden ? <p className="text-slate-500">该用户已隐藏关注和粉丝列表。</p> : null}
                  <div className="grid gap-3 md:grid-cols-2">
                    {profileRelationUsers.map(user => (
                      <button key={user.userId} className="flex items-center gap-3 border border-slate-200 p-3 text-left hover:border-pink-400" type="button" onClick={() => void openProfile(user.userId)}>
                        <span className="grid h-12 w-12 place-items-center overflow-hidden border border-slate-200 bg-slate-100 font-black">
                          {user.avatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={user.avatarUrl} alt={user.nickname} fallbackContent={buildFallbackInitials(user.nickname)} /> : buildFallbackInitials(user.nickname)}
                        </span>
                        <span className="min-w-0">
                          <strong className="block truncate">{user.nickname}</strong>
                          <span className="text-sm text-slate-500">粉丝 {user.followerCount} / 关注 {user.followingCount} / 获赞 {user.receivedLikeCount}</span>
                        </span>
                      </button>
                    ))}
                  </div>
                  {!profileRelationsHidden && profileRelationUsers.length === 0 ? <p className="text-slate-500">这里暂时还没有人。</p> : null}
                </div>
              ) : null}
              <div className="flex gap-3">
                <button className={`border px-5 py-3 font-black ${mineTab === 'published' ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => setMineTab('published')}>发布</button>
                {isOwnProfile ? <button className={`border px-5 py-3 font-black ${mineTab === 'favorites' ? 'border-pink-500 bg-pink-500 text-white' : 'border-slate-200 bg-white'}`} type="button" onClick={() => setMineTab('favorites')}>收藏</button> : null}
              </div>
              <div className="grid grid-cols-1 gap-5 md:grid-cols-3">
                {displayedMinePosts.map(post => renderPostCard(post, true))}
                {displayedMinePosts.length === 0 ? <p className="text-slate-500">{mineTab === 'published' ? '还没有发布内容。' : '还没有收藏帖子。'}</p> : null}
              </div>
            </section>
          </div>
        </div>
      ) : null}

      {selectedPost ? (
        <div className="fixed inset-0 z-50 grid place-items-center bg-black/70 px-5 py-6">
          <button className="absolute right-8 top-8 grid h-12 w-12 place-items-center rounded-full bg-black/70 text-white hover:bg-white hover:text-black" type="button" onClick={() => setSelectedPost(null)} title="关闭">
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
                  <button
                    className="absolute left-4 grid h-11 w-11 place-items-center rounded-full border border-white/30 bg-white text-3xl text-black hover:bg-black hover:text-white"
                    type="button"
                    onClick={() => setSelectedImageIndex(current => (current - 1 + selectedImages.length) % selectedImages.length)}
                  >
                    ‹
                  </button>
                  <button
                    className="absolute right-4 grid h-11 w-11 place-items-center rounded-full border border-white/30 bg-white text-3xl text-black hover:bg-black hover:text-white"
                    type="button"
                    onClick={() => setSelectedImageIndex(current => (current + 1) % selectedImages.length)}
                  >
                    ›
                  </button>
                  <span className="absolute right-5 top-5 rounded-full bg-black/50 px-3 py-1 text-sm font-black">{selectedImageIndex + 1}/{selectedImages.length}</span>
                </>
              ) : null}
            </div>
            <div className="grid min-h-0 grid-rows-[auto_minmax(0,1fr)_auto] border-l border-white/10 bg-zinc-950">
              <header className="flex items-center gap-4 border-b border-white/10 p-6">
                <button className="grid h-14 w-14 place-items-center overflow-hidden rounded-full border border-white/10 bg-zinc-800 text-xl font-black hover:border-rose-300" type="button" onClick={() => void openProfile(selectedPost.post.authorUserId)} title="查看主页">
                  {selectedPost.post.authorAvatarUrl ? <BackendAssetImage className="h-full w-full object-cover" assetUrl={selectedPost.post.authorAvatarUrl} alt={getAuthorDisplayName(selectedPost.post)} fallbackContent={buildFallbackInitials(getAuthorDisplayName(selectedPost.post))} /> : buildFallbackInitials(getAuthorDisplayName(selectedPost.post))}
                </button>
                <button className="min-w-0 flex-1 truncate text-left text-xl font-black hover:text-rose-200" type="button" onClick={() => void openProfile(selectedPost.post.authorUserId)}>{getAuthorDisplayName(selectedPost.post)}</button>
                {selectedPost.post.authorUserId !== signedInUser?.userId ? (
                  <button
                    className="rounded-full bg-rose-500 px-8 py-3 text-lg font-black text-white disabled:bg-zinc-700"
                    type="button"
                    disabled={followedAuthors.has(selectedPost.post.authorUserId)}
                    onClick={() => void followAuthor(selectedPost.post)}
                  >
                    {followedAuthors.has(selectedPost.post.authorUserId) ? '已关注' : '关注'}
                  </button>
                ) : null}
              </header>
              <div className="min-h-0 overflow-y-auto p-6">
                <h2 className="text-2xl font-black">{selectedPost.post.title}</h2>
                <p className="mt-4 whitespace-pre-wrap leading-8 text-zinc-100">{selectedPost.content}</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {selectedPostCities.map(city => <span key={city} className="rounded-full bg-zinc-800 px-3 py-1 text-sm text-zinc-200">{city}</span>)}
                  {selectedPostTags.map(tag => <span key={`${tag.tagType}-${tag.tagValue}`} className="rounded-full bg-rose-500/20 px-3 py-1 text-sm text-rose-200">#{tag.tagValue}</span>)}
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
                            <button className="font-black hover:text-rose-200" type="button" onClick={() => void openProfile(comment.authorUserId)}>{comment.authorDisplayName}</button>
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
                    {selectedPost.comments.length === 0 ? <p className="text-zinc-500">还没有评论，来坐第一排。</p> : null}
                  </div>
                </div>
              </div>
              <footer className="grid gap-3 border-t border-white/10 p-5">
                <div className="flex items-center gap-3">
                  <input className="min-h-11 min-w-0 flex-1 rounded-full border border-white/10 bg-zinc-900 px-5 text-white outline-none focus:border-rose-400" value={commentDraft} onChange={event => setCommentDraft(event.target.value)} placeholder="说点什么..." />
                  <button className="rounded-full bg-zinc-800 px-5 py-3 font-black hover:bg-zinc-700" type="button" disabled={!commentDraft.trim()} onClick={() => void submitComment()}>评论</button>
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
      ) : null}
    </section>
  )
}
