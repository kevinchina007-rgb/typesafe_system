// StaticAssetRouter 负责静态资源请求的路由分发。

package com.typesafe.travel.static

import cats.effect.IO
import fs2.Stream
import com.typesafe.travel.persistence.UploadedBinaryAssetReader
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers
import org.typelevel.ci.CIString

import java.nio.file.{Files => NioFiles, Path => FsPath}

final class StaticAssetRouter(
    uploadedBinaryAssetReader: Option[UploadedBinaryAssetReader],
    avatarUploadRootDirectoryPath: FsPath,
    contentUploadRootDirectoryPath: FsPath,
    frontendDistRootDirectoryPath: FsPath
):

  private def serveFrontendAsset(requestPath: String): IO[Response[IO]] =
    val normalizedRequestPath = requestPath.stripPrefix("/")
    val candidatePath =
      if normalizedRequestPath.isEmpty then frontendDistRootDirectoryPath.resolve("index.html")
      else frontendDistRootDirectoryPath.resolve(normalizedRequestPath).normalize()

    val resolvedPath =
      if candidatePath.startsWith(frontendDistRootDirectoryPath) && NioFiles.exists(candidatePath) && !NioFiles.isDirectory(candidatePath) then
        candidatePath
      else
        frontendDistRootDirectoryPath.resolve("index.html").normalize()

    IO.blocking(NioFiles.exists(resolvedPath)).flatMap {
      case false =>
        NotFound()
      case true =>
        IO.blocking(NioFiles.readAllBytes(resolvedPath)).flatMap { fileBytes =>
          Ok(Stream.emits(fileBytes).covary[IO]).map(_.putHeaders(headers.`Content-Type`(inferMediaType(resolvedPath.getFileName.toString))))
        }
    }

  private def serveUploadFile(rootDirectoryPath: FsPath, fileName: String): IO[Response[IO]] =
    if fileName.isEmpty || fileName.contains("\\") || fileName.contains("/") then
      NotFound()
    else
      val filePath = rootDirectoryPath.resolve(fileName).normalize()
      if filePath.startsWith(rootDirectoryPath) then
        IO.blocking(NioFiles.exists(filePath)).flatMap {
          case false =>
            NotFound()
          case true =>
            IO.blocking(NioFiles.readAllBytes(filePath)).flatMap { fileBytes =>
              Ok(Stream.emits(fileBytes).covary[IO]).map(
                _.putHeaders(
                  headers.`Content-Type`(inferMediaType(fileName)),
                  Header.Raw(CIString("Access-Control-Allow-Origin"), "*"),
                  Header.Raw(CIString("Cross-Origin-Resource-Policy"), "cross-origin"),
                  Header.Raw(CIString("Cache-Control"), "no-store, no-cache, must-revalidate"),
                  Header.Raw(CIString("Content-Disposition"), "inline")
                )
              )
            }
        }
      else
        NotFound()

  private def serveUploadedBinaryAsset(assetId: String): IO[Response[IO]] =
    if assetId.isEmpty then
      NotFound()
    else
      uploadedBinaryAssetReader match
        case None => NotFound()
        case Some(assetReader) =>
          assetReader.findAssetByAssetId(assetId).flatMap {
            case None => NotFound()
            case Some(asset) =>
              Ok(Stream.emits(asset.binaryContent).covary[IO]).map(
                _.putHeaders(
                  headers.`Content-Type`(MediaType.unsafeParse(asset.mimeType)),
                  Header.Raw(CIString("Access-Control-Allow-Origin"), "*"),
                  Header.Raw(CIString("Cross-Origin-Resource-Policy"), "cross-origin"),
                  Header.Raw(CIString("Cache-Control"), "no-store, no-cache, must-revalidate"),
                  Header.Raw(CIString("Content-Disposition"), "inline")
                )
              )
          }

  private def inferMediaType(fileName: String): MediaType =
    fileName.toLowerCase match
      case name if name.endsWith(".html") => MediaType.text.html
      case name if name.endsWith(".js")   => MediaType.text.javascript
      case name if name.endsWith(".css")  => MediaType.text.css
      case name if name.endsWith(".json") => MediaType.application.json
      case name if name.endsWith(".svg")  => MediaType.unsafeParse("image/svg+xml")
      case name if name.endsWith(".png")  => MediaType.image.png
      case name if name.endsWith(".jpg")  => MediaType.image.jpeg
      case name if name.endsWith(".jpeg") => MediaType.image.jpeg
      case name if name.endsWith(".webp") => MediaType.unsafeParse("image/webp")
      case name if name.endsWith(".ico")  => MediaType.unsafeParse("image/x-icon")
      case _                              => MediaType.application.`octet-stream`

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "uploads" / "avatars" / fileNameValue =>
      serveUploadFile(avatarUploadRootDirectoryPath, fileNameValue.trim)

    case GET -> Root / "uploads" / "content" / collectionValue / fileNameValue =>
      val normalizedCollection = collectionValue.trim.toLowerCase
      val normalizedFileName = fileNameValue.trim
      if normalizedCollection.isEmpty then NotFound()
      else serveUploadFile(contentUploadRootDirectoryPath.resolve(normalizedCollection).normalize(), normalizedFileName)

    case GET -> Root / "uploads" / "assets" / assetIdValue =>
      serveUploadedBinaryAsset(assetIdValue.trim)

    case GET -> Root / "uploads" / "assets" / assetIdValue / _ =>
      serveUploadedBinaryAsset(assetIdValue.trim)

    case GET -> path =>
      serveFrontendAsset(path.renderString)
  }

object StaticAssetRouter:
  def apply(
      uploadedBinaryAssetReader: Option[UploadedBinaryAssetReader],
      avatarUploadRootDirectoryPath: FsPath,
      contentUploadRootDirectoryPath: FsPath,
      frontendDistRootDirectoryPath: FsPath
  ): StaticAssetRouter =
    new StaticAssetRouter(
      uploadedBinaryAssetReader,
      avatarUploadRootDirectoryPath,
      contentUploadRootDirectoryPath,
      frontendDistRootDirectoryPath
    )
