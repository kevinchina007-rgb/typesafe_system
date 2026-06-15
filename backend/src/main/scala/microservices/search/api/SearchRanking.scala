// SearchRanking 是 search 后端内部基础层的排序和打分工具。
// 它不会暴露给前端独立调用，也不对应单独的 frontend 搜索微服务目录。
// 其职责是为各业务 planner 提供统一的关键词归一化、得分计算和排序规则。
package com.typesafe.travel.api.application

object SearchRanking:
  def normalizeKeyword(rawKeyword: String): String =
    rawKeyword.trim.toLowerCase

  def usableKeyword(rawKeyword: String): Option[String] =
    Option(normalizeKeyword(rawKeyword)).filter(_.nonEmpty)

  def textScore(candidateText: String, rawKeyword: String): Int =
    val normalizedKeyword = normalizeKeyword(rawKeyword)
    val normalizedCandidate = normalizeKeyword(candidateText)
    if normalizedKeyword.isEmpty || normalizedCandidate.isEmpty then 0
    else if normalizedCandidate == normalizedKeyword then 120
    else if normalizedCandidate.startsWith(normalizedKeyword) then 90
    else if normalizedCandidate.split("\\s+").exists(_.startsWith(normalizedKeyword)) then 72
    else if normalizedCandidate.contains(normalizedKeyword) then 48
    else 0

  def weightedScore(rawKeyword: String, weightedTexts: (String, Int)*): Int =
    weightedTexts.iterator
      .map { case (candidateText, weightValue) => textScore(candidateText, rawKeyword) * weightValue }
      .sum

  def rankByScore[A](values: List[A])(scoreOf: A => Int): List[A] =
    values
      .map(value => value -> scoreOf(value))
      .filter(_._2 > 0)
      .sortBy { case (value, scoreValue) => (-scoreValue, value.hashCode()) }
      .map(_._1)

  def topDistinctByValue(values: List[SearchSuggestion], maximumCount: Int): List[SearchSuggestion] =
    values
      .sortBy(suggestion => (-suggestion.score, suggestion.title))
      .foldLeft(List.empty[SearchSuggestion]) { (currentSuggestions, suggestion) =>
        if currentSuggestions.exists(_.value.equalsIgnoreCase(suggestion.value)) then currentSuggestions
        else currentSuggestions :+ suggestion
      }
      .take(maximumCount)
