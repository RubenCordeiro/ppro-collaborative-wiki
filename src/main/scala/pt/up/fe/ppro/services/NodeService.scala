package pt.up.fe.ppro.services

import java.io.File

import akka.actor.{Actor, Props}
import akka.util.Timeout
import pt.up.fe.ppro.WeakiConfiguration

import scala.async.Async._
import scala.collection.immutable
import scala.sys.process.Process
import scala.util.Success

import concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import concurrent.duration._

object NodeService {
  def props() = Props[NodeService]()

  case object Start
  case object Stop
  private[NodeService] case class ProcessStopped(exitCode: Int)
}

class NodeService extends Actor {

  val nodeProcess = Process(
    Seq(
      WeakiConfiguration.Config.nodeExecutable,
      getClass.getClassLoader.getResource("index.js").getPath().substring(1),
      WeakiConfiguration.Collab.port.toString
    )
  )

  var nodeProcessInstance = Option.empty[Process]

  def receive = {
    case NodeService.Start if nodeProcessInstance.isEmpty =>
      try {
        nodeProcessInstance = Some(nodeProcess.run())

        async {
          nodeProcessInstance.get.exitValue()
        } andThen {
          case Success(exitCode) =>
            self ! NodeService.ProcessStopped(exitCode)
        }

      } catch {
        case e: Exception => println(e.getMessage)
      }

    case NodeService.Stop if nodeProcessInstance.isDefined =>
      nodeProcessInstance.get.destroy()
      nodeProcessInstance = None

    case NodeService.ProcessStopped(exitCode) if nodeProcessInstance.isDefined =>
      println(s"Node process exited with $exitCode")
      nodeProcessInstance = None

  }

  override def postStop(): Unit = {
    for (proc <- nodeProcessInstance)
      proc.destroy()
  }
}