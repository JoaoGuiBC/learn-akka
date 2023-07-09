package actors

import akka.actor.{Actor, Props, ActorRef, ActorSystem}
import actors.ChildActors.CreditCard.AttachToAccount
import actors.ChildActors.CreditCard.CheckStatus

object ChildActors extends App {
  // Actors can create other actors

  object Parent:
    case class CreateChild(name: String)
    case class TellChild(message: String)

  class Parent extends Actor:
    import Parent._
    override def receive: Actor.Receive = 
      case CreateChild(name) => 
        println(s"[${self.path}]: Creating child")
        // Create a new actor right here
        val childRef = context.actorOf(Props(Child()), name)
        context.become(withChild(childRef))

    def withChild(childRef: ActorRef): Receive =
      case TellChild(message) => childRef forward message

  class Child extends Actor:
    override def receive: Actor.Receive = 
      case message => println(s"[${self.path}]: I got: $message")

  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props(Parent()), "parent")

  import Parent._
  parent ! CreateChild("Bobby")
  parent ! TellChild("A little message")

  // actor hierarchies
  // parent -> child  -> grandChild
  //        -> child2 ->

  /*
    Guardian Actors (top-level)
    - /system = system guardian
    - /user   = user-level guardian
    - /       = the root guardian
   */

  /**
    * Actor selection
    */

  val childSelection = system.actorSelection("user/parent/Bobby")
  childSelection ! "I found you"

  /**
    *  Danger!
    * 
    * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS` REFERENCE, TO CHILD ACTORS.
    */

  object NaiveBankAccount:
    case object InitializeAccount
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
  class NaiveBankAccount extends Actor:
    import NaiveBankAccount._
    import CreditCard._

    var amount = 0
    override def receive: Actor.Receive = 
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props(CreditCard()), "card")
        creditCardRef ! AttachToAccount(this) // !!
      case Deposit(funds)  => deposit(funds)
      case Withdraw(funds) => withdraw(funds)

    def deposit(funds: Int) =
      println(s"[${self.path}]: depositing $funds on top of $amount")
      amount += funds
    def withdraw(funds: Int) =
      println(s"[${self.path}]: withdrawing $funds from $amount")
      amount -= funds

  object CreditCard:
    case object CheckStatus
    case class AttachToAccount(bankAccount: NaiveBankAccount) // !!
  class CreditCard extends Actor:
    override def receive: Actor.Receive = 
      case AttachToAccount(account) => context.become(attachedTo(account))

    def attachedTo(account: NaiveBankAccount): Receive =
      case CheckStatus =>
        println(s"[${self.path}]: your message has been processed")
        // benign
        account.withdraw(1) // because i can

  import NaiveBankAccount._
  import CreditCard._

  val bankAccountRef = system.actorOf(Props(NaiveBankAccount()), "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus

  // WRONG!
}
