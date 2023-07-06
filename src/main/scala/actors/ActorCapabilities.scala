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

  // jane ! "Hi!" // reply to "me"

  /*
    5 - forwarding messages
      |> D -> A -> B

      forwarding = sending a message with the ORIGINAL sender
   */

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  jane ! WirelessPhoneMessage("Hi", john) // no sender

  // DOMAIN of the counter
  object Counter:
    case object Increment
    case object Decrement
    case object Print

  class Counter extends Actor:
    import Counter._

    var value = 0
    override def receive: Actor.Receive =
      case Increment => value += 1
      case Decrement => value -= 1
      case Print     => println(s"[${self.path}]: O valor atual é: $value")

  val counter = system.actorOf(Props(Counter()), "counter")

  counter ! Counter.Print
  counter ! Counter.Increment
  counter ! Counter.Increment
  counter ! Counter.Decrement
  counter ! Counter.Print

  object Bank:
    case class Deposit(value: Int)
    case class Withdraw(value: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(message: String)

  class Bank extends Actor:
    import Bank._

    var amount = 0
    override def receive: Actor.Receive =
      case Deposit(value) =>
        if amount < 0 then
          sender() ! TransactionFailure("Valor inválido para depósito")
        else
          amount += value
          sender() ! TransactionSuccess("Depósito realizado com sucesso")
      case Withdraw(value) =>
        if amount < 0 then
          sender() ! TransactionFailure("Valor inválido para saque")
        else if amount - value < 0 then
          sender() ! TransactionFailure("Fundos insuficientes para saque")
        else
          amount -= value
          sender() ! TransactionSuccess("Saque realizado com sucesso")
      case Statement => sender() ! s"O seu saldo é: $amount"

  object Person:
    case class LiveTheLife(account: ActorRef)

  class Person extends Actor:
    import Person._
    import Bank._

    override def receive: Actor.Receive =
      case LiveTheLife(account) =>
        account ! Deposit(1000)
        account ! Withdraw(2000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)

  val account = system.actorOf(Props(Bank()), "account")
  val person = system.actorOf(Props(Person()), "person")

  person ! Person.LiveTheLife(account)
}
