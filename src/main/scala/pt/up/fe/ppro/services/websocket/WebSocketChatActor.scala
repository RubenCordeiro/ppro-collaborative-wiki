package pt.up.fe.ppro.services.websocket

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pt.up.fe.ppro.json.MyJsonFormat
import pt.up.fe.ppro.models.messages
import pt.up.fe.ppro.models.messages.{Say, Register}
import pt.up.fe.ppro.websocket.WebSocket
import spray.json._

import scala.util.Try
import scala.util.Success

import MyJsonFormat._

object WebSocketChatActor {
  def props(document: ActorRef) = Props(classOf[WebSocketChatActor], document)

}
class WebSocketChatActor(val documentActor: ActorRef) extends Actor with ActorLogging {

  var webSocket: WebSocket = _
  var name = Option.empty[String]

  override def receive = {
    case WebSocket.Open(ws) =>
      if (ws != webSocket) {
        webSocket = ws
        log.info(s"registered monitor for url ${ws.path}")
        ws.send("Hello World!")
      }

    case WebSocket.Close(ws, code, reason) =>
      if (ws == webSocket) {
        log.info("unregister monitor for url {}", ws.path)
        context stop self
      }

    case WebSocket.Error(ws, ex) =>
      if (ws == webSocket) {
        log.info("unregister monitor for url {} because {}", ws.path, ex)
        context stop self
      }

    case WebSocket.Message(ws, msg) =>
      if (ws == webSocket) {
        log.info("url {} received msg '{}'", ws.path, msg)
        processMessage(msg)
      }

    case DocumentActor.NewMessage(message) =>
      if (webSocket != null) {
        webSocket.send(message)
      }
  }

  def processMessage(msg: String) = Try(msg.parseJson.convertTo[messages.Message]) match {
    case Success(msg) => msg match {
      case Register(name) =>
        this.name = Some(name)
        documentActor ! DocumentActor.ClientName(name)
      case Say(content) if name.isDefined =>
        documentActor ! DocumentActor.NewMessage(content)
    }

  }
}
