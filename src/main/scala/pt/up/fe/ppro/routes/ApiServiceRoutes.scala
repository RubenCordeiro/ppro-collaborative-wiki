package pt.up.fe.ppro

import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.json.pimpAny
import spray.routing.Directive.pimpApply
import spray.routing.{HttpService, Route}
import akka.actor.ActorSystem

import spray.can.Http
import akka.io.IO
import akka.actor.ActorSystem
import spray.routing.RequestContext

trait ProxyDirectives {

  private def proxyRequest(updateRequest: RequestContext => HttpRequest)(implicit system: ActorSystem): Route =
    ctx => IO(Http)(system) tell (updateRequest(ctx), ctx.responder)

  private def stripHostHeader(headers: List[HttpHeader] = Nil) =
    headers filterNot (header => header is HttpHeaders.Host.lowercaseName)

  private val updateUriUnmatchedPath = (ctx: RequestContext, uri: Uri) => uri.withPath(uri.path ++ ctx.unmatchedPath).withQuery(ctx.request.uri.query)

  def updateRequest(uri: Uri, updateUri: (RequestContext, Uri) => Uri): RequestContext => HttpRequest =
    ctx => ctx.request.copy(
      uri = updateUri(ctx, uri)).withHeaders(stripHostHeader(ctx.request.headers))

  def proxyToUnmatchedPath(uri: Uri)(implicit system: ActorSystem): Route = proxyRequest(updateRequest(uri, updateUriUnmatchedPath))
}

object ProxyDirectives extends ProxyDirectives

import ProxyDirectives._

trait ApiServiceRoutes extends HttpService {
  self: akka.actor.Actor =>
  
  import MyJsonFormat._
  
  
  val routes: Route = {
    path("person") {
      get {
        respondWithMediaType(`application/json`) {
          complete {
            Person("Bob", "Type A", System.currentTimeMillis()).toJson.compactPrint
          }
        }
      }
    } ~ 
    pathPrefix("node") {
	    proxyToUnmatchedPath("http://" + WeakiConfiguration.Collab.interface + ":" + WeakiConfiguration.Collab.port)(self.context.system)
      /*unmatchedPath { unmatched =>
        redirect(new Uri(WeakiConfiguration.Collab.interface + ":" + WeakiConfiguration.Collab.port + unmatched), StatusCodes.PermanentRedirect)
      }*/
    }
  }
}