package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import io.circe.Json
import io.circe.parser.parse

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.sql.Connection

object GenerateAdvertisementTextCandidatesPlanner extends ConnectionApiPlan[GenerateAdvertisementTextCandidatesRequest, GenerateAdvertisementTextCandidatesResponse]:
  override val name: String = "GenerateAdvertisementTextCandidatesPlanner"

  private val httpClient: HttpClient =
    HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

  override def plan(input: GenerateAdvertisementTextCandidatesRequest, connection: Connection): IO[GenerateAdvertisementTextCandidatesResponse] =
    val sourceText = normalizedSourceText(input)
    val candidateCount = input.candidateCount.getOrElse(3).max(1).min(3)

    if sourceText.isEmpty then
      IO.raiseError(new IllegalArgumentException("advertisement_text_prompt_required"))
    else
      requestTextCandidates(input, candidateCount).handleError { _ =>
        GenerateAdvertisementTextCandidatesResponse(localFallbackCandidates(input, candidateCount))
      }

  private def requestTextCandidates(
      input: GenerateAdvertisementTextCandidatesRequest,
      candidateCount: Int
  ): IO[GenerateAdvertisementTextCandidatesResponse] =
    IO.blocking {
      val apiKey = sys.env.getOrElse("POLLINATIONS_API_KEY", "")
      val body = buildRequestBody(input, candidateCount)
      val requestBuilder =
        HttpRequest
          .newBuilder(URI.create("https://gen.pollinations.ai/v1/chat/completions"))
          .header("Content-Type", "application/json")

      if apiKey.nonEmpty then requestBuilder.header("Authorization", s"Bearer $apiKey")

      val request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build()
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      if response.statusCode() / 100 != 2 then
        throw new IllegalStateException(s"pollinations_text_failed_${response.statusCode()}")

      val generatedContent = extractContent(response.body())
      parseTextCandidates(generatedContent, input, candidateCount)
    }

  private def buildRequestBody(input: GenerateAdvertisementTextCandidatesRequest, candidateCount: Int): String =
    val sourceText = normalizedSourceText(input)
    val styleRequirement = normalizedStyleRequirement(input)

    val systemPrompt =
      s"""You design artistic Chinese display-text styles for an advertisement editor.
         |Return strict JSON only, in the shape:
         |{"candidates":[{"text":"...","emphasis":"..."},{"text":"...","emphasis":"..."}]}
         |Rules:
         |- exactly $candidateCount candidates
         |- every candidate text must be exactly the same as the provided source text
         |- do not rewrite, shorten, expand, or translate the source text
         |- emphasis should briefly describe the visual style treatment in Chinese
         |- emphasis should stay under 18 Chinese characters if possible
         |- do not include markdown
         |- do not include numbering
         |- do not include explanation outside JSON
         |- suitable for editable banner text styles, not copywriting alternatives
         |""".stripMargin

    val userPrompt =
      List(
        Some(s"""我希望得到当前文本：“$sourceText”的艺术字体。"""),
        Some(s"""要求：“$styleRequirement”"""),
        input.resourceLabel.map(_.trim).filter(_.nonEmpty).map(value => s"资源或品牌：$value"),
        input.advertisementKind.map(_.trim).filter(_.nonEmpty).map(value => s"广告类型：$value"),
        input.avoidText.map(_.trim).filter(_.nonEmpty).map(value => s"不要出现：$value")
      ).flatten.mkString("\n")

    Json
      .obj(
        "model" -> Json.fromString("openai"),
        "temperature" -> Json.fromDoubleOrNull(0.9d),
        "messages" -> Json.arr(
          Json.obj("role" -> Json.fromString("system"), "content" -> Json.fromString(systemPrompt)),
          Json.obj("role" -> Json.fromString("user"), "content" -> Json.fromString(userPrompt))
        ),
        "response_format" -> Json.obj("type" -> Json.fromString("json_object"))
      )
      .noSpaces

  private def extractContent(responseBody: String): String =
    val json = parse(responseBody).fold(throw _, identity)
    json.hcursor
      .downField("choices")
      .downArray
      .downField("message")
      .downField("content")
      .as[String]
      .fold(throw _, identity)

  private def parseTextCandidates(
      content: String,
      input: GenerateAdvertisementTextCandidatesRequest,
      candidateCount: Int
  ): GenerateAdvertisementTextCandidatesResponse =
    val parsed = parse(content).fold(throw _, identity)
    val sourceText = normalizedSourceText(input)
    val candidates =
      parsed.hcursor
        .downField("candidates")
        .as[List[Json]]
        .fold(_ => Nil, identity)
        .take(candidateCount)
        .zipWithIndex
        .flatMap { case (json, index) =>
          for
            returnedText <- json.hcursor.downField("text").as[String].toOption.map(_.trim).filter(_.nonEmpty)
            emphasis = json.hcursor.downField("emphasis").as[String].fold(_ => "粗标题", identity).trim match
              case "" => "粗标题"
              case value => value
          yield AdvertisementTextCandidateResponse(
            text = if returnedText == sourceText then returnedText else sourceText,
            emphasis = emphasis,
            seed = 2000 + index * 97
          )
        }

    val finalCandidates =
      if candidates.nonEmpty then candidates
      else localFallbackCandidates(input, candidateCount)

    GenerateAdvertisementTextCandidatesResponse(finalCandidates)

  private def localFallbackCandidates(
      input: GenerateAdvertisementTextCandidatesRequest,
      candidateCount: Int
  ): List[AdvertisementTextCandidateResponse] =
    val sourceText = normalizedSourceText(input) match
      case value if value.nonEmpty => value
      case _ => "广告标题"
    val styleRequirement = normalizedStyleRequirement(input)
    val styleSummary = styleRequirement.take(10)
    val emphasisOptions =
      List(
        s"粗标题 / $styleSummary",
        s"海报感 / $styleSummary",
        s"横幅字 / $styleSummary"
      ).map(_.stripSuffix(" / ").trim)

    emphasisOptions.take(candidateCount).zipWithIndex.map { case (emphasis, index) =>
      AdvertisementTextCandidateResponse(
        text = sourceText,
        emphasis = if emphasis.nonEmpty then emphasis else (if index == 0 then "粗标题" else "海报感"),
        seed = 2000 + index * 97
      )
    }

  private def normalizedSourceText(input: GenerateAdvertisementTextCandidatesRequest): String =
    input.sourceText.map(_.trim).filter(_.nonEmpty).getOrElse(input.prompt.trim)

  private def normalizedStyleRequirement(input: GenerateAdvertisementTextCandidatesRequest): String =
    List(
      input.styleRequirement.map(_.trim).filter(_.nonEmpty),
      input.focus.map(_.trim).filter(_.nonEmpty),
      input.tone.map(_.trim).filter(_.nonEmpty).map(value => s"整体风格偏$value"),
      input.avoidText.map(_.trim).filter(_.nonEmpty).map(value => s"不要出现$value")
    ).flatten match
      case Nil => "做成适合广告横幅的艺术字体"
      case values => values.mkString("，")
