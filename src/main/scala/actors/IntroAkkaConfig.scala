package actors

import com.typesafe.config.ConfigFactory
import akka.actor.{Actor, ActorSystem, ActorLogging, Props}

object IntroAkkaConfig extends App {
  class SimpleLoggingActor extends Actor with ActorLogging:
    override def receive: Actor.Receive =
      case message => log.info(message.toString)

  // 1 - Inline configuration
  val configString =
    """
    akka {
      loglevel = "ERROR"
    }
    """
  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props(SimpleLoggingActor()))

  actor ! "A message to remember"

  // 2 - Config file
  val configFileSystem = ActorSystem("ConfigFileDemo")
  val configActor = configFileSystem.actorOf(Props(SimpleLoggingActor()))

  configActor ! "Remember me"

  // 3 Separate config in the same file
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor =
    specialConfigSystem.actorOf(Props(SimpleLoggingActor()))

  specialConfigActor ! "Remeber me i'm special"

  // 4 - Separate config in another file
  val separateConfig = ConfigFactory.load("secretFolder/secretConfig.conf")
  println(
    s"separate config log level: ${separateConfig.getString("akka.loglevel")}"
  )

  /*
    5 - Different file formats
    > JSON, properties
   */

  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config: ${jsonConfig.getString("aJsonProperty")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")

  val propsConfig = ConfigFactory.load("props/propsConfig.properties")
  println(s"json config: ${propsConfig.getString("my.simpleProperty")}")
  println(s"json config: ${propsConfig.getString("akka.loglevel")}")
}
