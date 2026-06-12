import type { ContentImageResponse } from '@/lib/mvp-types/index'
import type { BlogNotificationResponse } from '@/microservices/content/objects/BlogNotificationResponse'
import type { BlogPostResponse } from '@/microservices/content/objects/BlogPostResponse'
import type { BlogPostSummaryResponse, BlogTagResponse } from '@/microservices/content/objects/BlogPostSummaryResponse'
import type { BlogTab, MineTab, NotificationFilter } from '@/pages/BlogPage/objects'

// 左侧导航项，直接驱动 Blog 页签切换。
export const sidebarItems: Array<{ key: BlogTab; label: string }> = [
  { key: 'home', label: '主页' },
  { key: 'publish', label: '发布' },
  { key: 'notifications', label: '通知' },
  { key: 'mine', label: '我的' },
]

// 首页筛选区的标签分组定义.
export const tagGroups = [
  { type: 'days', label: '行程天数', values: ['1-2天', '3-5天', '6天以上'] },
  { type: 'season', label: '出发时间', values: ['春夏', '秋冬', '节假日'] },
  { type: 'companion', label: '和谁出行', values: ['一个人', '朋友', '亲子/情侣'] },
  { type: 'style', label: '旅行玩法', values: ['美食', '省钱', '拍照打卡'] },
]

// 首页和发布页共享的城市筛选列表.
export const travelCities = ['北京', '上海', '武汉', '南京', '杭州', '深圳', '重庆', '广州', '成都', '长沙', '厦门', '西安']

// 通知页顶部的筛选按钮配置.
export const notificationFilters: Array<{ key: NotificationFilter; label: string; types: string[] }> = [
  { key: 'comments', label: '回复与评论', types: ['comment', 'reply', 'postCommented'] },
  { key: 'likes', label: '收到喜欢', types: ['like', 'favorite', 'postLiked', 'postFavorited'] },
  { key: 'followers', label: '新增粉丝', types: ['follow', 'userFollowed'] },
]

// 空草稿的初始值.
export const emptyDraft = {
  postId: '',
  title: '',
  summary: '',
  coverText: '',
  content: '',
  travelCities: [] as string[],
  images: [] as ContentImageResponse[],
  tags: [] as BlogTagResponse[],
}

// 生成头像或首字母兜底文本.
export function buildFallbackInitials(label?: string | null) {
  return (label ?? '').trim().slice(0, 1).toUpperCase() || '旅'
}

// 取作者显示名，兜底到通用文案.
export function getAuthorDisplayName(post: BlogPostSummaryResponse) {
  return post.authorDisplayName || '旅行用户'
}

// 获取卡片封面图，优先使用封面图，其次用首图.
export function getPostCoverImage(post: BlogPostSummaryResponse) {
  return post.coverImageUrl ?? post.images?.[0]?.publicUrl ?? null
}

// 获取卡片展示文案，按摘要、标题、默认文案顺序兜底.
export function getPostCoverText(post: BlogPostSummaryResponse) {
  return post.coverText || post.summary || post.title || '这趟旅行还没写封面语'
}

// 统一返回帖子的标签数组.
export function getPostTags(post: BlogPostSummaryResponse) {
  return Array.isArray(post.tags) ? post.tags : []
}

// 统一返回帖子的城市数组.
export function getPostCities(post: BlogPostSummaryResponse) {
  const cities = Array.isArray(post.travelCities) ? post.travelCities : []
  return cities.length > 0 ? cities : post.travelCity ? [post.travelCity] : []
}

// 把时间格式化成短日期展示.
export function formatShortDate(value?: string | null) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 根据筛选项返回通知类型集合.
export function buildNotificationTypeSet(filterKey: NotificationFilter) {
  return notificationFilters.find(filter => filter.key === filterKey)?.types ?? []
}

// 按通知类型筛出当前页要显示的通知.
export function filterNotifications(
  notifications: BlogNotificationResponse[],
  filterKey: NotificationFilter,
) {
  const activeNotificationTypes = buildNotificationTypeSet(filterKey)
  return notifications.filter(notification => activeNotificationTypes.includes(notification.notificationType))
}

// 决定我的页面当前应该展示哪组帖子.
export function getDisplayedMinePosts(
  mineTab: MineTab,
  isOwnProfile: boolean,
  viewedProfileExists: boolean,
  favoritePosts: BlogPostSummaryResponse[],
  viewedProfilePosts: BlogPostSummaryResponse[],
  myPosts: BlogPostSummaryResponse[],
) {
  return mineTab === 'favorites' && isOwnProfile ? favoritePosts : viewedProfileExists ? viewedProfilePosts : myPosts
}

// 读取选中帖子的城市数组.
export function getSelectedPostCities(selectedPost: BlogPostResponse | null) {
  return selectedPost ? getPostCities(selectedPost.post) : []
}

// 读取选中帖子的标签数组.
export function getSelectedPostTags(selectedPost: BlogPostResponse | null) {
  return selectedPost ? getPostTags(selectedPost.post) : []
}

// 读取选中帖子的图片数组.
export function getSelectedImages(selectedPost: BlogPostResponse | null) {
  return selectedPost?.post.images ?? []
}

// 读取选中图片索引对应的图片.
export function getSelectedImage(selectedPost: BlogPostResponse | null, selectedImageIndex: number) {
  const selectedImages = getSelectedImages(selectedPost)
  return selectedImages[selectedImageIndex] ?? selectedImages[0] ?? null
}

// 生成选中标签的展示文本.
export function getSelectedTagLabel(selectedTag: BlogTagResponse | null) {
  return selectedTag ? `${selectedTag.tagType}:${selectedTag.tagValue}` : '全部'
}

// 生成选中城市的展示文本.
export function getSelectedCityLabel(selectedCities: string[]) {
  return selectedCities.join('|') || '全部城市'
}

// 获取当前通知筛选对应的类型集合.
export function getActiveNotificationTypes(filterKey: NotificationFilter) {
  return buildNotificationTypeSet(filterKey)
}

// 判断个人主页的关系列表是否需要隐藏.
export function getProfileRelationsHidden(currentProfile: { relationListHidden?: boolean } | null, isOwnProfile: boolean) {
  return Boolean(currentProfile?.relationListHidden && !isOwnProfile)
}

// 判断当前查看的主页是不是自己的主页.
export function getIsOwnProfile(signedInUserId: string | null | undefined, currentProfileUserId: string | null | undefined) {
  return Boolean(signedInUserId && currentProfileUserId && currentProfileUserId === signedInUserId)
}
