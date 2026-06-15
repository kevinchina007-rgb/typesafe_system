// TravelSearchAliases 是 search 后端内部基础层的别名匹配和查询扩展工具。
// 它负责把用户输入的自然语言关键词扩展成机场代码、酒店地点、火车站和景点等可检索条件。
// 这个文件只服务后端内部 search 逻辑，不对应前端独立微服务目录。
package com.typesafe.travel.api.application

import com.typesafe.travel.shared.kernel.{AirportCode, HotelLocation}

object TravelSearchAliases:
  private val airportAliasIndex: Map[String, Set[AirportCode]] =
    Map(
      "shanghai" -> Set(AirportCode.unsafe("PVG"), AirportCode.unsafe("SHA")),
      "上海" -> Set(AirportCode.unsafe("PVG"), AirportCode.unsafe("SHA")),
      "tokyo" -> Set(AirportCode.unsafe("NRT"), AirportCode.unsafe("HND")),
      "东京" -> Set(AirportCode.unsafe("NRT"), AirportCode.unsafe("HND")),
      "seoul" -> Set(AirportCode.unsafe("ICN"), AirportCode.unsafe("GMP")),
      "首尔" -> Set(AirportCode.unsafe("ICN"), AirportCode.unsafe("GMP")),
      "hangzhou" -> Set(AirportCode.unsafe("HGH")),
      "杭州" -> Set(AirportCode.unsafe("HGH")),
      "incheon" -> Set(AirportCode.unsafe("ICN")),
      "gimpo" -> Set(AirportCode.unsafe("GMP")),
      "narita" -> Set(AirportCode.unsafe("NRT")),
      "haneda" -> Set(AirportCode.unsafe("HND")),
      "pudong" -> Set(AirportCode.unsafe("PVG")),
      "hongqiao" -> Set(AirportCode.unsafe("SHA")),
      "xiaoshan" -> Set(AirportCode.unsafe("HGH"))
    )

  private val hotelLocationAliasIndex: Map[String, Set[String]] =
    Map(
      "hangzhou" -> Set("hangzhou", "west lake", "杭州", "西湖"),
      "west lake" -> Set("hangzhou", "west lake", "杭州", "西湖"),
      "杭州" -> Set("hangzhou", "west lake", "杭州", "西湖"),
      "西湖" -> Set("hangzhou", "west lake", "杭州", "西湖"),
      "shanghai" -> Set("shanghai", "bund", "上海", "外滩"),
      "bund" -> Set("shanghai", "bund", "上海", "外滩"),
      "上海" -> Set("shanghai", "bund", "上海", "外滩"),
      "外滩" -> Set("shanghai", "bund", "上海", "外滩")
    )

  def normalizeSearchText(rawSearchText: String): String =
    rawSearchText.trim.toLowerCase

  def hasUsableKeyword(rawSearchText: String): Boolean =
    val normalizedSearchText = normalizeSearchText(rawSearchText)
    if normalizedSearchText.isEmpty then false
    else if isExactAirportCode(normalizedSearchText) then true
    else if normalizedSearchText.forall(isAsciiLetterOrDigit) then normalizedSearchText.length >= 3
    else normalizedSearchText.length >= 2

  def matchesAirportQuery(airportCode: AirportCode, rawSearchText: String): Boolean =
    val normalizedSearchText = normalizeSearchText(rawSearchText)
    if normalizedSearchText.isEmpty then false
    else
      val normalizedAirportCode = airportCode.value.toLowerCase
      val aliasAirportCodes = resolveAirportAliasCodes(normalizedSearchText)
      val hasExactAirportMatch = normalizedAirportCode == normalizedSearchText
      val hasPrefixAirportMatch = isKeywordEligible(normalizedSearchText) && normalizedAirportCode.startsWith(normalizedSearchText)
      val hasSubstringAirportMatch = isKeywordEligible(normalizedSearchText) && normalizedAirportCode.contains(normalizedSearchText)
      hasExactAirportMatch || hasPrefixAirportMatch || hasSubstringAirportMatch || aliasAirportCodes.contains(airportCode)

  def matchesHotelLocationQuery(hotelLocation: HotelLocation, rawSearchText: String): Boolean =
    val normalizedSearchText = normalizeSearchText(rawSearchText)
    val normalizedHotelLocation = normalizeSearchText(hotelLocation.value)
    if normalizedSearchText.isEmpty then false
    else
      val expandedSearchTerms = resolveHotelLocationTerms(normalizedSearchText)
      expandedSearchTerms.exists { searchTerm =>
        normalizedHotelLocation == searchTerm ||
        (isKeywordEligible(searchTerm) && normalizedHotelLocation.startsWith(searchTerm)) ||
        (isKeywordEligible(searchTerm) && normalizedHotelLocation.contains(searchTerm))
      }

  def airportSearchTerms(rawSearchText: String): Set[String] =
    val normalizedSearchText = normalizeSearchText(rawSearchText)
    if normalizedSearchText.isEmpty then Set.empty
    else
      resolveAirportAliasCodes(normalizedSearchText).map(_.value.toLowerCase) +
        normalizedSearchText

  def hotelLocationSearchTerms(rawSearchText: String): Set[String] =
    val normalizedSearchText = normalizeSearchText(rawSearchText)
    if normalizedSearchText.isEmpty then Set.empty
    else resolveHotelLocationTerms(normalizedSearchText)

  def matchesTrainStationQuery(stationCodeValue: String, stationNameValue: String, rawSearchText: String): Boolean =
    val normalizedSearchText = normalizeSearchText(rawSearchText)
    val normalizedStationCode = normalizeSearchText(stationCodeValue)
    val normalizedStationName = normalizeSearchText(stationNameValue)
    if normalizedSearchText.isEmpty then false
    else
      matchesText(normalizedStationCode, normalizedSearchText) ||
      matchesText(normalizedStationName, normalizedSearchText)

  def matchesAttractionQuery(attractionNameValue: String, cityValue: String, locationValue: String, rawSearchText: String): Boolean =
    val normalizedSearchText = normalizeSearchText(rawSearchText)
    val normalizedAttractionName = normalizeSearchText(attractionNameValue)
    val normalizedCity = normalizeSearchText(cityValue)
    val normalizedLocation = normalizeSearchText(locationValue)
    if normalizedSearchText.isEmpty then false
    else
      matchesText(normalizedAttractionName, normalizedSearchText) ||
      matchesText(normalizedCity, normalizedSearchText) ||
      matchesText(normalizedLocation, normalizedSearchText)

  private def resolveAirportAliasCodes(normalizedSearchText: String): Set[AirportCode] =
    val directMatches = airportAliasIndex.collect {
      case (aliasText, airportCodes) if aliasMatches(aliasText, normalizedSearchText) => airportCodes
    }.flatten
    directMatches.toSet

  private def resolveHotelLocationTerms(normalizedSearchText: String): Set[String] =
    val directTerms = Set(normalizedSearchText)
    val aliasTerms = hotelLocationAliasIndex.collect {
      case (aliasText, locationTerms) if aliasMatches(aliasText, normalizedSearchText) => locationTerms
    }.flatten
    directTerms ++ aliasTerms

  private def aliasMatches(aliasText: String, normalizedSearchText: String): Boolean =
    aliasText == normalizedSearchText ||
    (isKeywordEligible(normalizedSearchText) && aliasText.startsWith(normalizedSearchText)) ||
    (isKeywordEligible(normalizedSearchText) && aliasText.contains(normalizedSearchText))

  private def matchesText(normalizedCandidateText: String, normalizedSearchText: String): Boolean =
    normalizedCandidateText == normalizedSearchText ||
    (isKeywordEligible(normalizedSearchText) && normalizedCandidateText.startsWith(normalizedSearchText)) ||
    (isKeywordEligible(normalizedSearchText) && normalizedCandidateText.contains(normalizedSearchText))

  private def isExactAirportCode(normalizedSearchText: String): Boolean =
    normalizedSearchText.matches("^[a-z]{3}$")

  private def isKeywordEligible(normalizedSearchText: String): Boolean =
    hasUsableKeyword(normalizedSearchText) && !isExactAirportCode(normalizedSearchText)

  private def isAsciiLetterOrDigit(characterValue: Char): Boolean =
    characterValue.isLetterOrDigit && characterValue.toInt <= 127
