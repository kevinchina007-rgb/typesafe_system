package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.api.storage.AvatarStorage
import com.typesafe.travel.identity.domain.{User, UserService}
import com.typesafe.travel.shared.kernel.*

enum AvatarApplicationError(val message: String) extends DomainError:
  case AvatarWasMissing
      extends AvatarApplicationError("Avatar file was missing")
  case AvatarFileTypeWasInvalid(contentTypeValue: String)
      extends AvatarApplicationError(s"Avatar content type '$contentTypeValue' is not supported")
  case AvatarFileExtensionWasInvalid(fileNameValue: String)
      extends AvatarApplicationError(s"Avatar file '$fileNameValue' is not supported")
  case AvatarFileWasTooLarge(maximumBytes: Long, actualBytes: Long)
      extends AvatarApplicationError(s"Avatar file exceeded the maximum size of $maximumBytes bytes with $actualBytes bytes")
  case AvatarUploadFailed(userId: UserId)
      extends AvatarApplicationError(s"Avatar upload failed for user '${userId.value}'")

trait AvatarApplicationService[F[_]]:
  def uploadUserAvatar(
      userId: UserId,
      originalFileName: String,
      contentTypeValue: String,
      fileBytes: Array[Byte]
  ): F[User]

final class LiveAvatarApplicationService[F[_]: MonadThrow](
    userService: UserService[F],
    avatarStorage: AvatarStorage[F]
) extends AvatarApplicationService[F]:
  private val allowedContentTypes =
    Set("image/png", "image/jpeg", "image/jpg")

  private val allowedFileExtensions =
    Set("png", "jpg", "jpeg")

  private val maximumAvatarBytes: Long = 2L * 1024L * 1024L

  override def uploadUserAvatar(
      userId: UserId,
      originalFileName: String,
      contentTypeValue: String,
      fileBytes: Array[Byte]
  ): F[User] =
    for
      _ <- validateAvatarPresence(originalFileName, fileBytes)
      _ <- validateContentType(contentTypeValue)
      fileExtension <- validateFileExtension(originalFileName)
      _ <- validateFileSize(fileBytes)
      storedAvatarFile <- avatarStorage.storeAvatar(userId, fileExtension, fileBytes)
      avatarUrl <- AvatarUrl.create(storedAvatarFile.publicUrl).liftTo[F]
      updatedUser <- userService.updateUserAvatar(userId, avatarUrl)
    yield updatedUser

  private def validateAvatarPresence(originalFileName: String, fileBytes: Array[Byte]): F[Unit] =
    if originalFileName.trim.nonEmpty && fileBytes.nonEmpty then MonadThrow[F].unit
    else MonadThrow[F].raiseError(AvatarApplicationError.AvatarWasMissing)

  private def validateContentType(contentTypeValue: String): F[String] =
    val normalizedContentType = contentTypeValue.trim.toLowerCase
    if allowedContentTypes.contains(normalizedContentType) then MonadThrow[F].pure(normalizedContentType)
    else MonadThrow[F].raiseError(AvatarApplicationError.AvatarFileTypeWasInvalid(contentTypeValue))

  private def validateFileExtension(originalFileName: String): F[String] =
    val normalizedFileName = originalFileName.trim.toLowerCase
    val extensionValue = normalizedFileName.split('.').lastOption.getOrElse("")
    if allowedFileExtensions.contains(extensionValue) then MonadThrow[F].pure(extensionValue)
    else MonadThrow[F].raiseError(AvatarApplicationError.AvatarFileExtensionWasInvalid(originalFileName))

  private def validateFileSize(fileBytes: Array[Byte]): F[Unit] =
    if fileBytes.length.toLong <= maximumAvatarBytes then MonadThrow[F].unit
    else MonadThrow[F].raiseError(AvatarApplicationError.AvatarFileWasTooLarge(maximumAvatarBytes, fileBytes.length.toLong))
