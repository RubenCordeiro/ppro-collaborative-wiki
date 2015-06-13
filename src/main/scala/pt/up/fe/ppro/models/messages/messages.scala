package pt.up.fe.ppro.models.messages

import spray.json._

sealed abstract class Message extends Product {
  def mType: String
}

sealed trait Control {
  this: Message =>

  override def mType = "Control"
}

case class Register(name: String) extends Message with Control

case class Join(name: String) extends Message with Control

case class Left(name: String) extends Message with Control

sealed trait Chat {
  this: Message =>

  override def mType = "Chat"
}

case class Say(content: String) extends Message with Chat

case class Said(name: String, content: String) extends Message with Chat

trait JsonProtocol extends DefaultJsonProtocol {
  object messagesFormats {
    implicit val registerFormat = jsonFormat1(Register)
    implicit val sayFormat = jsonFormat1(Say)
    implicit val saidFormat = jsonFormat2(Said)
    implicit val joinFormat = jsonFormat1(Join)
    implicit val leftFormat = jsonFormat1(Left)
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


        }
    }
  }
}