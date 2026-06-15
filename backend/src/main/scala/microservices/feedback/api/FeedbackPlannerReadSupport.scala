// 本文件封装 feedback 域线程列表与线程详情的组装逻辑，把数据库读取结果整理成 planner 返回对象。
package com.typesafe.travel.feedback.domain

import cats.effect.IO
import cats.syntax.traverse.*
import com.typesafe.travel.persistence.feedback.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection

def feedbackKindForChannel(channel: String): FeedbackThreadKind =
  if channel.trim.equalsIgnoreCase("manager") then FeedbackThreadKind.ManagerEscalation
  else FeedbackThreadKind.ServiceReview

def mergeThreads(first: List[FeedbackThread], second: List[FeedbackThread]): List[FeedbackThread] =
  (first ++ second)
    .groupBy(_.threadId.value)
    .values
    .map(_.maxBy(_.updatedAt))
    .toList
    .sortBy(thread => thread.updatedAt)(Ordering[java.time.Instant].reverse)

def requireFeedbackThread(connection: Connection, threadId: SupportTicketId): IO[FeedbackThread] =
  FeedbackPlannerPlainSql.findByThreadId(connection, threadId).flatMap {
    case Some(thread) => IO.pure(thread)
    case None         => IO.raiseError(FeedbackError.ThreadWasNotFound(threadId))
  }

def toThreadDetailsResponse(connection: Connection, thread: FeedbackThread): IO[FeedbackThreadDetailsPlannerResponse] =
  for
    messages <- FeedbackPlannerPlainSql.listMessages(connection, thread.threadId)
    managerActorLogo <- FeedbackPlannerPlainSql.findManagerActorLogoAssetPath(connection, thread.managerType, thread.managerActorId)
    siteAdminActorLogo <- FeedbackPlannerPlainSql.findSiteAdminActorLogoAssetPath(connection, thread.siteAdminActorId)
  yield FeedbackThreadDetailsPlannerResponse(thread, messages, managerActorLogo, siteAdminActorLogo)

def toThreadListResponse(connection: Connection)(threads: List[FeedbackThread]): IO[FeedbackThreadListPlannerResponse] =
  threads.traverse(toThreadDetailsResponse(connection, _)).map(FeedbackThreadListPlannerResponse.apply)
