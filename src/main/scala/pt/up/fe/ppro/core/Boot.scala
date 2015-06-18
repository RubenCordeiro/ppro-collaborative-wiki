package pt.up.fe.ppro.core


import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.event.Logging.InfoLevel
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import pt.up.fe.ppro.WeakiConfiguration
import pt.up.fe.ppro.api.ManagerService
import pt.up.fe.ppro.services.node.NodeActor
import spray.can.Http
import spray.can.server.UHttp
import spray.http.HttpRequest
import spray.routing.directives.LogEntry

import scala.concurrent.duration._

trait BootedCore extends Core {
  this: CoreActors =>

  val rootService = system.actorOf(Props(classOf[ManagerService]), "root-service")

  private def showReq(req: HttpRequest) = LogEntry(req.uri, InfoLevel)
}

object Boot extends App with BootedCore with CoreActors {
  implicit protected lazy val system = ActorSystem("RealTimeWeaki")
  implicit val timeout = Timeout(5.seconds)

  def actorRefFactory: ActorRefFactory = system

  nodeActor ! NodeActor.Start
  
  IO(UHttp) ? Http.Bind(rootService, interface = WeakiConfiguration.Chat.interface, port = WeakiConfiguration.Chat.port)

  scala.sys.addShutdownHook {
    IO(UHttp) ! Http.Unbind
    nodeActor ! NodeActor.Stop
    system.shutdown()
  }
}
