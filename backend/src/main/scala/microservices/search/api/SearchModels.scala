// SearchModels 定义 search 基础层的资源类型和结果模型。
package com.typesafe.travel.api.application

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

final case class SearchSuggestion(
    resourceType: SearchResourceType,
    value: String,
    title: String,
    subtitle: String,
    score: Int
)

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
