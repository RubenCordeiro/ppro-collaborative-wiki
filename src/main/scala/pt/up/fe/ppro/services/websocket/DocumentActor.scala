package pt.up.fe.ppro.services.websocket

import akka.actor._
import pt.up.fe.ppro.models.messages.{Message, Join, Say, Said, Left}
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
    case Terminated(client) =>
      clients -= client
      val name = names get client
      if (name.isDefined) {
        val msg: Message = Left(name.get)
        clients.foreach(_ ! msg)
        names -= client
      }
      if (clients.isEmpty)
        context stop self

    case `NewClient` =>
      val newActor = context.actorOf(WebSocketChatActor.props(self))
      context.watch(newActor)
      clients += newActor
      sender ! ClientAdded(newActor)

    case ClientName(name) =>
      if (clients contains sender) {
        names.update(sender, name)
        clients.foreach(_ ! Join(name))
      }

    case Say(message) if names.keySet.contains(sender()) =>
      clients.foreach(_ ! Said(names(sender()), message))
  }

}
