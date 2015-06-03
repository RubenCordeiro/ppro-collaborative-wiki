package pt.up.fe.ppro.json

import pt.up.fe.ppro.models
import pt.up.fe.ppro.models.messages.{Message, _}
import spray.json._

object MyJsonFormat extends DefaultJsonProtocol {
  object messagesFormats {
    implicit val registerFormat = jsonFormat1(Register)
    implicit val sayFormat = jsonFormat1(Say)
  }

  implicit val personFormat = jsonFormat3(models.Person)
  implicit val messageFormat = new RootJsonFormat[Message] {
    import messagesFormats._

    override def write(obj: Message): JsValue = {
      JsObject((obj match {
        case msg: Register => msg.toJson
        case msg: Say => msg.toJson
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
        }

    }
  }
}