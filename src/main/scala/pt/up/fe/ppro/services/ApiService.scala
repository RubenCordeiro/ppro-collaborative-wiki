package pt.up.fe.ppro

import akka.actor.{ Actor, ActorRef, Props }

object ApiService {
  def props() = Props[ApiService]
}

class ApiService() extends Actor with ApiServiceRoutes {
  def actorRefFactory = context
  
  def receive = runRoute(routes)
}
