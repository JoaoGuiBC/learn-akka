package actors

import akka.actor.{Actor, ActorSystem, Props, ActorRef}

object ActorCapabilities extends App {
  class SimpleActor extends Actor:
    override def receive: Actor.Receive =
      case "Hi!" => sender() ! "Hello, there!" // replying to a message
      case message: String =>
        println(s"[${self.path}] I have received a message: $message")
      case number: Int =>
        println(s"[${self.path}] I have received a number: $number")
      case SpecialMessage(contents) =>
        println(s"[${self.path}] I have received a Special Message: $contents")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) =>
        ref ! "Hi!" // jane is being passed as the sender
      case WirelessPhoneMessage(content, ref) =>
        ref forward (content + "s") // i keep the original sender of the WPM

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props(SimpleActor()), "simpleActor")

  simpleActor ! "Hello, actor"

  /*
    1 - messages can be of any type
      |> messages must be IMMUTABLE
      |> messages must be SERIALIZABLE

      in practice use case classes and case objects
   */

  simpleActor ! 10 // who is the sender?

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  /*
    2 - actors have information about their context and about themselves
    context.self === `this` in OOP
   */

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor")

  /*
    3 - actors can REPLY to messages
   */

  val jane = system.actorOf(Props(SimpleActor()), "jane")
  val john = system.actorOf(Props(SimpleActor()), "john")

  case class SayHiTo(ref: ActorRef)
  jane ! SayHiTo(john)

  /*
    4 - dead letters
   */

  jane ! "Hi!" // reply to "me"

  /*
    5 - forwarding messages
      |> D -> A -> B

      forwarding = sending a message with the ORIGINAL sender
   */

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  jane ! WirelessPhoneMessage("Hi", john) // no sender
}
