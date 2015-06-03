package pt.up.fe.ppro.services.websocket

import akka.actor._
import scala.collection.mutable

object DocumentActor {
  def props() = Props[DocumentActor]

  case object NewClient
  case class ClientAdded(client: ActorRef)

  case class NewMessage(message: String)
  case class ClientName(name: String)

}

class DocumentActor extends Actor with ActorLogging {
  import DocumentActor._

  val clients = mutable.Set[ActorRef]()
  val names = mutable.HashMap[ActorRef, String]()

  override def receive = {
    case NewClient =>
      val newActor = context.actorOf(WebSocketChatActor.props(self))
      context.watch(newActor)
      val msg = NewMessage(s"${newActor.path.name} joined")
      clients.foreach(_ ! msg)
      clients += newActor
      sender() ! ClientAdded(newActor)

    case ClientName(name) =>
      if (clients contains sender)
        names.update(sender, name)

    case NewMessage(message) if names.keySet.contains(sender()) =>
      val msg = NewMessage(s"${names(sender())} said: $message")
      clients.view.filter(_ != sender()).foreach(_ ! msg)

    case Terminated(client) =>
      clients -= client
      val msg = NewMessage(s"${client.path.name} left")
      clients.foreach(_ ! msg)
      if (clients.isEmpty)
        context stop self
  }

}
