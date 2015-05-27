package pt.up.fe.ppro

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import pt.up.fe.ppro.services.NodeService
import spray.can.Http

import scala.concurrent.duration._

object Boot extends App {
  implicit val system = ActorSystem("RealTimeWeaki")
  
  implicit val timeout = Timeout(5.seconds)
  
  val service = system.actorOf(ApiService.props(), "ApiService")

  val nodeService = system.actorOf(NodeService.props(), "NodeService")

  IO(Http) ? Http.Bind(service, interface = WeakiConfiguration.Chat.interface, port = WeakiConfiguration.Chat.port)

  nodeService ! NodeService.Start

  scala.sys.addShutdownHook {
    nodeService ! NodeService.Stop
     system.shutdown()
  }
}