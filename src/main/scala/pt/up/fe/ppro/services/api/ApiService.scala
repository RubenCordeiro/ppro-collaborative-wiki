package pt.up.fe.ppro.services.api

import pt.up.fe.ppro.json.MyJsonFormat
import pt.up.fe.ppro.models.Person
import spray.http.MediaTypes._
import spray.json._
import spray.routing.Directives

import scala.concurrent.ExecutionContext

class ApiService()(implicit executionContext: ExecutionContext)
  extends Directives {

  import MyJsonFormat._

  val route =
    path("person") {
      get {
        respondWithMediaType(`application/json`) {
          complete {
            Person("Bob", "Type A", System.currentTimeMillis()).toJson.compactPrint
          }
        }
      }
    }
}
