package pt.up.fe.ppro.services.node

import akka.actor.{ActorLogging, Actor, Props}
import pt.up.fe.ppro.WeakiConfiguration

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.sys.process.Process
import scala.util.Success

object NodeActor {
  def props() = Props[NodeActor]()

  case object Start
  case object Stop
  private[NodeActor] case class ProcessStopped(exitCode: Int)
}

class NodeActor extends Actor with ActorLogging {

  val nodeProcess = Process(
    Seq(
      WeakiConfiguration.Config.nodeExecutable,
      getClass.getClassLoader.getResource("index.js").getPath().substring(1),
      WeakiConfiguration.Collab.port.toString
    )
  )

  var nodeProcessInstance = Option.empty[Process]

  def receive = {
    case NodeActor.Start if nodeProcessInstance.isEmpty =>
      try {
        nodeProcessInstance = Some(nodeProcess.run())

        async {
          nodeProcessInstance.get.exitValue()
        } andThen {
          case Success(exitCode) =>
            self ! NodeActor.ProcessStopped(exitCode)
        }

      } catch {
        case e: Exception => println(e.getMessage)
      }

    case NodeActor.Stop if nodeProcessInstance.isDefined =>
      nodeProcessInstance.get.destroy()
      nodeProcessInstance = None

    case NodeActor.ProcessStopped(exitCode) if nodeProcessInstance.isDefined =>
      log.error(s"Node process exited with $exitCode")
      nodeProcessInstance = None

  }

  override def postStop(): Unit = {
    for (proc <- nodeProcessInstance)
      proc.destroy()
  }
}