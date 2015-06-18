package pt.up.fe.ppro.services.websocket

import akka.actor._
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import pt.up.fe.ppro.models.messages._
import scala.collection.mutable
import spray.json._
import spray.json.DefaultJsonProtocol._

object DocumentActor {
  def props() = Props[DocumentActor]

  case object NewClient
  case class ClientAdded(client: ActorRef)

  case class NewMessage(message: String)
  case class ClientToken(name: String)

}

class DocumentActor extends Actor with ActorLogging {
  import DocumentActor._

  val clients = mutable.Set[ActorRef]()
  val clientsData = mutable.HashMap[ActorRef, (String, String)]()

  override def receive = {
    case Terminated(client) =>
      clients -= client
      val name = clientsData get client
      if (name.isDefined) {
        val timestamp: Long = System.currentTimeMillis / 1000
        val msg: Message = Left(timestamp, name.get._1, name.get._2)
        clients.foreach(_ ! msg)

        // TODO: Save to redis

        clientsData -= client
      }
      if (clients.isEmpty)
        context stop self

    case `NewClient` =>
      val newActor = context.actorOf(WebSocketChatActor.props(self))
      context.watch(newActor)
      clients += newActor
      sender ! ClientAdded(newActor)

    case ClientToken(token) =>
      if (clients contains sender) {
        try {
          val jwtConsumer = new JwtConsumerBuilder().setDisableRequireSignature().setDisableRequireSignature().setJwsAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE).setJweAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE).setJweContentEncryptionAlgorithmConstraints(AlgorithmConstraints.ALLOW_ONLY_NONE).build()

          val jwtClaims = jwtConsumer.processToClaims(token)

          val data = jwtClaims.toJson.parseJson.asJsObject.fields.get("data").get.convertTo[Map[String,String]]

          val name = data.get("nickname").get
          val email = data.get("email").get

          clientsData.update(sender, (name, email))

          sender ! Registered(name, email)

          val timestamp: Long = System.currentTimeMillis / 1000
          val msg: Message = Join(timestamp, name, email)
          clients.view.filter(_ != sender).foreach(_ ! msg)

          // TODO: Save to redis

          // TODO: Send History
        } catch {
          case e =>
            // TODO: Send ERROR
        }

      }

    case Say(message) if clientsData.keySet.contains(sender()) =>
      val senderData = clientsData(sender())
      val timestamp: Long = System.currentTimeMillis / 1000
      clients.foreach(_ ! Said(timestamp, senderData._1, senderData._2, message))

      // TODO: Save to redis
  }

}
