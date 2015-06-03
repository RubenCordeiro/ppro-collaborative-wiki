package pt.up.fe.ppro.core

import pt.up.fe.ppro.services.node.NodeActor

trait CoreActors {
  this: Core =>

  lazy val nodeActor = system.actorOf(NodeActor.props(), "NodeService")
  // lazy val webSocketActor = system.actorOf(WebSocketActor.props(), "WebSocketService")
}
