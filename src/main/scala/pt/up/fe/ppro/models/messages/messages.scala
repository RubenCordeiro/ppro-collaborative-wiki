package pt.up.fe.ppro.models.messages

import spray.json._

sealed abstract class Message extends Product {
  def mType: String
}

sealed trait Control {
  this: Message =>

  override def mType = "Control"
}

case object Connected extends Message with Control

case class Registered(name: String, email: String) extends Message with Control

case class Register(token: String) extends Message with Control

case class Join(timestamp: Long, name: String, email: String) extends Message with Control

case class Left(timestamp: Long, name: String, email: String) extends Message with Control

sealed trait Chat {
  this: Message =>

  override def mType = "Chat"
}

case class Say(content: String) extends Message with Chat

case class Said(timestamp: Long, name: String, email: String, content: String) extends Message with Chat

trait JsonProtocol extends DefaultJsonProtocol {
  object messagesFormats {
    implicit val registerFormat = jsonFormat1(Register)
    implicit val sayFormat = jsonFormat1(Say)
    implicit val saidFormat = jsonFormat4(Said)
    implicit val joinFormat = jsonFormat3(Join)
    implicit val leftFormat = jsonFormat3(Left)
    implicit val registeredFormat = jsonFormat2(Registered)
  }

  implicit val messageFormat = new RootJsonFormat[Message] {
    import messagesFormats._

    override def write(obj: Message): JsValue = {
      JsObject((obj match {
        case msg: Register => msg.toJson
        case msg: Say => msg.toJson
        case msg: Said => msg.toJson
        case msg: Join => msg.toJson
        case msg: Left => msg.toJson
        case `Connected` => JsObject()
        case msg: Registered => msg.toJson
      }).asJsObject.fields +
        ("mType" -> JsString(obj.mType)) +
        ("msg" -> JsString(obj.productPrefix))
      )
    }

    override def read(json: JsValue): Message = json match {
      case JsObject(fields) =>
        fields("msg") match {
          case JsString("Register") => json.convertTo[Register]
          case JsString("Say") => json.convertTo[Say]
          case JsString("Said") => json.convertTo[Said]
          case JsString("Join") => json.convertTo[Join]
          case JsString("Left") => json.convertTo[Left]
          case JsString("Connected") => Connected
          case JsString("Registered") => json.convertTo[Registered]
        }
    }
  }
}