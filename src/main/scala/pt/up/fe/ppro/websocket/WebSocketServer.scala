package pt.up.fe.ppro.websocket

import akka.actor._
import pt.up.fe.ppro.RouteActor
import spray.can.Http
import spray.can.websocket.frame._
import spray.can.websocket.{UpgradedToWebSocket, WebSocketServerWorker}
import spray.http.{Uri, StatusCodes, HttpResponse, HttpRequest}
import spray.routing.{HttpServiceActor, Rejected, RequestContext, Route}

import scala.concurrent.duration.DurationInt

object WebSocketServer {
  def props(serverConnection : ActorRef) = Props(classOf[WebSocketServer], serverConnection)
}

abstract class WebSocketServer(val serverConnection : ActorRef) extends HttpServiceActor with WebSocketServerWorker with WebSocket {
  import context.dispatcher

  def pathPrefix: String

  def handleConnection(request: HttpRequest)

  def route: Route

  override def receive = handshakingIf orElse initialization orElse businessLogicNoUpgrade

  def handshakingIf: Receive = {
    case request@HttpRequest(_, Uri.Path(path), _, _, _) if path.startsWith("/" + pathPrefix) && handshaking.isDefinedAt(request) =>
      handshaking(request)
      context.become(handshaking orElse initialization orElse businessLogicNoUpgrade)
      handleConnection(request)

    case request@(_: HttpRequest) if handshaking.isDefinedAt(request) =>
      sender ! HttpResponse(StatusCodes.NotFound)
      context stop self

  }
  def initialization: Receive = {
    case WebSocket.Register(request, actor, ping) =>
      log.info("Websocket registered")
      if (ping) pinger = Some(pingTimer)
      handler = actor
      uripath = request.uri.path.toString()
      handler ! WebSocket.Open(this)

    case Rejected(rejections) =>
      log.info(s"Rejecting with $rejections sender: ${sender.path}")
      context stop self
  }


  def businessLogicNoUpgrade: Receive = runRoute(route)

  override def businessLogic = {
    case TextFrame(message) =>
      ping()
      handler ! WebSocket.Message(this, message.utf8String)

    case UpgradedToWebSocket if handler != self =>
      handler ! WebSocket.Connected

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

    case WebSocket.Register(request, actor, ping) if handler == self =>
      log.info("Websocket registered")
      if (ping) pinger = Some(pingTimer)
      handler = actor
      uripath = request.uri.path.toString()
      handler ! WebSocket.Open(this)
      handler ! WebSocket.Connected

    case Rejected(rejections) if handler == self =>
      log.info(s"Rejecting with $rejections sender: ${sender.path}")
      context stop self

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
