// 本文件提供 content 域的投诉管理会话打开入口。
package com.typesafe.travel.feedback.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.persistence.feedback.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection

object OpenComplaintManagerThreadPlanner extends ConnectionApiPlan[OpenComplaintManagerThreadPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "OpenComplaintManagerThreadPlanner"
  override def plan(input: OpenComplaintManagerThreadPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    val messageId = SupportMessageId(input.complaintMessageId)
    for
      siteThreads <- FeedbackPlannerPlainSql.listSiteAdminParticipantThreads(connection, "user", input.siteAdminActorId)
      cardMessage <- siteThreads
        .map(thread => FeedbackPlannerPlainSql.findMessage(connection, thread.threadId, messageId))
        .foldLeft(IO.pure(Option.empty[FeedbackMessage])) { (current, next) =>
          current.flatMap {
            case found @ Some(_) => IO.pure(found)
            case None            => next
          }
        }
      payload <- cardMessage.flatMap(_.complaintPayload) match
        case Some(found) => IO.pure(found)
        case None        => IO.raiseError(new IllegalArgumentException(s"Complaint card '${input.complaintMessageId}' was not found"))
      managerThreadId <- payload.managerThreadId match
        case Some(threadId) => IO.pure(SupportTicketId(threadId))
        case None           => IO.raiseError(new IllegalArgumentException("Complaint card does not have a manager thread"))
      managerThread <- requireFeedbackThread(connection, managerThreadId)
      response <- toThreadDetailsResponse(connection, managerThread)
    yield response

