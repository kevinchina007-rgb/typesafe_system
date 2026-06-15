// 本文件汇总系统的 Planner 路由定义。
package com.typesafe.travel.api.routes

object PlannerDefinitions:
  val advertisingPlanners: PlannerRegistry = PlannerDefinitionsAdvertising.registry
  val attractionPlanners: PlannerRegistry = PlannerDefinitionsAttraction.registry
  val authPlanners: PlannerRegistry = PlannerDefinitionsAuth.registry
  val blogPlanners: PlannerRegistry = PlannerDefinitionsBlog.registry
  val contentPlanners: PlannerRegistry = PlannerDefinitionsContent.registry
  val flightPlanners: PlannerRegistry = PlannerDefinitionsFlight.registry
  val hotelPlanners: PlannerRegistry = PlannerDefinitionsHotel.registry
  val feedbackPlanners: PlannerRegistry = PlannerDefinitionsFeedback.registry
  val identityPlanners: PlannerRegistry = PlannerDefinitionsIdentity.registry
  val orderPlanners: PlannerRegistry = PlannerDefinitionsOrder.registry
  val operationsPlanners: PlannerRegistry = PlannerDefinitionsOperations.registry
  val tourGroupPlanners: PlannerRegistry = PlannerDefinitionsTourGroup.registry
  val trainPlanners: PlannerRegistry = PlannerDefinitionsTrain.registry
  val travelerPlanners: PlannerRegistry = PlannerDefinitionsTraveler.registry

  val allPlanners: PlannerRegistry =
    PlannerRegistry.combine(
      advertisingPlanners,
      attractionPlanners,
      authPlanners,
      blogPlanners,
      contentPlanners,
      flightPlanners,
      hotelPlanners,
      feedbackPlanners,
      identityPlanners,
      orderPlanners,
      operationsPlanners,
      tourGroupPlanners,
      trainPlanners,
      travelerPlanners
    )
