package pt.up.fe.ppro

import spray.json.DefaultJsonProtocol

object MyJsonFormat extends DefaultJsonProtocol {
  implicit val personFormat = jsonFormat3(Person)
}