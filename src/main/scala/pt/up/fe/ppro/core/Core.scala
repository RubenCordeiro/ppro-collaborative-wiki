package pt.up.fe.ppro.core

import akka.actor.ActorSystem

trait Core {
  protected implicit def system: ActorSystem
}
