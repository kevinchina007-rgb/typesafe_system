// 本文件是广告图片候选生成入口，只服务后端广告编辑流程，不对应前端镜像文件。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.sql.Connection
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.ArrayDeque
import javax.imageio.ImageIO
import io.circe.Json
import io.circe.parser.parse

object GenerateAdvertisementImageCandidatesPlanner extends ConnectionApiPlan[GenerateAdvertisementImageCandidatesRequest, GenerateAdvertisementImageCandidatesResponse]:
  override val name: String = "GenerateAdvertisementImageCandidatesPlanner"

  private val httpClient: HttpClient =
    HttpClient
      .newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(Duration.ofSeconds(8))
      .build()

  override def plan(input: GenerateAdvertisementImageCandidatesRequest, connection: Connection): IO[GenerateAdvertisementImageCandidatesResponse] =
    val prompt = buildPrompt(input)
    val width = input.width.getOrElse(960).max(320).min(1600)
    val height = input.height.getOrElse(240).max(120).min(1200)
    val candidateCount = input.candidateCount.getOrElse(1).max(1).min(1)
    val createdAt = Instant.now()

    if prompt.trim.isEmpty then
      IO.raiseError(new IllegalArgumentException("advertisement_image_prompt_required"))
    else
      (0 until candidateCount).toList.parTraverse { index =>
        val seed = 1000 + index * 137
        val generatedImageIo =
          downloadGeneratedImage(prompt, width, height, seed)
            .map(image => maybeMakeBackgroundTransparent(image, input))
            .handleErrorWith { error =>
              IO.blocking(System.err.println(s"[GenerateAdvertisementImageCandidatesPlanner] external image generation failed: ${error.getMessage}")) *>
                IO.pure(localFallbackImage(width, height, seed, input.tone.getOrElse("default"), transparentBackgroundEnabled(input)))
            }
        for
          generatedImage <- generatedImageIo
          uploaded <- AdvertisementPlainSql.uploadImage(
            connection,
            UploadAdvertisementImageRequest(
              originalFileName = s"advertisement-ai-$seed.${extensionFromMimeType(generatedImage.mimeType)}",
              mimeType = generatedImage.mimeType,
              fileContentBase64 = Base64.getEncoder.encodeToString(generatedImage.bytes)
            ),
            createdAt
          )
        yield AdvertisementImageCandidateResponse(
          assetId = uploaded.assetId,
          publicUrl = uploaded.publicUrl,
          prompt = prompt,
          mimeType = uploaded.mimeType,
          seed = seed
        )
      }.map(candidates => GenerateAdvertisementImageCandidatesResponse(candidates))

  private final case class GeneratedImage(bytes: Array[Byte], mimeType: String)

  private def buildPrompt(input: GenerateAdvertisementImageCandidatesRequest): String =
    val prompt = input.prompt.trim
    val supporting = input.supportingCopy.map(_.trim).filter(_.nonEmpty)
    val tone = input.tone.map(_.trim).filter(_.nonEmpty)
    val resourceLabel = input.resourceLabel.map(_.trim).filter(_.nonEmpty)
    val advertisementKind = input.advertisementKind.map(_.trim).filter(_.nonEmpty)
    val imageFactoryKind = input.imageFactoryKind.map(_.trim).filter(_.nonEmpty)
    val transparentBackground = transparentBackgroundEnabled(input)
    val avoidText = input.avoidText.map(_.trim).filter(_.nonEmpty)

    List(
      Some(prompt),
      resourceLabel.map(value => s"focus on $value"),
      tone.map(value => s"overall tone: $value"),
      supporting.map(value => s"visual elements: $value"),
      advertisementKind.map(value => s"advertisement type: $value"),
      imageFactoryKind.map(value => s"image role: $value"),
      Option.when(transparentBackground)("transparent background, isolated subject cutout, keep only the requested subject, full object visible, no cropping, no scene backdrop, no shadow plate, no frame, no text"),
      avoidText.map(value => s"avoid: $value"),
      Some("wide advertising banner composition, leave room for editable overlay text, no watermark, no embedded letters")
    ).flatten.mkString(", ")

  private def transparentBackgroundEnabled(input: GenerateAdvertisementImageCandidatesRequest): Boolean =
    input.imageFactoryKind.exists(_.equalsIgnoreCase("element")) && input.transparentBackground.getOrElse(false)

  private def downloadGeneratedImage(prompt: String, width: Int, height: Int, seed: Int): IO[GeneratedImage] =
    requestParateraImage(prompt, width, height, seed)

  private def requestParateraImage(prompt: String, width: Int, height: Int, seed: Int): IO[GeneratedImage] =
    IO.blocking {
      val apiKey = sys.env.getOrElse("PARATERA_API_KEY", "").trim
      if apiKey.isEmpty then throw new IllegalStateException("paratera_api_key_missing")

      val baseUrl = sys.env.getOrElse("PARATERA_BASE_URL", "https://llmapi.paratera.com").trim.stripSuffix("/")
      val model = sys.env.getOrElse("PARATERA_IMAGE_MODEL", "Doubao-Seedream-4.0").trim
      val payload =
        Json.obj(
          "model" -> Json.fromString(model),
          "prompt" -> Json.fromString(prompt)
        ).noSpaces

      val request =
        HttpRequest
          .newBuilder(URI.create(s"$baseUrl/v1/images/generations"))
          .timeout(Duration.ofMinutes(3))
          .header("Authorization", s"Bearer $apiKey")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
          .build()

      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      if response.statusCode() / 100 != 2 then
        throw new IllegalStateException(s"paratera_request_failed_${response.statusCode()}")

      parseParateraImageResponse(response.body())
    }

  private def parseParateraImageResponse(responseBody: String): GeneratedImage =
    val json = parse(responseBody).fold(throw _, identity)
    val firstData = json.hcursor.downField("data").downArray.focus.getOrElse(throw new IllegalStateException("paratera_missing_image_data"))
    val cursor = firstData.hcursor

    cursor.downField("b64_json").as[String].toOption.filter(_.trim.nonEmpty) match
      case Some(base64Value) =>
        GeneratedImage(Base64.getDecoder.decode(base64Value), "image/png")
      case None =>
        val imageUrl =
          cursor.downField("url").as[String].toOption
            .orElse(cursor.downField("image_url").as[String].toOption)
            .map(_.trim)
            .filter(_.nonEmpty)
            .getOrElse(throw new IllegalStateException("paratera_missing_image_url"))
        downloadImageFromUrl(imageUrl)

  private def downloadImageFromUrl(imageUrl: String): GeneratedImage =
    val request =
      HttpRequest
        .newBuilder(URI.create(imageUrl))
        .timeout(Duration.ofMinutes(3))
        .GET()
        .build()
    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
    if response.statusCode() / 100 != 2 then
      throw new IllegalStateException(s"paratera_download_failed_${response.statusCode()}")

    val mimeType =
      Option(response.headers().firstValue("content-type").orElse(null))
        .map(_.split(";").head.trim)
        .filter(_.nonEmpty)
        .getOrElse("image/png")
    GeneratedImage(response.body(), mimeType)

  private def extensionFromMimeType(mimeType: String): String =
    mimeType.trim.toLowerCase match
      case "image/png" => "png"
      case "image/webp" => "webp"
      case "image/gif" => "gif"
      case "image/svg+xml" => "svg"
      case _ => "jpg"

  private def localFallbackImage(width: Int, height: Int, seed: Int, tone: String, transparentBackground: Boolean): GeneratedImage =
    val (startColor, endColor) =
      tone.toLowerCase match
        case value if value.contains("楂樼骇") || value.contains("premium") => ("#111827", "#d4af37")
        case value if value.contains("娲诲姏") || value.contains("energetic") => ("#be185d", "#fb923c")
        case value if value.contains("娓╂殩") || value.contains("warm") => ("#166534", "#facc15")
        case _ => ("#075985", "#38bdf8")

    val circleX = width - 180 + (seed % 30)
    val circleY = 72 + (seed % 18)
    val pathStartY = height - 58 - (seed % 12)
    val pathMidY = 110 + (seed % 18)
    val pathEndY = 132 - (seed % 10)
    val backgroundLayer =
      if transparentBackground then ""
      else s"""<rect width="$width" height="$height" fill="url(#g)"/>"""
    val subjectLayer =
      if transparentBackground then
        s"""<ellipse cx="${width / 2}" cy="${height / 2}" rx="${Math.max(56, width / 6)}" ry="${Math.max(36, height / 5)}" fill="$endColor" fill-opacity="0.92"/>
           |  <path d="M${width / 2 - 90} ${height / 2 + 16} C${width / 2 - 30} ${height / 2 - 42}, ${width / 2 + 26} ${height / 2 - 38}, ${width / 2 + 90} ${height / 2 + 12}" fill="none" stroke="rgba(255,255,255,0.58)" stroke-width="10" stroke-linecap="round"/>""".stripMargin
      else
        s"""<circle cx="$circleX" cy="$circleY" r="92" fill="rgba(255,255,255,0.16)"/>
           |  <path d="M80 $pathStartY C250 108, 390 ${height - 24}, 560 $pathMidY S${width - 140} 76, ${width - 40} $pathEndY" fill="none" stroke="rgba(255,255,255,0.34)" stroke-width="12" stroke-linecap="round"/>""".stripMargin
    val svg =
      s"""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
         |  <defs>
         |    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
         |      <stop stop-color="$startColor" offset="0"/>
         |      <stop stop-color="$endColor" offset="1"/>
         |    </linearGradient>
         |  </defs>
         |  $backgroundLayer
         |  $subjectLayer
         |</svg>""".stripMargin

    GeneratedImage(svg.getBytes(StandardCharsets.UTF_8), if transparentBackground then "image/svg+xml" else "image/svg+xml")

  private def maybeMakeBackgroundTransparent(image: GeneratedImage, input: GenerateAdvertisementImageCandidatesRequest): GeneratedImage =
    if !transparentBackgroundEnabled(input) then image
    else
      try removeBackgroundToTransparentPng(image)
      catch case _: Throwable => image

  private def removeBackgroundToTransparentPng(image: GeneratedImage): GeneratedImage =
    val buffered = ImageIO.read(new ByteArrayInputStream(image.bytes))
    if buffered == null then image
    else
      val width = buffered.getWidth
      val height = buffered.getHeight
      val argb = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB)
      val graphics = argb.createGraphics()
      graphics.drawImage(buffered, 0, 0, null)
      graphics.dispose()

      val backgroundColor = averageCornerColor(argb)
      val queue = ArrayDeque[(Int, Int)]()
      val visited = Array.ofDim[Boolean](width, height)

      def enqueue(x: Int, y: Int): Unit =
        if x >= 0 && x < width && y >= 0 && y < height && !visited(x)(y) then
          visited(x)(y) = true
          if colorDistance(argb.getRGB(x, y), backgroundColor) <= 42 then queue.addLast((x, y))

      (0 until width).foreach { x =>
        enqueue(x, 0)
        enqueue(x, height - 1)
      }
      (0 until height).foreach { y =>
        enqueue(0, y)
        enqueue(width - 1, y)
      }

      var removedPixels = 0
      while !queue.isEmpty do
        val (x, y) = queue.removeFirst()
        val current = argb.getRGB(x, y)
        if ((current >>> 24) & 0xff) != 0 then
          argb.setRGB(x, y, current & 0x00ffffff)
          removedPixels += 1
          enqueue(x + 1, y)
          enqueue(x - 1, y)
          enqueue(x, y + 1)
          enqueue(x, y - 1)

      if removedPixels == 0 then image
      else
        val out = ByteArrayOutputStream()
        ImageIO.write(argb, "png", out)
        GeneratedImage(out.toByteArray, "image/png")

  private def averageCornerColor(image: java.awt.image.BufferedImage): Int =
    val corners = List(
      image.getRGB(0, 0),
      image.getRGB(image.getWidth - 1, 0),
      image.getRGB(0, image.getHeight - 1),
      image.getRGB(image.getWidth - 1, image.getHeight - 1)
    )
    val red = corners.map(rgb => (rgb >> 16) & 0xff).sum / corners.size
    val green = corners.map(rgb => (rgb >> 8) & 0xff).sum / corners.size
    val blue = corners.map(rgb => rgb & 0xff).sum / corners.size
    (0xff << 24) | (red << 16) | (green << 8) | blue

  private def colorDistance(left: Int, right: Int): Int =
    val r = Math.abs(((left >> 16) & 0xff) - ((right >> 16) & 0xff))
    val g = Math.abs(((left >> 8) & 0xff) - ((right >> 8) & 0xff))
    val b = Math.abs((left & 0xff) - (right & 0xff))
    Math.max(r, Math.max(g, b))

  private def escapeXml(value: String): String =
    value
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
