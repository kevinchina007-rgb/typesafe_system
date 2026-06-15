// 本文件定义 feedback 域对外可见的错误模型，供 planner 和 domain functions 在找不到线程、校验失败或引用缺失时统一抛出。
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
