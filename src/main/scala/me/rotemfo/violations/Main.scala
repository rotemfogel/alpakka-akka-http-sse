package me.rotemfo.violations

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import org.slf4j.{Logger, LoggerFactory}

/**
  * project: akka-sse
  * package:
  * file:    Main
  * created: 2019-07-29
  * author:  rotem
  */
object Main {
  private final val log: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    log.info("starting {}", getClass.getName)
    implicit val system: ActorSystem = ActorSystem("violations")
    val root = system.actorOf(Props(new Root), "root")
    system.actorOf(Props(new Terminator(root)), "terminator")
    sys.addShutdownHook(() => system.terminate())
  }

  final class Root extends Actor with ActorLogging {
    implicit val system: ActorSystem = context.system

//    final val violationsFacade = context.actorOf(ViolationsFacade.props())
//    context.watch(violationsFacade)

    val config = context.system.settings.config
    val address = config.getString("violations.api.address")
    val port = config.getInt("violations.api.port")
    val timeout = config.getDuration("violations.api.timeout")
    val bufferSize = config.getInt("violations.api.buffer-size")
    val heartbeat = config.getDuration("violations.api.heartbeat")
    new Api(address, port, /*violationsFacade, */timeout, bufferSize, heartbeat)
    log.info("{} up and running", context.system.name)

    override def receive: Receive = {
      case Terminated(actor) =>
        log.error("Terminating the system because {} terminated!", actor.path)
        context.system.terminate()
    }
  }

  // Needed to terminate the actor system on initialization errors of root, e.g. missing configuration settings!
  final class Terminator(root: ActorRef) extends Actor with ActorLogging {
    context.watch(root)

    override def receive: PartialFunction[Any, Unit] = {
      case Terminated(`root`) =>
        log.error("Terminating the system because root terminated!")
        context.system.terminate()
    }
  }

}
