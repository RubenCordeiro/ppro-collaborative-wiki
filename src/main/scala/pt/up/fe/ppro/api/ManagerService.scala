package pt.up.fe.ppro.api

import akka.actor.{ActorRef, ActorLogging, Props, Terminated}
import pt.up.fe.ppro.services.websocket.{DocumentActor, ChatServerActor}
import spray.can.Http
import spray.routing.HttpServiceActor

import scala.collection.mutable.HashMap

object ManagerService {

  case class GetDocumentActor(document: String)
  case class DocumentActor(document: String, actor: ActorRef)

}

class ManagerService extends HttpServiceActor with ActorLogging {

  private val documentActors = new HashMap[String, ActorRef]

  private def newDocumentActor(doc: String) = {
    log.info(s"Instanciating new documentActor for document $doc")
    val actor = context.actorOf(DocumentActor.props())
    context.watch(actor)
    actor
  }

  override def receive: Receive = {
    case connected : Http.Connected =>
      log.info("Registering new ChatServerActor actor for {}.", sender.path.name)

      val childActor = context.actorOf(Props(classOf[ChatServerActor], sender, self))
      sender ! Http.Register(childActor)

      context.watch(childActor)

    case Terminated(documentActor) =>
      log.debug(s"Actor ${documentActor.path.name} terminated")
      documentActors.retain{ case (_, ac) => ac != documentActor }

    case ManagerService.GetDocumentActor(document) =>
      val docActor = documentActors.getOrElseUpdate(document, newDocumentActor(document))
      sender ! ManagerService.DocumentActor(document, docActor)

    case whatever =>
      log.debug(s"${this.getClass.getSimpleName} got some $whatever")
  }
}
