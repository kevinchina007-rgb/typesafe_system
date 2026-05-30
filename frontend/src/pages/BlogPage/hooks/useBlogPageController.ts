import { useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { BlogNotificationResponse } from '@/microservices/content/objects/BlogNotificationResponse'
import type { BlogPostResponse } from '@/microservices/content/objects/BlogPostResponse'
import type { BlogPostSummaryResponse, BlogTagResponse } from '@/microservices/content/objects/BlogPostSummaryResponse'
import type { BlogProfileResponse, BlogProfileUserResponse } from '@/microservices/content/objects/BlogProfileResponse'
import type { BlogCommentResponse } from '@/microservices/content/objects/BlogCommentResponse'
import type { BlogPageController, BlogPageProps, BlogTab, MineTab, NotificationFilter, ProfileRelationTab } from '../objects'
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

export function useBlogPageController({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: BlogPageProps): BlogPageController {
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

  const selectedTagLabel = getSelectedTagLabel(selectedTag)
  const selectedCityLabel = getSelectedCityLabel(selectedCities)
  const selectedImages = getSelectedImages(selectedPost)
  const selectedImage = getSelectedImage(selectedPost, selectedImageIndex)
  const selectedPostCities = getSelectedPostCities(selectedPost)
  const selectedPostTags = getSelectedPostTags(selectedPost)
  const currentProfile = viewedProfile ?? profile
  const isOwnProfile = getIsOwnProfile(signedInUser?.userId, currentProfile?.userId)
  const profileRelationsHidden = getProfileRelationsHidden(currentProfile, isOwnProfile)
  const displayedMinePosts = getDisplayedMinePosts(mineTab, isOwnProfile, Boolean(viewedProfile), favoritePosts, viewedProfilePosts, myPosts)
  const filteredNotifications = useMemo(() => filterNotifications(notifications, notificationFilter), [notifications, notificationFilter])

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
    setSelectedPost(current => (current && current.post.postId === nextPost.postId ? { ...current, post: nextPost } : current))
  }

  function toggleCity(city: string) {
    setSelectedCities(current => (current.includes(city) ? current.filter(item => item !== city) : [...current, city]))
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

  async function uploadDraftImage(imageFile: File): Promise<ContentImageResponse> {
    return fileToContentImage(imageFile, Date.now())
  }

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
