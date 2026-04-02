package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

trait OrderApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def orderRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "api" / "users" / userIdValue / "orders" =>
      for
        currentUserId <- requireCurrentUserId(request)
        _ <- if currentUserId == UserId(userIdValue) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.SessionActorDidNotMatch)
        currentTime <- currentInstantF
        orders <- orderLifecycleApplicationService.listOrdersForUser(UserId(userIdValue), currentTime)
        orderResponses <- orders.traverse(toOrderResponseDto)
        response <- Ok(OrderListResponseDto(orderResponses).asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" =>
      for
        currentUserId <- requireCurrentUserId(request)
        createOrderRequestDto <- request.as[CreateOrderRequestDto]
        createdAt <- currentInstantF
        createdOrder <- orderService.createDraftOrder(
          ownerUserId = currentUserId,
          orderCurrency = OrderDtoMappers.toCurrency(createOrderRequestDto.orderCurrency),
          createdAt = createdAt
        )
        response <- Created(OrderResponseDto.fromDomain(createdOrder).asJson)
      yield response

    case GET -> Root / "api" / "orders" / orderIdValue =>
      currentInstantF.flatMap { currentTime =>
        orderLifecycleApplicationService.getOrder(OrderId(orderIdValue), currentTime).flatMap { foundOrder =>
          toOrderResponseDto(foundOrder).flatMap(orderResponseDto => Ok(orderResponseDto.asJson))
        }
      }.handleErrorWith {
        case throwable: Throwable => handleDomainError(throwable)
      }

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "submit" =>
      for
        _ <- requireCurrentUserId(request)
        order <- orderService.submitOrderForPayment(OrderId(orderIdValue))
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "pay" =>
      for
        _ <- requireCurrentUserId(request)
        authorizePaymentRequestDto <- request.as[PayOrderRequestDto]
        currentTime <- currentInstantF
        orderAfterPaymentAuthorization <- orderLifecycleApplicationService.payOrder(
          orderId = OrderId(orderIdValue),
          paymentMethod = OrderDtoMappers.toPaymentMethod(authorizePaymentRequestDto.paymentMethod),
          paymentSucceeded = authorizePaymentRequestDto.paymentSucceeded,
          currentTime = currentTime
        )
        orderResponseDto <- toOrderResponseDto(orderAfterPaymentAuthorization)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "cancel" =>
      for
        _ <- requireCurrentUserId(request)
        cancelledAt <- currentInstantF
        order <- orderLifecycleApplicationService.cancelOrder(OrderId(orderIdValue), cancelledAt)
        orderResponseDto <- toOrderResponseDto(order)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case request @ POST -> Root / "api" / "orders" / orderIdValue / "refunds" =>
      for
        _ <- requireCurrentUserId(request)
        requestRefundRequestDto <- request.as[RequestRefundRequestDto]
        existingOrder <- orderRepository.findOrderById(OrderId(orderIdValue)).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(OrderId(orderIdValue))))
        updatedOrder <- if existingOrder.orderLineItems.exists(_.isInstanceOf[TrainOrderItem]) then
          for
            requestedAt <- currentInstantF
            refundAmount <- trainBookingApplicationService.calculateRefundAmountForOrder(OrderId(orderIdValue), requestedAt)
            order <- orderService.requestOrderRefund(
              orderId = OrderId(orderIdValue),
              refundAmount = refundAmount,
              refundReason = requestRefundRequestDto.refundReason,
              requestedAt = requestedAt
            )
          yield order
        else
          currentInstantF.flatMap { requestedAt =>
            orderService.requestCustomerRefund(
              orderId = OrderId(orderIdValue),
              refundReason = requestRefundRequestDto.refundReason,
              requestedAt = requestedAt
            )
          }
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Ok(orderResponseDto.asJson)
      yield response

    case POST -> Root / "api" / "orders" / orderIdValue / "refunds" / refundIdValue / "approve" =>
      currentInstantF.flatMap { approvedAt =>
        orderService
          .approveRequestedRefund(OrderId(orderIdValue), RefundId(refundIdValue), approvedAt)
          .flatMap(order => toOrderResponseDto(order).flatMap(orderResponseDto => Ok(orderResponseDto.asJson)))
      }

    case POST -> Root / "api" / "orders" / orderIdValue / "refunds" / refundIdValue / "settle" =>
      currentInstantF.flatMap { settledAt =>
        orderService
          .settleApprovedRefund(OrderId(orderIdValue), RefundId(refundIdValue), settledAt)
          .flatMap(order => toOrderResponseDto(order).flatMap(orderResponseDto => Ok(orderResponseDto.asJson)))
      }
  }
