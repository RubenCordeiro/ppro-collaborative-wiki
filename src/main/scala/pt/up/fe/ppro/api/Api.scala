package pt.up.fe.ppro.api

import pt.up.fe.ppro.core.{Core, CoreActors}
import pt.up.fe.ppro.services.api.ApiService
import pt.up.fe.ppro.services.node.NodeService
import spray.routing.RouteConcatenation

import scala.concurrent.ExecutionContext.Implicits.global

trait Api extends RouteConcatenation {
  this: CoreActors with Core =>

  val routes =
    new ApiService().route ~
    new NodeService(nodeActor).route

}
