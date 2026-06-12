// PlannerDefinitions 璐熻矗鏀堕泦鍚勪釜妯″潡鐨勮鍒掔粍鍚堛€?
package com.typesafe.travel.api.routes

object PlannerDefinitions:
  val advertisingPlanners: PlannerRegistry = PlannerDefinitionsAdvertising.registry
  val attractionPlanners: PlannerRegistry = PlannerDefinitionsAttraction.registry
  val authPlanners: PlannerRegistry = PlannerDefinitionsAuth.registry
  val contentPlanners: PlannerRegistry = PlannerDefinitionsContent.registry
  val flightPlanners: PlannerRegistry = PlannerDefinitionsFlight.registry
  val hotelPlanners: PlannerRegistry = PlannerDefinitionsHotel.registry
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
      contentPlanners,
      flightPlanners,
      hotelPlanners,
      identityPlanners,
      orderPlanners,
      operationsPlanners,
      tourGroupPlanners,
      trainPlanners,
      travelerPlanners
    )
