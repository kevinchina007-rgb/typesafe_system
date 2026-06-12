// PlannerDefinitionsContentBlog 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.content.domain.*

object PlannerDefinitionsContentBlog:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(BlogSuggestionsPlanner),
        WithConnection(ListBlogPostsPlanner),
        WithConnection(GetBlogPostPlanner),
        WithConnection(CreateBlogPostPlanner),
        WithConnection(UpdateBlogPostPlanner),
        WithConnection(SaveBlogDraftPlanner),
        WithConnection(PublishBlogPostPlanner),
        WithConnection(ApproveBlogPostPlanner),
        WithConnection(RejectBlogPostPlanner),
        WithConnection(ArchiveBlogPostPlanner),
        WithConnection(AddBlogCommentPlanner),
        WithConnection(DeleteBlogCommentPlanner),
        WithConnection(LikeBlogPostPlanner),
        WithConnection(UnlikeBlogPostPlanner),
        WithConnection(LikeBlogCommentPlanner),
        WithConnection(UnlikeBlogCommentPlanner),
        WithConnection(FavoriteBlogPostPlanner),
        WithConnection(UnfavoriteBlogPostPlanner),
        WithConnection(FollowBlogUserPlanner),
        WithConnection(BlockBlogUserPlanner),
        WithConnection(ListBlogNotificationsPlanner),
        WithConnection(GetBlogProfilePlanner),
        WithConnection(UpdateBlogProfilePrivacyPlanner),
        WithConnection(ListBlogFollowersPlanner),
        WithConnection(ListBlogFollowingPlanner)
      ).map(planner => planner.name -> planner).toMap
    )
