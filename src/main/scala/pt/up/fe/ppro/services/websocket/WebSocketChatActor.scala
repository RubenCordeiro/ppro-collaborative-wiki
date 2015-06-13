package pt.up.fe.ppro.services.websocket

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pt.up.fe.ppro.json.MyJsonFormat
import pt.up.fe.ppro.models.messages
import pt.up.fe.ppro.models.messages._
import pt.up.fe.ppro.websocket.WebSocket
import spray.json._

import scala.util.Try
import scala.util.Success

import scalaz._

import MyJsonFormat._

object WebSocketChatActor {
  def props(document: ActorRef) = Props(classOf[WebSocketChatActor], document)
}

object Helper {

  implicit class RichWebSocket(websocket: WebSocket) {
    def !(msg: messages.Message) = websocket.send(msg.toJson.toString)
  }

}

import Helper._

class WebSocketChatActor(val documentActor: ActorRef) extends Actor with ActorLogging {
  override def receive = unconnectedClient

  def commonBehavior: Receive = {
    case WebSocket.Close(ws, code, reason) =>
      log.info("unregister monitor for url {}", ws.path)
      context stop self

    case WebSocket.Error(ws, ex) =>
      log.info("unregister monitor for url {} because {}", ws.path, ex)
      context stop self
  }

  def unconnectedClient = {
    {
      case WebSocket.Open(ws) =>
        log.info(s"registered monitor for url ${ws.path}")
        context.become(connectedClient(ws))
    }: Receive
  } orElse commonBehavior

  def connectedClient(webSocket: WebSocket) = {
    {
      case WebSocket.Message(ws, msg) =>
        log.debug("url {} received msg '{}'", ws.path, msg)

        Try(msg.parseJson.convertTo[messages.Message]) map {
          case Register(name) =>
            documentActor ! DocumentActor.ClientName(name)
            context.become(registeredClient(webSocket, name))
          case _ =>
        }
    }: Receive
  } orElse commonBehavior

  def registeredClient(webSocket: WebSocket, name: String) = {
    {
      case WebSocket.Message(ws, msg) =>
        log.debug("url {} received msg '{}'", ws.path, msg)

        Try(msg.parseJson.convertTo[messages.Message]) map {
          case message: Say =>
            documentActor ! message
          case _ =>
        }

      case msg: Said =>
        webSocket ! msg

      case msg: Join =>
        webSocket ! msg

      case msg: Left =>
        webSocket ! msg
    }: Receive
  } orElse commonBehavior

  def processMessage(msg: String) = Try(msg.parseJson.convertTo[messages.Message]) match {
    case Success(msg) => msg match {
      case Say(content) =>
        documentActor ! msg
    }

  }
}
