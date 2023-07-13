package actors

import akka.event.Logging
import akka.actor.{Actor, ActorSystem, Props, ActorLogging}

object ActorLoggingDemo extends App {
  // 1 -> Explicit logging
  class SimpleActorWithExplicitLogger extends Actor:
    val logger = Logging(context.system, this)
    /*
      1 - DEBUG
      2 - INFO
      3 - WARNING/WARN
      4 - ERROR
     */
    override def receive: Actor.Receive =
      case message => logger.info(message.toString)

  val system = ActorSystem("LoggingDemo")
  val actor =
    system.actorOf(Props(SimpleActorWithExplicitLogger()), "loggingActor")

  actor ! "message"

  // 2 -> Actor logging
  class ActorWithLogging extends Actor with ActorLogging:
    override def receive: Actor.Receive =
      case (a, b) =>
        log.info("Two things: {} and {}", a, b) // Two things: a and b
      case message => log.info(message.toString)

  val simplerActor =
    system.actorOf(Props(ActorWithLogging()), "simplerLoggingActor")

  simplerActor ! "another message"
  simplerActor ! (12, 10)
}
