package pt.up.fe.ppro.services.websocket

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import pt.up.fe.ppro.api.ManagerService
import pt.up.fe.ppro.websocket.{WebSocket, WebSocketServer}
import spray.http.HttpRequest
import spray.routing.Route

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class ChatServerActor(connection: ActorRef, manager: ActorRef) extends WebSocketServer(connection) {
  implicit val timeout = Timeout(5.seconds)

  val pathPrefix = "chat"

  def handleConnection(request: HttpRequest) = {
      val doc = request.uri.path.toString.replaceFirst("/" + pathPrefix, "")

      log.debug(s"Requesting documentActor for $doc")
      (manager ? ManagerService.GetDocumentActor(doc)) onSuccess {
        case ManagerService.DocumentActor(`doc`, docActor) =>

          log.debug(s"Requesting new client for $doc")

          (docActor ? DocumentActor.NewClient) onSuccess {
            case DocumentActor.ClientAdded(actor) =>

              log.debug(s"New Client Added for $doc")
              self ! WebSocket.Register(request, actor, autoping = true)
          }
      }
    }

}
