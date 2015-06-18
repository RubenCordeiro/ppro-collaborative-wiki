package pt.up.fe.ppro.json

import pt.up.fe.ppro.models
import pt.up.fe.ppro.models.messages
import spray.json._

object MyJsonFormat extends DefaultJsonProtocol with messages.JsonProtocol {
}