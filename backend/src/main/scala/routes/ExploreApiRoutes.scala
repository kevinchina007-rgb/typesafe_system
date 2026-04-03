package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.{BlogPostScope, ExploreSearchResult, SearchRanking, SearchResourceType, SearchSuggestion}
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.shared.kernel.{SharedValidationError, StayPeriod}
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

trait ExploreApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def exploreRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "explore" / "suggestions" :? SearchQueryParamMatcher(queryValue) =>
      for
        queryText <- fromEither(queryValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        suggestions <- buildExploreSuggestions(queryText)
        response <- Ok(SearchSuggestionListResponseDto(suggestions.map(SearchSuggestionResponseDto.fromApplication)).asJson)
      yield response

    case GET -> Root / "api" / "explore" / "search" :? SearchQueryParamMatcher(queryValue) +& ExploreTypeQueryParamMatcher(resourceTypeValue) =>
      for
        queryText <- fromEither(queryValue.filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("q")))
        results <- buildExploreResults(queryText, resourceTypeValue)
        response <- Ok(ExploreSearchResponseDto(results.map(ExploreSearchResultResponseDto.fromApplication)).asJson)
      yield response
  }

  private def buildExploreSuggestions(queryText: String): F[List[SearchSuggestion]] =
    (
      flightBookingApplicationService.suggestFlights(queryText),
      hotelBookingApplicationService.suggestHotels(queryText),
      trainBookingApplicationService.suggestTrains(queryText),
      attractionBookingApplicationService.suggestAttractions(queryText),
      blogApplicationService.suggestPublishedPosts(queryText)
    ).mapN { (flightSuggestions, hotelSuggestions, trainSuggestions, attractionSuggestions, blogSuggestions) =>
      SearchRanking.topDistinctByValue(
        flightSuggestions ++ hotelSuggestions ++ trainSuggestions ++ attractionSuggestions ++ blogSuggestions,
        12
      )
    }

  private def buildExploreResults(queryText: String, resourceTypeValue: Option[String]): F[List[ExploreSearchResult]] =
    val requestedResourceType = resourceTypeValue.flatMap(SearchResourceType.fromSearchScope)
    (
      loadFlightResults(queryText, requestedResourceType),
      loadHotelResults(queryText, requestedResourceType),
      loadTrainResults(queryText, requestedResourceType),
      loadAttractionResults(queryText, requestedResourceType),
      loadBlogResults(queryText, requestedResourceType)
    ).mapN { (flightResults, hotelResults, trainResults, attractionResults, blogResults) =>
      (flightResults ++ hotelResults ++ trainResults ++ attractionResults ++ blogResults)
        .sortBy(result => (-result.score, result.title))
        .take(30)
    }

  private def loadFlightResults(queryText: String, requestedResourceType: Option[SearchResourceType]): F[List[ExploreSearchResult]] =
    if requestedResourceType.exists(_ != SearchResourceType.Flight) then Async[F].pure(List.empty)
    else
      (
        flightBookingApplicationService.browseFlights(Some(queryText), None, None),
        flightBookingApplicationService.browseFlights(None, Some(queryText), None)
      ).mapN { (departureMatches, arrivalMatches) =>
        (departureMatches ++ arrivalMatches)
          .groupBy(_._2.flightId.value)
          .values
          .map(_.head)
          .toList
          .map { case (airline, flight) =>
            ExploreSearchResult(
              resourceType = SearchResourceType.Flight,
              resourceId = flight.flightId.value,
              title = s"${airline.airlineName.value} ${flight.flightNumber.value}",
              summary = s"${flight.departureAirport.value} -> ${flight.arrivalAirport.value}",
              metaLabel = s"${flight.flightSchedule.departureAt.toLocalDate} · ${flight.basePrice.amount} ${flight.basePrice.currency}",
              navigationHint = "flights",
              imageUrl = None,
              score = SearchRanking.weightedScore(
                queryText,
                flight.flightNumber.value -> 4,
                airline.airlineName.value -> 3,
                flight.departureAirport.value -> 3,
                flight.arrivalAirport.value -> 3
              )
            )
          }
          .filter(_.score > 0)
      }

  private def loadHotelResults(queryText: String, requestedResourceType: Option[SearchResourceType]): F[List[ExploreSearchResult]] =
    if requestedResourceType.exists(_ != SearchResourceType.Hotel) then Async[F].pure(List.empty)
    else
      hotelBookingApplicationService.browseHotels(Some(queryText), Option.empty[StayPeriod]).map(
        _.map { hotel =>
          ExploreSearchResult(
            resourceType = SearchResourceType.Hotel,
            resourceId = hotel.hotelId.value,
            title = hotel.hotelName.value,
            summary = hotel.hotelLocation.value,
            metaLabel = hotel.roomTypes.headOption.map(roomType => s"${roomType.basePrice.amount} ${roomType.basePrice.currency}").getOrElse(""),
            navigationHint = "hotels",
            imageUrl = None,
            score = SearchRanking.weightedScore(queryText, hotel.hotelName.value -> 4, hotel.hotelLocation.value -> 4)
          )
        }.filter(_.score > 0)
      )

  private def loadTrainResults(queryText: String, requestedResourceType: Option[SearchResourceType]): F[List[ExploreSearchResult]] =
    if requestedResourceType.exists(_ != SearchResourceType.Train) then Async[F].pure(List.empty)
    else
      (
        trainBookingApplicationService.browseTrains(Some(queryText), None, None),
        trainBookingApplicationService.browseTrains(None, Some(queryText), None)
      ).mapN { (fromMatches, toMatches) =>
        (fromMatches ++ toMatches)
          .groupBy(_.trainId.value)
          .values
          .map(_.head)
          .toList
          .map { trainJourney =>
            ExploreSearchResult(
              resourceType = SearchResourceType.Train,
              resourceId = trainJourney.trainId.value,
              title = trainJourney.trainNumber.value,
              summary = trainJourney.stops.map(_.stationName.value).mkString(" -> "),
              metaLabel = trainJourney.saleStartsAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate.toString,
              navigationHint = "trains",
              imageUrl = None,
              score = SearchRanking.weightedScore(
                queryText,
                trainJourney.trainNumber.value -> 4,
                trainJourney.stops.map(_.stationCode.value).mkString(" ") -> 3,
                trainJourney.stops.map(_.stationName.value).mkString(" ") -> 3
              )
            )
          }
          .filter(_.score > 0)
      }

  private def loadAttractionResults(queryText: String, requestedResourceType: Option[SearchResourceType]): F[List[ExploreSearchResult]] =
    if requestedResourceType.exists(_ != SearchResourceType.Attraction) then Async[F].pure(List.empty)
    else
      attractionBookingApplicationService.browseAttractions(Some(queryText)).map(
        _.map { attraction =>
          ExploreSearchResult(
            resourceType = SearchResourceType.Attraction,
            resourceId = attraction.attractionId.value,
            title = attraction.attractionName,
            summary = s"${attraction.city} · ${attraction.location}",
            metaLabel = attraction.ticketTypes.headOption.map(ticketType => s"${ticketType.unitPrice.amount} ${ticketType.unitPrice.currency}").getOrElse(""),
            navigationHint = "attractions",
            imageUrl = None,
            score = SearchRanking.weightedScore(queryText, attraction.attractionName -> 4, attraction.city -> 3, attraction.location -> 2)
          )
        }.filter(_.score > 0)
      )

  private def loadBlogResults(queryText: String, requestedResourceType: Option[SearchResourceType]): F[List[ExploreSearchResult]] =
    if requestedResourceType.exists(_ != SearchResourceType.Blog) then Async[F].pure(List.empty)
    else
      blogApplicationService.listPosts(None, BlogPostScope.Latest, Some(queryText)).map(
        _.map { post =>
          ExploreSearchResult(
            resourceType = SearchResourceType.Blog,
            resourceId = post.postId.value,
            title = post.title,
            summary = post.searchResultSnippet.getOrElse(post.summary),
            metaLabel = s"${post.authorDisplayName} · ${post.likeCount} likes · ${post.commentCount} comments",
            navigationHint = "blog",
            imageUrl = post.imageRefs.headOption.map(_.publicUrl),
            score = SearchRanking.weightedScore(queryText, post.title -> 4, post.summary -> 2, post.searchResultSnippet.getOrElse("") -> 1)
          )
        }.filter(_.score > 0)
      )
