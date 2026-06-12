// CreateFeedbackComplaintPlanner 是内容模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSql
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.Instant

object CreateFeedbackComplaintPlanner extends ConnectionApiPlan[CreateFeedbackComplaintPlannerRequest, FeedbackThreadDetailsPlannerResponse]:
  override val name: String = "CreateFeedbackComplaintPlanner"
  override def plan(input: CreateFeedbackComplaintPlannerRequest, connection: Connection): IO[FeedbackThreadDetailsPlannerResponse] =
    val sourceThreadId = SupportTicketId(input.sourceThreadId)
    for
      sourceThread <- requireFeedbackThread(connection, sourceThreadId)
      _ <-
        if sourceThread.ownerUserId.isDefined then IO.unit
        else IO.raiseError(new IllegalArgumentException("Only user feedback threads can be complained about"))
      selectedMessages <- FeedbackPlannerPlainSql.listMessagesByIds(connection, sourceThreadId, input.selectedMessageIds)
      _ <-
        if selectedMessages.nonEmpty then IO.unit
        else IO.raiseError(new IllegalArgumentException("Please select at least one message to complain about"))
      _ <-
        if input.userExplanation.trim.nonEmpty then IO.unit
        else IO.raiseError(new IllegalArgumentException("Complaint explanation is required"))
      siteAdminActorId <- FeedbackPlannerPlainSql.findDefaultSiteAdminActorId(connection).map(_.getOrElse("site-admin"))
      managerActorId <- FeedbackPlannerPlainSql.findManagerActorForFeedbackThread(connection, sourceThread).flatMap {
        case Some(actorId) => IO.pure(actorId)
        case None          => IO.raiseError(new IllegalArgumentException("Could not find the manager account for this complaint"))
      }
      now = Instant.now()
      targetDisplayName = complaintTargetDisplayName(sourceThread)
      managerThread <- FeedbackPlannerPlainSql.findManagerSiteAdminThread(connection, managerActorId, siteAdminActorId).map {
        case Some(existingThread) => existingThread
        case None => createComplaintManagerThread(sourceThread, managerActorId, siteAdminActorId, targetDisplayName, now)
      }
      userThread = createComplaintUserThread(sourceThread, siteAdminActorId, targetDisplayName, now)
      complaintId = s"complaint-${java.util.UUID.randomUUID().toString.take(12)}"
      snapshots = selectedMessages.map(toComplaintSnapshot)
      payload = ComplaintCardPayload(
        complaintId = complaintId,
        sourceThreadId = sourceThread.threadId.value,
        managerThreadId = Some(managerThread.threadId.value),
        userExplanation = input.userExplanation.trim,
        summary = buildComplaintSummary(selectedMessages),
        targetDisplayName = targetDisplayName,
        selectedMessages = snapshots,
        createdAt = now.toString
      )
      userCard = createComplaintCardMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        userThread.threadId,
        payload,
        now
      )
      managerCard = createComplaintCardMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        managerThread.threadId,
        payload,
        now
      )
      userNotice = createComplaintSystemMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        userThread.threadId,
        s"Complaint has been submitted to site admin. Target: $targetDisplayName",
        now
      )
      managerNotice = createComplaintSystemMessage(
        SupportMessageId(s"support-message-${java.util.UUID.randomUUID().toString.take(12)}"),
        managerThread.threadId,
        s"The user submitted a complaint about $targetDisplayName. Site admin can handle it here.",
        now
      )
      savedUserThread = userThread.copy(unreadBySiteAdmin = 1, updatedAt = now)
      savedManagerThread = managerThread.copy(unreadByManager = managerThread.unreadByManager + 1, updatedAt = now)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, savedUserThread)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, userNotice)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, userCard)
      _ <- FeedbackPlannerPlainSql.saveThread(connection, savedManagerThread)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, managerNotice)
      _ <- FeedbackPlannerPlainSql.insertMessage(connection, managerCard)
      response <- toThreadDetailsResponse(connection, savedUserThread)
    yield response
