// ListFeedbackThreadsPlanner 鏄唴瀹规ā鍧楃殑鍒楄〃鏌ヨ鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.content.domain

import cats.effect.IO
import cats.syntax.apply.*
import cats.syntax.traverse.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection

object ListFeedbackThreadsPlanner extends ConnectionApiPlan[ListFeedbackThreadsPlannerRequest, FeedbackThreadListPlannerResponse]:
  override val name: String = "ListFeedbackThreadsPlanner"
  override def plan(input: ListFeedbackThreadsPlannerRequest, connection: Connection): IO[FeedbackThreadListPlannerResponse] =
    val threads =
      input.userId
        .map(userId => FeedbackPlannerPlainSql.listByOwnerUserId(connection, userId))
        .orElse(
          input.managerType.map { managerType =>
            input.managerActorId.map(_.trim).filter(_.nonEmpty) match
              case Some(managerActorId) =>
                (
                  FeedbackPlannerPlainSql.listServiceReviewsByManagerType(connection, managerType, input.scopeId),
                  FeedbackPlannerPlainSql.listManagerParticipantThreads(connection, managerType, managerActorId)
                ).mapN((serviceThreads, managerThreads) => mergeThreads(serviceThreads, managerThreads))
              case None =>
                FeedbackPlannerPlainSql.listServiceReviewsByManagerType(connection, managerType, input.scopeId)
          }
        )
        .orElse(
          for
            channel <- input.channel
            siteAdminActorId <- input.siteAdminActorId.map(_.trim).filter(_.nonEmpty)
          yield FeedbackPlannerPlainSql.listSiteAdminParticipantThreads(connection, channel, siteAdminActorId)
        )
        .orElse(input.channel.map(channel => FeedbackPlannerPlainSql.listByKind(connection, feedbackKindForChannel(channel))))
        .getOrElse(FeedbackPlannerPlainSql.listAll(connection))

    threads.flatMap(toThreadListResponse(connection))

