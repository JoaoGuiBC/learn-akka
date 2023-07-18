package testing

import akka.actor.{ActorSystem, Props, Actor}
import akka.testkit.{TestKit, ImplicitSender}

import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec
    extends TestKit(ActorSystem("BasicSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import BasicSpec._

  "A simple actor" should {
    "Send back the same message" in {
      val echoActor = system.actorOf(Props(SimpleActor()))
      val message = "Hello test"
      echoActor ! message

      expectMsg(message) // akka.test.single-expect-default
    }
  }

  "A blackhole actor" should {
    "send back some message" in {
      val blackholeActor = system.actorOf(Props(BlackHole()))
      val message = "Hello test"
      blackholeActor ! message

      expectNoMessage(1.seconds)
    }
  }

  // message assertions
  "A lab test actor" should {
    val labTestActor = system.actorOf(Props(LabTestActor()))

    "turn a string into uppercase" in {
      labTestActor ! "I love akka"

      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply with favorite tech" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with cool tech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages = receiveN(2)

      // free to do more complicated assertions
    }

    "reply with cool tech in a fancy way" in {
      labTestActor ! "favoriteTech"

      expectMsgPF() {
        case "Scala" => // only care if the PF is defined
        case "Akka"  =>
      }
    }
  }
}

object BasicSpec:
  class SimpleActor extends Actor:
    override def receive: Actor.Receive =
      case message => sender() ! message

  class BlackHole extends Actor:
    def receive: Actor.Receive = Actor.emptyBehavior

  class LabTestActor extends Actor:
    val random = new Random()
    override def receive: Actor.Receive =
      case "greeting" =>
        if random.nextBoolean()
        then sender() ! "hi"
        else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()
