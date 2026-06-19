// 本文件是广告文案候选生成入口，只服务后端广告编辑流程，不对应前端镜像文件。
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
      requestParateraTextCandidates(input, candidateCount)
        .handleErrorWith(_ => requestPollinationsTextCandidates(input, candidateCount))
        .handleError { _ =>
          GenerateAdvertisementTextCandidatesResponse(localFallbackCandidates(input, candidateCount))
        }

  private def requestParateraTextCandidates(
      input: GenerateAdvertisementTextCandidatesRequest,
      candidateCount: Int
  ): IO[GenerateAdvertisementTextCandidatesResponse] =
    IO.blocking {
      val apiKey = sys.env.getOrElse("PARATERA_API_KEY", "").trim
      if apiKey.isEmpty then throw new IllegalStateException("paratera_api_key_missing")

      val baseUrl = sys.env.getOrElse("PARATERA_BASE_URL", "https://llmapi.paratera.com").trim.stripSuffix("/")
      val model = sys.env.getOrElse("PARATERA_TEXT_MODEL", "GLM-4-Flash").trim
      val requestBuilder =
        HttpRequest
          .newBuilder(URI.create(s"$baseUrl/v1/chat/completions"))
          .header("Authorization", s"Bearer $apiKey")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(
            Json
              .obj(
                "model" -> Json.fromString(model),
                "temperature" -> Json.fromDoubleOrNull(0.9d),
                "messages" -> Json.arr(
                  Json.obj("role" -> Json.fromString("system"), "content" -> Json.fromString(buildSystemPrompt(candidateCount))),
                  Json.obj("role" -> Json.fromString("user"), "content" -> Json.fromString(buildUserPrompt(input)))
                ),
                "response_format" -> Json.obj("type" -> Json.fromString("json_object"))
              )
              .noSpaces,
            StandardCharsets.UTF_8
          ))

      val request = requestBuilder.build()
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      if response.statusCode() / 100 != 2 then
        throw new IllegalStateException(s"paratera_text_failed_${response.statusCode()}")

      val generatedContent = extractContent(response.body())
      val parsed = parseTextCandidates(generatedContent, input, candidateCount)
      System.err.println("[GenerateAdvertisementTextCandidatesPlanner] external text generation succeeded via Paratera")
      parsed
    }

  private def requestPollinationsTextCandidates(
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
    val regenerationHint = normalizedRegenerationHint(input)

    val systemPrompt =
      s"""You design artistic Chinese display-text styles for an advertisement editor.
         |Return strict JSON only, in the shape:
       |{"candidates":[{"text":"...","emphasis":"..."},{"text":"...","emphasis":"..."}]}
       |Rules:
       |- exactly $candidateCount candidates
       |- every candidate must visibly include the exact provided source text
       |- do not rewrite, shorten, expand, translate, or reorder the source text
       |- emphasis should briefly describe the visual style treatment in Chinese
       |- emphasis should stay under 18 Chinese characters if possible
       |- do not include markdown
         |- do not include numbering
         |- do not include explanation outside JSON
         |- suitable for editable banner text styles, not copywriting alternatives
         |""".stripMargin

    val userPrompt =
      List(
        Some(s"Source text: $sourceText"),
        Some(s"Style requirement: $styleRequirement"),
        Some(s"Regeneration hint: $regenerationHint"),
        input.resourceLabel.map(_.trim).filter(_.nonEmpty).map(value => s"Resource label: $value"),
        input.advertisementKind.map(_.trim).filter(_.nonEmpty).map(value => s"Advertisement kind: $value"),
        input.avoidText.map(_.trim).filter(_.nonEmpty).map(value => s"Avoid: $value")
      ).flatten.mkString("\\n")

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

  private def buildSystemPrompt(candidateCount: Int): String =
    s"""You design concise advertisement text styles for an editor.
       |Return strict JSON only, in the shape:
       |{"candidates":[{"text":"...","emphasis":"..."}]}
       |Rules:
       |- exactly $candidateCount candidates
       |- every candidate must visibly include the exact provided source text
       |- the text itself must be the visual subject, not a background image
       |- each regeneration request should explore a meaningfully different composition while keeping the same source text exact
       |- do not rewrite, shorten, expand, translate, or reorder the source text unless the user explicitly asks for copywriting
       |- emphasis should briefly describe the visual style treatment in Chinese
       |- do not include markdown, numbering, or explanation outside JSON
       |""".stripMargin

  private def buildUserPrompt(input: GenerateAdvertisementTextCandidatesRequest): String =
    List(
      Some(s"Source text: ${normalizedSourceText(input)}"),
      Some(s"Style requirement: ${normalizedStyleRequirement(input)}"),
      input.resourceLabel.map(_.trim).filter(_.nonEmpty).map(value => s"Resource label: $value"),
      input.advertisementKind.map(_.trim).filter(_.nonEmpty).map(value => s"Advertisement kind: $value"),
      input.avoidText.map(_.trim).filter(_.nonEmpty).map(value => s"Avoid: $value")
    ).flatten.mkString("\\n")

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
            emphasis = json.hcursor.downField("emphasis").as[String].fold(_ => "Bold heading", identity).trim match
              case "" => "Bold heading"
              case value => value
          yield AdvertisementTextCandidateResponse(
            text = if returnedText == sourceText then returnedText else sourceText,
            emphasis = emphasis,
            seed = candidateSeed(input, index)
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
      case _ => "Advertisement copy"
    val styleRequirement = normalizedStyleRequirement(input)
    val styleSummary = styleRequirement.take(10)
    val emphasisOptions =
      List(
        s"Bold heading / $styleSummary",
        s"Poster style / $styleSummary",
        s"Art text / $styleSummary"
      ).map(_.stripSuffix(" / ").trim)

    emphasisOptions.take(candidateCount).zipWithIndex.map { case (emphasis, index) =>
      AdvertisementTextCandidateResponse(
        text = sourceText,
        emphasis = if emphasis.nonEmpty then emphasis else (if index == 0 then "Bold heading" else "Poster style"),
        seed = candidateSeed(input, index)
      )
    }

  private def normalizedSourceText(input: GenerateAdvertisementTextCandidatesRequest): String =
    input.sourceText.map(_.trim).filter(_.nonEmpty).getOrElse(input.prompt.trim)

  private def normalizedStyleRequirement(input: GenerateAdvertisementTextCandidatesRequest): String =
    val preference = input.styleRequirement.map(_.trim).filter(_.nonEmpty)
    val colorHint = preference.flatMap(resolveColorPreferenceHint)
    List(
      preference,
      input.focus.map(_.trim).filter(_.nonEmpty),
      input.tone.map(_.trim).filter(_.nonEmpty).map(value => s"Tone: $value"),
      colorHint,
      input.avoidText.map(_.trim).filter(_.nonEmpty).map(value => s"Avoid: $value")
    ).flatten match
      case Nil => "Suitable for a banner art-text style"
      case values => values.mkString(", ")

  private def resolveColorPreferenceHint(preference: String): Option[String] =
    val normalized = preference.toLowerCase
    if normalized.contains("红") || normalized.contains("red") || normalized.contains("crimson") || normalized.contains("scarlet") then
      Some("Color priority: red should be the dominant text color.")
    else if normalized.contains("蓝") || normalized.contains("blue") || normalized.contains("azure") || normalized.contains("sky") then
      Some("Color priority: blue should be the dominant text color.")
    else if normalized.contains("绿") || normalized.contains("green") || normalized.contains("emerald") || normalized.contains("jade") then
      Some("Color priority: green should be the dominant text color.")
    else if normalized.contains("金") || normalized.contains("gold") || normalized.contains("golden") then
      Some("Color priority: gold should be the dominant text color.")
    else if normalized.contains("紫") || normalized.contains("purple") || normalized.contains("violet") then
      Some("Color priority: purple should be the dominant text color.")
    else
      None

  private def normalizedRegenerationHint(input: GenerateAdvertisementTextCandidatesRequest): String =
    input.regenerationNonce match
      case Some(value) => s"Regeneration round $value; make the layout noticeably different from the previous round."
      case None => "Initial generation; choose a clear first composition."

  private def candidateSeed(input: GenerateAdvertisementTextCandidatesRequest, index: Int): Int =
    val nonce = input.regenerationNonce.getOrElse(0).abs
    2000 + nonce * 997 + index * 97
