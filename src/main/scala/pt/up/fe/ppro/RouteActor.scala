package pt.up.fe.ppro

import akka.actor.ActorRef
import spray.routing.{Route, HttpServiceActor}

trait RouteActor extends HttpServiceActor {
  def connection : ActorRef
  def route : Route
}