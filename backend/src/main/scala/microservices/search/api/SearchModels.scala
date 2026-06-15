// SearchModels 是 search 后端内部基础层的模型文件。
// 这里不会对应 frontend/src/microservices/search，因为它不是面向前端的独立业务微服务。
// 它只负责给其他后端 planner 复用的资源类型、建议项和探索搜索结果模型。
package com.typesafe.travel.api.application

// SearchResourceType 统一描述搜索基础层能处理的资源域。
// 这些值会被 flight / hotel / train / attraction / blog 等不同 planner 复用。
enum SearchResourceType(val value: String):
  case Flight extends SearchResourceType("flight")
  case Hotel extends SearchResourceType("hotel")
  case Train extends SearchResourceType("train")
  case Attraction extends SearchResourceType("attraction")
  case Blog extends SearchResourceType("blog")

object SearchResourceType:
  val all: Vector[SearchResourceType] =
    Vector(Flight, Hotel, Train, Attraction, Blog)

  def fromText(value: String): SearchResourceType =
    fromSearchScope(value).getOrElse(throw new IllegalArgumentException(s"Unknown search resource type: $value"))

  def fromSearchScope(value: String): Option[SearchResourceType] =
    value.trim.toLowerCase match
      case "flight"     => Some(Flight)
      case "hotel"      => Some(Hotel)
      case "train"      => Some(Train)
      case "attraction" => Some(Attraction)
      case "blog"       => Some(Blog)
      case _            => None

// SearchSuggestion 表示搜索建议中的单条候选项。
// 它属于后端搜索基础设施，不是单独的前端对象文件。
final case class SearchSuggestion(
    resourceType: SearchResourceType,
    value: String,
    title: String,
    subtitle: String,
    score: Int
)

// ExploreSearchResult 表示探索搜索中的单条结果。
// 这个模型供 content/ExploreSearchPlanner 以及相近的搜索入口复用。
final case class ExploreSearchResult(
    resourceType: SearchResourceType,
    resourceId: String,
    title: String,
    summary: String,
    metaLabel: String,
    navigationHint: String,
    imageUrl: Option[String],
    score: Int
)
