// OpenComplaintManagerThreadPlanner 鏄唴瀹规ā鍧楃殑鎵撳紑鍏ュ彛锛岃礋璐ｈ姹傛牎楠屻€佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
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

