// ListBlogNotificationsPlanner：博客域博客通知列表入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object ListBlogNotificationsPlanner extends ConnectionApiPlan[ListBlogNotificationsPlannerRequest, BlogNotificationListPlannerResponse]:
  override val name: String = "ListBlogNotificationsPlanner"
  override def plan(input: ListBlogNotificationsPlannerRequest, connection: Connection): IO[BlogNotificationListPlannerResponse] =
    BlogPlannerPlainSql.listNotifications(connection, input.userId)
