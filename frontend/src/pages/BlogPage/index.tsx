import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
﻿import { BlogPanel } from '@/pages/BlogPage/components/BlogPanel'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, UserResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'

type BlogPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

export function BlogPage({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: BlogPageProps) {
  const { isBusy, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

  function requireSignedInUser() {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUser
  }

  return (
    <BlogPanel
      currentLanguage={currentLanguage}
      isBusy={isBusy}
      signedInUser={signedInUser}
      translate={translate}
      onListPosts={async (scope, query) => {
        const response = await travelMvpApiClient.listBlogPosts(scope, signedInUser?.userId, query)
        return response.posts
      }}
      onLoadPost={postId => travelMvpApiClient.getBlogPost(postId, signedInUser?.userId)}
      onUploadImage={async imageFile => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () => travelMvpApiClient.uploadBlogImage(nextSignedInUser.userId, imageFile),
          translate('content.imagesUpload'),
          translate('notice.actionSuccess'),
        )
      }}
      onCreatePost={async payload => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () =>
            travelMvpApiClient.createBlogPost({
              userId: nextSignedInUser.userId,
              title: payload.title,
              summary: payload.summary,
              content: payload.content,
              images: payload.images,
            }),
          translate('blog.publish'),
          translate('blog.pendingReviewSuccess'),
        )
      }}
      onUpdatePost={async (postId, payload) => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () =>
            travelMvpApiClient.updateBlogPost(postId, {
              userId: nextSignedInUser.userId,
              title: payload.title,
              summary: payload.summary,
              content: payload.content,
              images: payload.images,
            }),
          translate('blog.save'),
          translate('notice.actionSuccess'),
        )
      }}
      onArchivePost={async postId => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () =>
            travelMvpApiClient.archiveBlogPost(postId, {
              userId: nextSignedInUser.userId,
            }),
          translate('blog.archive'),
          translate('notice.actionSuccess'),
        )
      }}
      onCommentPost={async (postId, content) => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () =>
            travelMvpApiClient.addBlogComment(postId, {
              userId: nextSignedInUser.userId,
              content,
            }),
          translate('blog.submitComment'),
          translate('notice.actionSuccess'),
        )
      }}
      onDeleteComment={async commentId => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () =>
            travelMvpApiClient.deleteBlogComment(commentId, {
              userId: nextSignedInUser.userId,
            }),
          translate('blog.deleteComment'),
          translate('notice.actionSuccess'),
        )
      }}
      onLikePost={async postId => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () =>
            travelMvpApiClient.likeBlogPost(postId, {
              userId: nextSignedInUser.userId,
            }),
          translate('blog.like'),
          translate('notice.actionSuccess'),
        )
      }}
      onUnlikePost={async postId => {
        const nextSignedInUser = requireSignedInUser()
        return runPageActionWithResult(
          () =>
            travelMvpApiClient.unlikeBlogPost(postId, {
              userId: nextSignedInUser.userId,
            }),
          translate('blog.unlike'),
          translate('notice.actionSuccess'),
        )
      }}
    />
  )
}
