// BlogPageModels：博客页面使用的视图模型与状态结构。

import type { AppLanguage, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { BlogNotificationResponse } from '@/microservices/blog/objects/BlogNotificationResponse'
import type { BlogPostResponse } from '@/microservices/blog/objects/BlogPostResponse'
import type { BlogPostSummaryResponse } from '@/microservices/blog/objects/BlogPostSummaryResponse'
import type { BlogTagResponse } from '@/microservices/blog/objects/BlogTagResponse'
import type { BlogProfileResponse } from '@/microservices/blog/objects/BlogProfileResponse'
import type { BlogProfileUserResponse } from '@/microservices/blog/objects/BlogProfileUserResponse'
import type { ContentImagePlannerResponse } from '@/lib/mvp-types/index'

// Blog 页面顶层参数，负责把语言、登录用户和通知回调传给整页。
export type BlogPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

// Blog 页面顶部导航标签。
export type BlogTab = 'home' | 'publish' | 'notifications' | 'mine'
// “我的”页面内部的二级标签。
export type MineTab = 'published' | 'favorites'
// 通知列表的筛选标签。
export type NotificationFilter = 'comments' | 'likes' | 'followers'
// 个人主页关系列表标签。
export type ProfileRelationTab = 'followers' | 'following'

// Blog 草稿编辑态的数据结构。
export type BlogDraft = {
  postId: string
  title: string
  summary: string
  coverText: string
  content: string
  travelCities: string[]
  images: ContentImagePlannerResponse[]
  tags: BlogTagResponse[]
}

// Blog 页面 controller 暴露给视图层的完整状态和动作集合。
export type BlogPageController = {
  isBusy: boolean
  activeTab: BlogTab
  mineTab: MineTab
  notificationFilter: NotificationFilter
  posts: BlogPostSummaryResponse[]
  myPosts: BlogPostSummaryResponse[]
  favoritePosts: BlogPostSummaryResponse[]
  drafts: BlogPostSummaryResponse[]
  notifications: BlogNotificationResponse[]
  profile: BlogProfileResponse | null
  viewedProfile: BlogProfileResponse | null
  viewedProfilePosts: BlogPostSummaryResponse[]
  profileRelationTab: ProfileRelationTab | null
  profileRelationUsers: BlogProfileUserResponse[]
  profileOverlayOpen: boolean
  profileSettingsOpen: boolean
  query: string
  selectedTag: BlogTagResponse | null
  selectedCities: string[]
  draft: BlogDraft
  selectedPost: BlogPostResponse | null
  selectedImageIndex: number
  commentDraft: string
  followedAuthors: Set<string>
  selectedTagLabel: string
  selectedCityLabel: string
  selectedImages: NonNullable<BlogPostResponse['post']['images']>
  selectedImage: NonNullable<BlogPostResponse['post']['images']>[number] | null
  selectedPostCities: string[]
  selectedPostTags: BlogTagResponse[]
  currentProfile: BlogProfileResponse | null
  isOwnProfile: boolean
  profileRelationsHidden: boolean
  displayedMinePosts: BlogPostSummaryResponse[]
  filteredNotifications: BlogNotificationResponse[]
  setActiveTab: (tab: BlogTab) => void
  setMineTab: (tab: MineTab) => void
  setNotificationFilter: (filter: NotificationFilter) => void
  setQuery: (query: string) => void
  setSelectedTag: (tag: BlogTagResponse | null) => void
  setSelectedCities: (cities: string[]) => void
  setDraft: (draft: BlogDraft | ((current: BlogDraft) => BlogDraft)) => void
  setSelectedPost: (post: BlogPostResponse | null) => void
  setSelectedImageIndex: (index: number | ((current: number) => number)) => void
  setCommentDraft: (draft: string) => void
  setProfileSettingsOpen: (open: boolean | ((current: boolean) => boolean)) => void
  toggleCity: (city: string) => void
  toggleDraftCity: (city: string) => void
  toggleDraftTag: (tag: BlogTagResponse) => void
  reloadHome: () => Promise<void>
  reloadMine: () => Promise<void>
  openPost: (postId: string) => Promise<void>
  openProfile: (profileUserId: string) => Promise<void>
  closeProfileOverlay: () => void
  openProfileRelation: (nextTab: ProfileRelationTab) => Promise<void>
  updateProfilePrivacy: (hideRelations: boolean) => Promise<void>
  saveDraft: (
    status: 'draft' | 'publish',
    editorDraft?: Pick<BlogDraft, 'title' | 'summary' | 'content' | 'images'>,
  ) => Promise<void>
  likePost: (post: BlogPostSummaryResponse) => Promise<void>
  favoritePost: (post: BlogPostSummaryResponse) => Promise<void>
  followAuthor: (post: BlogPostSummaryResponse) => Promise<void>
  submitComment: () => Promise<void>
  likeComment: (comment: BlogPostResponse['comments'][number]) => Promise<void>
  handleImages: (files: FileList | null) => Promise<void>
  uploadDraftImage: (imageFile: File) => Promise<ContentImagePlannerResponse>
  removeDraftImage: (imageId: string) => void
  setProfileRelationTab: (tab: ProfileRelationTab | null) => void
  setProfileRelationUsers: (users: BlogProfileUserResponse[]) => void
}
