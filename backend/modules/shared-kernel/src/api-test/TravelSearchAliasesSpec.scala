package com.typesafe.travel.api.application

import com.typesafe.travel.shared.kernel.{AirportCode, HotelLocation}
import munit.FunSuite

final class TravelSearchAliasesSpec extends FunSuite:
  test("airport alias resolves city keywords to related airport codes") {
    assert(TravelSearchAliases.matchesAirportQuery(AirportCode.unsafe("PVG"), "Shanghai"))
    assert(TravelSearchAliases.matchesAirportQuery(AirportCode.unsafe("SHA"), "上海"))
    assert(TravelSearchAliases.matchesAirportQuery(AirportCode.unsafe("NRT"), "tokyo"))
  }

  test("hotel alias supports explicit area keywords and substring matching") {
    assert(TravelSearchAliases.matchesHotelLocationQuery(HotelLocation.unsafe("Hangzhou"), "hang"))
    assert(TravelSearchAliases.matchesHotelLocationQuery(HotelLocation.unsafe("Hangzhou"), "杭州"))
    assert(TravelSearchAliases.matchesHotelLocationQuery(HotelLocation.unsafe("Shanghai"), "Bund"))
  }

  test("too-short latin keywords are rejected while airport exact code is allowed") {
    assert(!TravelSearchAliases.hasUsableKeyword("sh"))
    assert(TravelSearchAliases.hasUsableKeyword("SHA"))
    assert(!TravelSearchAliases.matchesHotelLocationQuery(HotelLocation.unsafe("Shanghai"), "sh"))
  }
