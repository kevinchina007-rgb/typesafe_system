// AuthSourceJsonCodecs 瀹氫箟璁よ瘉妯″潡鐨勬簮鏁版嵁 JSON codec銆?
package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.Instant
import scala.util.Try

object AuthSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[CredentialId] = Encoder.encodeString.contramap(_.value)
  given Decoder[CredentialId] = Decoder.decodeString.map(CredentialId.apply)

  given Encoder[SessionId] = Encoder.encodeString.contramap(_.value)
  given Decoder[SessionId] = Decoder.decodeString.map(SessionId.apply)

  given Encoder[UserId] = Encoder.encodeString.contramap(_.value)
  given Decoder[UserId] = Decoder.decodeString.map(UserId.apply)

  given Encoder[ManagerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ManagerId] = Decoder.decodeString.map(ManagerId.apply)

  given Encoder[EmailAddress] = Encoder.encodeString.contramap(_.value)
  given Decoder[EmailAddress] = Decoder.decodeString.emap(value => EmailAddress.create(value).left.map(_.getMessage))



