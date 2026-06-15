import { useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { BlogNotificationResponse } from '@/microservices/blog/objects/BlogNotificationResponse'
import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import type { BlogPostSummaryResponse, BlogTagResponse } from '@/microservices/blog/objects/BlogPostSummaryResponse'
import type { BlogProfileResponse, BlogProfileUserResponse } from '@/microservices/blog/objects/BlogProfileResponse'
import type { BlogCommentResponse } from '@/microservices/blog/objects/BlogCommentResponse'
import type { BlogDraft, BlogPageController, BlogPageProps, BlogTab, MineTab, NotificationFilter, ProfileRelationTab } from '../objects'
import {
  emptyDraft,
  filterNotifications,
  getDisplayedMinePosts,
  getIsOwnProfile,
  getProfileRelationsHidden,
  getSelectedCityLabel,
  getSelectedImage,
  getSelectedImages,
  getSelectedPostCities,
  getSelectedPostTags,
  getSelectedTagLabel,
} from '../functions'
import { usePageActions } from '@/pages/shared/usePageActions'
import type { ContentImageResponse } from '@/lib/mvp-types/index'

// 把本地图片转成页面可直接展示的内容图片对象。
async function fileToContentImage(file: File, sortOrder: number) {
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

// Blog 页面总控制器，负责组织状态、接口调用和页面动作。
export function useBlogPageController({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: BlogPageProps): BlogPageController {
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

  // 顶部导航当前选中的标签页。
  const [activeTab, setActiveTab] = useState<BlogTab>('home')
  // “我的内容”区域当前选中的标签页。
  const [mineTab, setMineTab] = useState<MineTab>('published')
  // 通知列表当前使用的筛选条件。
  const [notificationFilter, setNotificationFilter] = useState<NotificationFilter>('comments')
  // 首页帖子列表。
  const [posts, setPosts] = useState<BlogPostSummaryResponse[]>([])
  // 我已发布的帖子列表。
  const [myPosts, setMyPosts] = useState<BlogPostSummaryResponse[]>([])
  // 我收藏的帖子列表。
  const [favoritePosts, setFavoritePosts] = useState<BlogPostSummaryResponse[]>([])
  // 我的草稿列表。
  const [drafts, setDrafts] = useState<BlogPostSummaryResponse[]>([])
  // 通知列表。
  const [notifications, setNotifications] = useState<BlogNotificationResponse[]>([])
  // 当前登录用户自己的主页数据。
  const [profile, setProfile] = useState<BlogProfileResponse | null>(null)
  // 当前正在查看的其他用户主页数据。
  const [viewedProfile, setViewedProfile] = useState<BlogProfileResponse | null>(null)
  // 当前查看的主页下的帖子列表。
  const [viewedProfilePosts, setViewedProfilePosts] = useState<BlogPostSummaryResponse[]>([])
  // 主页关系列表当前选中的页签。
  const [profileRelationTab, setProfileRelationTab] = useState<ProfileRelationTab | null>(null)
  // 主页关系列表当前展示的用户。
  const [profileRelationUsers, setProfileRelationUsers] = useState<BlogProfileUserResponse[]>([])
  // 主页浮层是否打开。
  const [profileOverlayOpen, setProfileOverlayOpen] = useState(false)
  // 主页设置浮层是否打开。
  const [profileSettingsOpen, setProfileSettingsOpen] = useState(false)
  // 首页搜索关键词。
  const [query, setQuery] = useState('')
  // 首页选中的标签。
  const [selectedTag, setSelectedTag] = useState<BlogTagResponse | null>(null)
  // 首页选中的城市。
  const [selectedCities, setSelectedCities] = useState<string[]>([])
  // 发布编辑器中的草稿内容。
  const [draft, setDraft] = useState(emptyDraft)
  // 当前选中的帖子详情。
  const [selectedPost, setSelectedPost] = useState<BlogPostResponse | null>(null)
  // 当前选中的帖子图片下标。
  const [selectedImageIndex, setSelectedImageIndex] = useState(0)
  // 评论输入草稿。
  const [commentDraft, setCommentDraft] = useState('')
  // 已关注作者集合。
  const [followedAuthors, setFollowedAuthors] = useState<Set<string>>(new Set())

  // 计算当前选中标签的展示文案。
  const selectedTagLabel = getSelectedTagLabel(selectedTag)
  // 计算当前选中城市的展示文案。
  const selectedCityLabel = getSelectedCityLabel(selectedCities)
  // 计算选中帖子对应的图片列表。
  const selectedImages = getSelectedImages(selectedPost)
  // 计算选中帖子当前显示的图片。
  const selectedImage = getSelectedImage(selectedPost, selectedImageIndex)
  // 计算选中帖子对应的城市列表。
  const selectedPostCities = getSelectedPostCities(selectedPost)
  // 计算选中帖子对应的标签列表。
  const selectedPostTags = getSelectedPostTags(selectedPost)
  // 合并当前正在展示的主页对象。
  const currentProfile = viewedProfile ?? profile
  // 判断当前查看的主页是不是自己。
  const isOwnProfile = getIsOwnProfile(signedInUser?.userId, currentProfile?.userId)
  // 判断关系列表是否需要隐藏。
  const profileRelationsHidden = getProfileRelationsHidden(currentProfile, isOwnProfile)
  // 计算“我的内容”区域当前展示的帖子来源。
  const displayedMinePosts = getDisplayedMinePosts(mineTab, isOwnProfile, Boolean(viewedProfile), favoritePosts, viewedProfilePosts, myPosts)
  // 根据通知筛选条件得到当前可见通知。
  const filteredNotifications = useMemo(() => filterNotifications(notifications, notificationFilter), [notifications, notificationFilter])

  // 重新加载首页帖子列表。
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

  // 重新加载“我的内容”相关数据。
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

  // 首次挂载或筛选项变化时，重新拉取首页帖子。
  useEffect(() => {
    void reloadHome().catch(() => setPosts([]))
  }, [query, selectedTagLabel, selectedCityLabel, signedInUser?.userId])

  // 登录态变化时，重新拉取“我的内容”相关数据。
  useEffect(() => {
    void reloadMine().catch(() => undefined)
  }, [signedInUser?.userId])

  // 校验当前用户是否已登录。
  function requireUser() {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUser
  }

  // 用最新帖子替换当前列表中的旧帖子。
  function replacePost(nextPost: BlogPostSummaryResponse) {
    setPosts(current => current.map(item => (item.postId === nextPost.postId ? nextPost : item)))
    setMyPosts(current => current.map(item => (item.postId === nextPost.postId ? nextPost : item)))
    setFavoritePosts(current => {
      const withoutPost = current.filter(item => item.postId !== nextPost.postId)
      return nextPost.favoritedByCurrentUser ? [nextPost, ...withoutPost] : withoutPost
    })
    setSelectedPost(current => (current && current.post.postId === nextPost.postId ? { ...current, post: nextPost } : current))
  }

  // 切换首页城市筛选项。
  function toggleCity(city: string) {
    setSelectedCities(current => (current.includes(city) ? current.filter(item => item !== city) : [...current, city]))
  }

  // 切换草稿里的城市。
  function toggleDraftCity(city: string) {
    setDraft(current => ({
      ...current,
      travelCities: current.travelCities.includes(city)
        ? current.travelCities.filter(item => item !== city)
        : [...current.travelCities, city],
    }))
  }

  // 切换草稿里的标签。
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

  // 打开帖子详情。
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

  // 打开某个用户的主页。
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

  // 关闭主页浮层。
  function closeProfileOverlay() {
    setProfileOverlayOpen(false)
    setViewedProfile(null)
    setViewedProfilePosts([])
    setProfileRelationTab(null)
    setProfileRelationUsers([])
    setProfileSettingsOpen(false)
  }

  // 打开主页的关注/粉丝列表。
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

  // 更新主页隐私设置。
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

  // 保存草稿或者发布文章。
  async function saveDraft(
    status: 'draft' | 'publish',
    editorDraft?: Pick<BlogDraft, 'title' | 'summary' | 'content' | 'images'>,
  ) {
    const user = requireUser()
    await runPageAction(async () => {
      const currentDraft = editorDraft ? { ...draft, ...editorDraft } : draft
      const payload = {
        userId: user.userId,
        postId: currentDraft.postId || null,
        title: currentDraft.title,
        summary: currentDraft.summary || currentDraft.coverText,
        coverText: currentDraft.coverText || currentDraft.summary,
        content: currentDraft.content,
        images: currentDraft.images,
        tags: currentDraft.tags,
        travelCity: currentDraft.travelCities[0] ?? null,
        travelCities: currentDraft.travelCities,
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

  // 给帖子点赞或取消点赞。
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

  // 收藏或取消收藏帖子。
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

  // 关注帖子作者。
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

  // 提交帖子评论。
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

  // 给评论点赞或取消点赞。
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

  // 处理本地上传图片并生成预览对象。
  async function handleImages(files: FileList | null) {
    if (!files || files.length === 0) return
    const nextImages = await Promise.all(Array.from(files).slice(0, 9).map((file, index) => fileToContentImage(file, index)))
    setDraft(current => ({ ...current, images: nextImages }))
  }

  // 把单张图片文件转成页面可用的内容图片。
  async function uploadDraftImage(imageFile: File): Promise<ContentImageResponse> {
    return fileToContentImage(imageFile, Date.now())
  }

  // 从草稿里移除一张图片。
  function removeDraftImage(imageId: string) {
    setDraft(current => ({ ...current, images: current.images.filter(image => image.imageId !== imageId) }))
  }

  return {
    isBusy,
    activeTab,
    mineTab,
    notificationFilter,
    posts,
    myPosts,
    favoritePosts,
    drafts,
    notifications,
    profile,
    viewedProfile,
    viewedProfilePosts,
    profileRelationTab,
    profileRelationUsers,
    profileOverlayOpen,
    profileSettingsOpen,
    query,
    selectedTag,
    selectedCities,
    draft,
    selectedPost,
    selectedImageIndex,
    commentDraft,
    followedAuthors,
    selectedTagLabel,
    selectedCityLabel,
    selectedImages,
    selectedImage,
    selectedPostCities,
    selectedPostTags,
    currentProfile,
    isOwnProfile,
    profileRelationsHidden,
    displayedMinePosts,
    filteredNotifications,
    setActiveTab,
    setMineTab,
    setNotificationFilter,
    setQuery,
    setSelectedTag,
    setSelectedCities,
    setDraft,
    setSelectedPost,
    setSelectedImageIndex,
    setCommentDraft,
    setProfileSettingsOpen,
    toggleCity,
    toggleDraftCity,
    toggleDraftTag,
    reloadHome,
    reloadMine,
    openPost,
    openProfile,
    closeProfileOverlay,
    openProfileRelation,
    updateProfilePrivacy,
    saveDraft,
    likePost,
    favoritePost,
    followAuthor,
    submitComment,
    likeComment,
    handleImages,
    uploadDraftImage,
    removeDraftImage,
    setProfileRelationTab,
    setProfileRelationUsers,
  }
}
