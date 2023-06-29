package actors

import akka.actor.{ActorSystem, Actor, Props}

object ActorsIntro extends App {
  // part 1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part 2 - create actors
  // word count actor

  class WordCountActor extends Actor:
    // internal data
    var totalWords = 0

    // behavior
    def receive: PartialFunction[Any, Unit] =
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")

  // part 3 - instantiate our actor
  val wordCounter =
    actorSystem.actorOf(Props(WordCountActor()), "wordCounter")
  val anotherWordCounter =
    actorSystem.actorOf(Props(WordCountActor()), "anotherWordCounter")

  // part 4 - communicate!
  wordCounter ! "I am learning Akka and it's pretty damn cool!"
  anotherWordCounter ! "A different message"
  // asynchornous!

  object Person:
    def props(name: String) = Props(Person(name))
  class Person(name: String) extends Actor:
    override def receive: Actor.Receive = {
      case "hi" => println(s"Hi, my name is ${this.name}")
      case _    =>
    }

  val person = actorSystem.actorOf(Person.props("John"))
  person ! "hi"
}
