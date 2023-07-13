package actors

import akka.actor.{Actor, Props, ActorRef, ActorSystem}

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

  /** Actor selection
    */

  val childSelection = system.actorSelection("user/parent/Bobby")
  childSelection ! "I found you"

  /** Danger!
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
    import CreditCard._
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

  // Distributed Word Counting

  object WordCounterMaster:
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String, id: Int)
    case class WordCountReply(count: Int, id: Int)

  class WordCounterMaster extends Actor:
    import WordCounterMaster._
    override def receive: Actor.Receive =
      case Initialize(nChildren) =>
        val children = (1 to nChildren).map(child =>
          context.actorOf(Props(WordCounterWorker()), s"child_$child")
        )
        context.become(withChildren(children, 0, 0, Map()))

    def withChildren(
        children: Seq[ActorRef],
        currentChild: Int,
        currentTask: Int,
        requestMap: Map[Int, ActorRef]
    ): Receive =
      case message: String =>
        children(currentChild) ! WordCountTask(message, currentTask)

        val nextChild = (currentChild + 1) % children.length
        val nextTask = currentTask + 1
        val newRequestMap = requestMap + (currentTask -> sender())

        context.become(
          withChildren(children, nextChild, nextTask, newRequestMap)
        )
      case WordCountReply(count, id) =>
        val originalSender = requestMap(id)
        context.become(
          withChildren(children, currentChild, currentTask, requestMap - id)
        )
    end withChildren

  end WordCounterMaster

  class WordCounterWorker extends Actor:
    import WordCounterMaster._
    override def receive: Actor.Receive =
      case WordCountTask(text, id) =>
        println(s"[${self.path}]: I have received task $id with $text")
        sender() ! WordCountReply(text.count(_ == ' ') + 1, id)

  val counterMaster = system.actorOf(Props(WordCounterMaster()), "counter")

  import WordCounterMaster._
  counterMaster ! Initialize(5)

  counterMaster ! "Word"
  counterMaster ! "Two words"
  counterMaster ! "Three words here"
  counterMaster ! "Four words in here"
  counterMaster ! "Five words in this string"
  counterMaster ! "Five words in this string message"

  // Round Robin Logic
  // 1, 2, 3, 4, 5 and 7 tasks
  // 1, 2, 3, 4, 5, 1, 2
}
