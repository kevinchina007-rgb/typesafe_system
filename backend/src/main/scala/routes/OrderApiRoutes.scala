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
import org.typelevel.ci.CIString

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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

    case request @ GET -> Root / "api" / "orders" / orderIdValue / "payment-link" :? PaymentMethodQueryParamMatcher(paymentMethodValue) +& LanguageQueryParamMatcher(languageValue) =>
      for
        currentUserId <- requireCurrentUserId(request)
        paymentMethodText <- Async[F].fromOption(
          paymentMethodValue.map(_.trim).filter(_.nonEmpty),
          SharedValidationError.RequiredFieldWasEmpty("paymentMethod")
        )
        paymentLanguage = normalizePaymentLanguage(languageValue)
        order <- orderRepository.findOrderById(OrderId(orderIdValue)).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(OrderId(orderIdValue))))
        _ <- if order.ownerUserId == currentUserId then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.SessionActorDidNotMatch)
        currentTime <- currentInstantF
        expiresAt = currentTime.plusSeconds(paymentLinkExpirySeconds.toLong)
        paymentMethod = OrderDtoMappers.toPaymentMethod(paymentMethodText)
        paymentToken = createPaymentLinkToken(OrderId(orderIdValue), paymentMethodText, paymentLanguage, expiresAt)
        paymentUrl = s"${resolvePublicBackendOrigin(request)}/pay?token=${URLEncoder.encode(paymentToken, StandardCharsets.UTF_8)}"
        response <- Ok(PaymentLinkResponseDto(paymentUrl = paymentUrl, expiresAt = expiresAt.toString).asJson)
      yield response

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

    case request @ GET -> Root / "pay" :? PaymentTokenQueryParamMatcher(tokenValue) =>
      renderPaymentLinkPage(tokenValue, request)

    case request @ GET -> Root / "pay" / "confirm" :? PaymentTokenQueryParamMatcher(tokenValue) +& PaymentMethodQueryParamMatcher(decisionValue) =>
      confirmPaymentLink(tokenValue, decisionValue, request)
  }

  private val paymentLinkExpirySeconds = 900

  private def renderPaymentLinkPage(tokenValue: Option[String], request: Request[F]): F[Response[F]] =
    tokenValue match
      case None => BadRequest(paymentHtmlDocument(paymentPageText("en").linkTitle, "The payment link is missing.", None, "en"))
      case Some(token) =>
        decodePaymentLinkToken(token).fold(
          errorMessage => BadRequest(paymentHtmlDocument(paymentPageText("en").linkTitle, errorMessage, None, "en")),
          paymentLink =>
            for
              order <- orderRepository.findOrderById(paymentLink.orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(paymentLink.orderId)))
              currentTime <- currentInstantF
              pageText = paymentPageText(paymentLink.language)
              htmlBody =
                if currentTime.isAfter(paymentLink.expiresAt) then
                  paymentHtmlDocument(pageText.expiredTitle, pageText.expiredMessage, None, paymentLink.language)
                else
                  paymentHtmlDocument(
                    pageText.confirmTitle,
                    s"${pageText.orderLabel} ${paymentLink.orderId.value} · ${order.totalBookedMoney.amount} ${order.orderCurrency}",
                    Some(
                      s"""
                         |<div style="display:flex;gap:12px;flex-wrap:wrap;margin-top:16px;">
                         |  <a href="/pay/confirm?token=${URLEncoder.encode(token, StandardCharsets.UTF_8)}&paymentMethod=success" style="${primaryActionStyle}">${pageText.confirmAction}</a>
                         |</div>
                         |<p style="margin-top:16px;color:#607284;">${pageText.methodLabel}: ${paymentLink.paymentMethod}</p>
                         |<p style="color:#607284;">${pageText.expiresAtLabel}: ${paymentLink.expiresAt}</p>
                         |""".stripMargin
                    )
                  ,
                    paymentLink.language
                  )
              response <- Ok(htmlBody).map(_.putHeaders(Header.Raw(CIString("Content-Type"), "text/html; charset=utf-8")))
            yield response
        )

  private def confirmPaymentLink(tokenValue: Option[String], decisionValue: Option[String], request: Request[F]): F[Response[F]] =
    tokenValue match
      case None => BadRequest(paymentHtmlDocument(paymentPageText("en").linkTitle, "The payment link is missing.", None, "en"))
      case Some(token) =>
        decodePaymentLinkToken(token).fold(
          errorMessage => BadRequest(paymentHtmlDocument(paymentPageText("en").linkTitle, errorMessage, None, "en")),
          paymentLink =>
            for
              currentTime <- currentInstantF
              pageText = paymentPageText(paymentLink.language)
              response <-
                if currentTime.isAfter(paymentLink.expiresAt) then
                  BadRequest(paymentHtmlDocument(pageText.expiredTitle, pageText.expiredMessage, None, paymentLink.language))
                else
                  decisionValue.map(_.trim.toLowerCase) match
                    case Some("success") =>
                      orderLifecycleApplicationService
                        .payOrder(
                          orderId = paymentLink.orderId,
                          paymentMethod = OrderDtoMappers.toPaymentMethod(paymentLink.paymentMethod),
                          paymentSucceeded = true,
                          currentTime = currentTime
                        )
                        .flatMap(_ => Ok(paymentHtmlDocument(pageText.successTitle, s"${pageText.orderLabel} ${paymentLink.orderId.value}${pageText.successSuffix}", None, paymentLink.language)).map(_.putHeaders(Header.Raw(CIString("Content-Type"), "text/html; charset=utf-8"))))
                    case _ =>
                      Ok(paymentHtmlDocument(pageText.pendingTitle, s"${pageText.orderLabel} ${paymentLink.orderId.value}${pageText.pendingSuffix}", None, paymentLink.language)).map(_.putHeaders(Header.Raw(CIString("Content-Type"), "text/html; charset=utf-8")))
            yield response
        )

  private def resolvePublicBackendOrigin(request: Request[F]): String =
    sys.env
      .get("TRAVEL_PUBLIC_BACKEND_ORIGIN")
      .map(_.trim)
      .filter(_.nonEmpty)
      .getOrElse {
        val scheme = request.uri.scheme.map(_.value).getOrElse("http")
        val authority = request.uri.authority.map(_.renderString).getOrElse("127.0.0.1:19095")
        s"$scheme://$authority"
      }

  private def createPaymentLinkToken(orderId: OrderId, paymentMethod: String, language: String, expiresAt: Instant): String =
    val payload = s"${orderId.value}|${paymentMethod.trim.toLowerCase}|${language}|${expiresAt.getEpochSecond}"
    val signature = signPaymentPayload(payload)
    s"${base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8))}.${base64UrlEncode(signature)}"

  private def decodePaymentLinkToken(token: String): Either[String, PaymentLinkToken] =
    token.split("\\.", 2).toList match
      case payloadSegment :: signatureSegment :: Nil =>
        val decodedPayload = base64UrlDecode(payloadSegment).map(bytes => String(bytes, StandardCharsets.UTF_8))
        val decodedSignature = base64UrlDecode(signatureSegment)
        for
          payload <- decodedPayload.toRight("The payment token payload is invalid.")
          signature <- decodedSignature.toRight("The payment token signature is invalid.")
          _ <- Either.cond(
            MessageDigest.isEqual(signature, signPaymentPayload(payload)),
            (),
            "The payment token signature does not match."
          )
          tokenValue <- payload.split("\\|", 4).toList match
            case orderIdValue :: paymentMethodValue :: languageValue :: expiresAtEpochSecondValue :: Nil =>
              expiresAtEpochSecondValue.toLongOption
                .map(epochSecond => PaymentLinkToken(OrderId(orderIdValue), paymentMethodValue, normalizePaymentLanguage(Some(languageValue)), Instant.ofEpochSecond(epochSecond)))
                .toRight("The payment token expiry is invalid.")
            case _ =>
              Left("The payment token payload structure is invalid.")
        yield tokenValue
      case _ =>
        Left("The payment token format is invalid.")

  private def signPaymentPayload(payload: String): Array[Byte] =
    val signingSecret = sys.env.get("TRAVEL_PAYMENT_LINK_SECRET").map(_.trim).filter(_.nonEmpty).getOrElse("travel-payment-link-secret")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))

  private def base64UrlEncode(bytes: Array[Byte]): String =
    Base64.getUrlEncoder.withoutPadding().encodeToString(bytes)

  private def base64UrlDecode(value: String): Option[Array[Byte]] =
    Either.catchNonFatal(Base64.getUrlDecoder.decode(value)).toOption

  private def paymentHtmlDocument(title: String, message: String, extraMarkup: Option[String], language: String): String =
    s"""
       |<!DOCTYPE html>
       |<html lang="$language">
       |  <head>
       |    <meta charset="utf-8" />
       |    <meta name="viewport" content="width=device-width, initial-scale=1" />
       |    <title>$title</title>
       |  </head>
       |  <body style="margin:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f4f8fc;color:#203145;">
       |    <main style="max-width:560px;margin:48px auto;padding:28px;border-radius:24px;background:#ffffff;box-shadow:0 16px 40px rgba(32,49,69,0.12);">
       |      <p style="margin:0 0 12px;color:#996126;font-weight:700;text-transform:uppercase;letter-spacing:0.08em;">${paymentPageText(language).brandLabel}</p>
       |      <h1 style="margin:0 0 16px;font-size:30px;">$title</h1>
       |      <p style="margin:0 0 8px;font-size:18px;line-height:1.6;">$message</p>
       |      ${extraMarkup.getOrElse("")}
       |    </main>
       |  </body>
       |</html>
       |""".stripMargin

  private def normalizePaymentLanguage(languageValue: Option[String]): String =
    languageValue.map(_.trim.toLowerCase) match
      case Some("zh") => "zh"
      case _          => "en"

  private def paymentPageText(language: String): PaymentPageText =
    language match
      case "zh" =>
        PaymentPageText(
          brandLabel = "旅行支付",
          linkTitle = "支付链接",
          confirmTitle = "确认支付",
          expiredTitle = "支付链接已过期",
          expiredMessage = "该支付链接已过期，请重新生成二维码。",
          confirmAction = "确认支付",
          methodLabel = "支付方式",
          expiresAtLabel = "过期时间",
          orderLabel = "订单",
          successTitle = "支付完成",
          successSuffix = " 已支付成功。",
          pendingTitle = "支付处理中",
          pendingSuffix = " 仍处于待支付状态。"
        )
      case _ =>
        PaymentPageText(
          brandLabel = "Travel payment",
          linkTitle = "Payment link",
          confirmTitle = "Confirm payment",
          expiredTitle = "Payment link expired",
          expiredMessage = "This payment link has expired. Please generate a new QR code.",
          confirmAction = "Confirm payment",
          methodLabel = "Method",
          expiresAtLabel = "Expires at",
          orderLabel = "Order",
          successTitle = "Payment complete",
          successSuffix = " has been paid successfully.",
          pendingTitle = "Payment pending",
          pendingSuffix = " remains pending payment."
        )

  private val primaryActionStyle =
    "display:inline-block;padding:12px 20px;border-radius:999px;background:linear-gradient(135deg,#1767ff,#1d8cff);color:#ffffff;text-decoration:none;font-weight:600;"

  private val secondaryActionStyle =
    "display:inline-block;padding:12px 20px;border-radius:999px;background:#e7eef8;color:#203145;text-decoration:none;font-weight:600;"

  private final case class PaymentLinkToken(orderId: OrderId, paymentMethod: String, language: String, expiresAt: Instant)
  private final case class PaymentPageText(
      brandLabel: String,
      linkTitle: String,
      confirmTitle: String,
      expiredTitle: String,
      expiredMessage: String,
      confirmAction: String,
      methodLabel: String,
      expiresAtLabel: String,
      orderLabel: String,
      successTitle: String,
      successSuffix: String,
      pendingTitle: String,
      pendingSuffix: String
  )
