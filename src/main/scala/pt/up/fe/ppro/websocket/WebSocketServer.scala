package pt.up.fe.ppro.websocket

import akka.actor._
import pt.up.fe.ppro.RouteActor
import spray.can.Http
import spray.can.websocket.frame._
import spray.can.websocket.{UpgradedToWebSocket, WebSocketServerWorker}
import spray.http.{StatusCodes, HttpResponse, HttpRequest}
import spray.routing.{Rejected, RequestContext, Route}

import scala.concurrent.duration.DurationInt

object WebSocketServer {
  def props(serverConnection : ActorRef) = Props(classOf[WebSocketServer], serverConnection)
}

abstract class WebSocketServer(val serverConnection : ActorRef) extends RouteActor with WebSocketServerWorker with WebSocket {
  import context.dispatcher

  override lazy val connection = serverConnection

  override def receive = matchRoute(route) orElse handshaking orElse closeLogic

  private def matchRoute(route: Route): Receive = {
    case request : HttpRequest =>
      val ctx = RequestContext(request, self, request.uri.path)
      log.debug(s"${sender.path.name}: HTTP request for uri {}", request.uri.path)
      if (handshaking.isDefinedAt(request)) {
        route(ctx.withResponder(self))
        handshaking(request)
      } else {
        log.debug(s"${sender.path.name}: Can't upgrade request. Terminating.")
        sender ! HttpResponse(status = StatusCodes.NotFound)
        context stop self
      }

    case WebSocket.Register(request, actor, ping) =>
      if (ping) pinger = Some(pingTimer)
      handler = actor
      uripath = request.uri.path.toString()
      handler ! WebSocket.Open(this)

    case Rejected(rejections) =>
      log.info(s"Rejecting with $rejections sender: ${sender.path}")
      context stop self

  }

  override def businessLogic = {
    case TextFrame(message) =>
      ping()
      handler ! WebSocket.Message(this, message.utf8String)
    case UpgradedToWebSocket =>

    case WebSocket.Ping =>
      send(PingFrame())

    case PongFrame(payload) =>
      ping()

    case Http.Aborted =>
      handler ! WebSocket.Error(this, "aborted")

    case Http.ErrorClosed(cause) =>
      handler ! WebSocket.Error(this, cause)

    case CloseFrame(status, reason) =>
      handler ! WebSocket.Close(this, status.code, reason)

    case Http.Closed =>
      handler ! WebSocket.Close(this, StatusCode.NormalClose.code, "")

    case Http.ConfirmedClosed =>
      handler ! WebSocket.Close(this, StatusCode.GoingAway.code, "")

    case Http.PeerClosed =>
      handler ! WebSocket.Close(this, StatusCode.GoingAway.code, "")

    case WebSocket.Release =>
      handler ! WebSocket.Close(this, StatusCode.NormalClose.code, "")

    case whatever =>
      log.debug("WebSocket received '{}'", whatever)
  }

  def send(message: String) = send(TextFrame(message))
  def close() = send(CloseFrame(StatusCode.NormalClose))
  def path() = uripath

  private def ping(): Unit = pinger match {
    case None =>
    case Some(timer) =>
      if (!timer.isCancelled) timer.cancel()
      pinger = Some(pingTimer)
  }

  private def pingTimer = context.system.scheduler.scheduleOnce(110.seconds, self, WebSocket.Ping)

  private var uripath = "/"
  private var pinger = Option.empty[Cancellable]
  private var handler = self
}
