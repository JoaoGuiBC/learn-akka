package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object ChangingActorBehavior extends App {
  object FussyKid:
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"

  class FussyKid extends Actor:
    import FussyKid._
    import Mom._

    var state = HAPPY
    override def receive: Actor.Receive =
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if state == HAPPY then sender() ! KidAccept
        else sender() ! KidReject

  class StatelessFussyKid extends Actor:
    import FussyKid._
    import Mom._
    override def receive: Actor.Receive = happyReceive

    def happyReceive: Receive =
      case Food(VEGETABLE) => context.become(sadReceive)
      case Food(CHOCOLATE) =>
      case Ask(_)          => sender() ! KidAccept

    def sadReceive: Receive =
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.become(happyReceive)
      case Ask(_)          => sender() ! KidReject

  object Mom:
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"

  class Mom extends Actor:
    import Mom._
    import FussyKid._
    override def receive: Actor.Receive =
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("Do tou want to play")
      case KidAccept => println("My kid is happy")
      case KidReject => println("My kid is sad, but he's healthy")

  val system = ActorSystem("changingActorBehaviorDemo")

  val kid = system.actorOf(Props(FussyKid()), "kid")
  val statelessKid = system.actorOf(Props(StatelessFussyKid()), "statelessKid")
  val mom = system.actorOf(Props(Mom()), "mom")

  import Mom._
  mom ! MomStart(kid)
  mom ! MomStart(statelessKid)

  object Counter:
    case object Increment
    case object Decrement
    case object Print

  class Counter extends Actor:
    import Counter._

    override def receive: Actor.Receive = countReceive(0)

    def countReceive(value: Int): Receive =
      case Increment => context.become(countReceive(value + 1))
      case Decrement => context.become(countReceive(value - 1))
      case Print     => println(value)

  val counter = system.actorOf(Props(Counter()), "counter")

  counter ! Counter.Increment
  counter ! Counter.Decrement
  counter ! Counter.Increment
  counter ! Counter.Increment
  counter ! Counter.Print

  /*
    1 - recreate the Counter actor with context.become and no mutable state
    2 - simplified voting system
   */
  object Citizen:
    case object VoteStatusRequest
    case class Vote(candidate: String)
    case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor:
    import Citizen._
    override def receive: Actor.Receive =
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
      case Vote(candidate)   => context.become(voteReceive(candidate))

    def voteReceive(candidate: String): Receive =
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
      case Vote(candidate)   => context.become(voteReceive(candidate))
  end Citizen

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor:
    import Citizen._
    override def receive: Actor.Receive = aggregateReceive(Map())

    def aggregateReceive(votes: Map[String, Int]): Receive =
      case AggregateVotes(citizens) =>
        val future = Future {
          citizens.foreach(_ ! VoteStatusRequest)
        }

        future.onComplete:
          case Success(_) => self ! VoteStatusRequest
          case Failure(_) => println(s"[${self.path}]: Something went wrong")

      case VoteStatusReply(candidate) =>
        context.become(aggregateReceive(candidate :: votes))
      case VoteStatusRequest => println(s"OS VOTOS SÃO: $votes")
  end VoteAggregator

  val john = system.actorOf(Props(Citizen()))
  val jane = system.actorOf(Props(Citizen()))
  val alice = system.actorOf(Props(Citizen()))
  val bob = system.actorOf(Props(Citizen()))

  import Citizen._
  // john ! Vote("Martin")
  jane ! Vote("Jonas")
  alice ! Vote("Roland")
  bob ! Vote("Roland")

  val voteAggregator = system.actorOf(Props(VoteAggregator()))
  voteAggregator ! AggregateVotes(Set(john, jane, alice, bob))
  // println(jane ! VoteStatusRequest)

  /*
    print the status of the votes

    Martin -> 1
    Jonas -> 1
    Roland -> 2
   */
}
