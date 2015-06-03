package pt.up.fe.ppro.services.node

import akka.actor.{ActorRef, ActorSystem}
import pt.up.fe.ppro.ProxyDirectives._
import pt.up.fe.ppro.WeakiConfiguration
import spray.routing.Directives

import scala.concurrent.ExecutionContext


class NodeService(nodeActor: ActorRef)(implicit executionContext: ExecutionContext, implicit val system: ActorSystem)
  extends Directives {

  val route =
    pathPrefix("node") {
      proxyToUnmatchedPath("http://" + WeakiConfiguration.Collab.interface + ":" + WeakiConfiguration.Collab.port)
    }
}
