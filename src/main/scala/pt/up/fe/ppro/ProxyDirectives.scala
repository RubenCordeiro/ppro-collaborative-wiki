package pt.up.fe.ppro

import akka.actor.ActorSystem
import akka.io.IO
import spray.can.Http
import spray.http._
import spray.routing.{RequestContext, Route}

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