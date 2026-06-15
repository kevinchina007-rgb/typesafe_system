// 博客域前端 API 统一导出入口，同时保留运行时模块清单。

import * as addBlogCommentPlanner from './AddBlogCommentPlanner'
import * as approveBlogPostPlanner from './ApproveBlogPostPlanner'
import * as archiveBlogPostPlanner from './ArchiveBlogPostPlanner'
import * as blockBlogUserPlanner from './BlockBlogUserPlanner'
import * as blogSuggestionsPlanner from './BlogSuggestionsPlanner'
import * as createBlogPostPlanner from './CreateBlogPostPlanner'
import * as deleteBlogCommentPlanner from './DeleteBlogCommentPlanner'
import * as favoriteBlogPostPlanner from './FavoriteBlogPostPlanner'
import * as followBlogUserPlanner from './FollowBlogUserPlanner'
import * as getBlogPostPlanner from './GetBlogPostPlanner'
import * as getBlogProfilePlanner from './GetBlogProfilePlanner'
import * as likeBlogCommentPlanner from './LikeBlogCommentPlanner'
import * as likeBlogPostPlanner from './LikeBlogPostPlanner'
import * as listBlogFollowersPlanner from './ListBlogFollowersPlanner'
import * as listBlogFollowingPlanner from './ListBlogFollowingPlanner'
import * as listBlogNotificationsPlanner from './ListBlogNotificationsPlanner'
import * as listBlogPostsPlanner from './ListBlogPostsPlanner'
import * as publishBlogPostPlanner from './PublishBlogPostPlanner'
import * as rejectBlogPostPlanner from './RejectBlogPostPlanner'
import * as saveBlogDraftPlanner from './SaveBlogDraftPlanner'
import * as unfavoriteBlogPostPlanner from './UnfavoriteBlogPostPlanner'
import * as unlikeBlogCommentPlanner from './UnlikeBlogCommentPlanner'
import * as unlikeBlogPostPlanner from './UnlikeBlogPostPlanner'
import * as updateBlogPostPlanner from './UpdateBlogPostPlanner'
import * as updateBlogProfilePrivacyPlanner from './UpdateBlogProfilePrivacyPlanner'
import * as uploadBlogImagePlanner from './UploadBlogImagePlanner'

export const blogApiModules = Object.freeze({
  addBlogCommentPlanner,
  approveBlogPostPlanner,
  archiveBlogPostPlanner,
  blockBlogUserPlanner,
  blogSuggestionsPlanner,
  createBlogPostPlanner,
  deleteBlogCommentPlanner,
  favoriteBlogPostPlanner,
  followBlogUserPlanner,
  getBlogPostPlanner,
  getBlogProfilePlanner,
  likeBlogCommentPlanner,
  likeBlogPostPlanner,
  listBlogFollowersPlanner,
  listBlogFollowingPlanner,
  listBlogNotificationsPlanner,
  listBlogPostsPlanner,
  publishBlogPostPlanner,
  rejectBlogPostPlanner,
  saveBlogDraftPlanner,
  unfavoriteBlogPostPlanner,
  unlikeBlogCommentPlanner,
  unlikeBlogPostPlanner,
  updateBlogPostPlanner,
  updateBlogProfilePrivacyPlanner,
  uploadBlogImagePlanner,
})

export * from './AddBlogCommentPlanner'
export * from './ApproveBlogPostPlanner'
export * from './ArchiveBlogPostPlanner'
export * from './BlockBlogUserPlanner'
export * from './BlogSuggestionsPlanner'
export * from './CreateBlogPostPlanner'
export * from './DeleteBlogCommentPlanner'
export * from './FavoriteBlogPostPlanner'
export * from './FollowBlogUserPlanner'
export * from './GetBlogPostPlanner'
export * from './GetBlogProfilePlanner'
export * from './LikeBlogCommentPlanner'
export * from './LikeBlogPostPlanner'
export * from './ListBlogFollowersPlanner'
export * from './ListBlogFollowingPlanner'
export * from './ListBlogNotificationsPlanner'
export * from './ListBlogPostsPlanner'
export * from './PublishBlogPostPlanner'
export * from './RejectBlogPostPlanner'
export * from './SaveBlogDraftPlanner'
export * from './UnfavoriteBlogPostPlanner'
export * from './UnlikeBlogCommentPlanner'
export * from './UnlikeBlogPostPlanner'
export * from './UpdateBlogPostPlanner'
export * from './UpdateBlogProfilePrivacyPlanner'
export * from './UploadBlogImagePlanner'
