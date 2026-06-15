// FeedbackErrors 定义内容模块的错误模型。

package com.typesafe.travel.feedback.domain

import com.typesafe.travel.shared.kernel.*

sealed trait FeedbackError extends DomainError:
  def message: String

object FeedbackError:
  final case class ThreadWasNotFound(threadId: SupportTicketId) extends FeedbackError:
    override def message: String = s"Feedback thread '${threadId.value}' was not found"

  final case class ThreadBodyWasInvalid(threadId: SupportTicketId) extends FeedbackError:
    override def message: String = s"Feedback thread '${threadId.value}' requires a non-empty message"

  final case class ReviewWasNotFound(reviewId: ReviewId) extends FeedbackError:
    override def message: String = s"Feedback thread could not be created because review '${reviewId.value}' was not found"
