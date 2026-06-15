// ListBlogNotificationsPlanner 是内容模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object ListBlogNotificationsPlanner extends ConnectionApiPlan[ListBlogNotificationsPlannerRequest, BlogNotificationListPlannerResponse]:
  override val name: String = "ListBlogNotificationsPlanner"
  override def plan(input: ListBlogNotificationsPlannerRequest, connection: Connection): IO[BlogNotificationListPlannerResponse] =
    BlogPlannerPlainSql.listNotifications(connection, input.userId)
