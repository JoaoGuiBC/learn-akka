package playground

import akka.actor.ActorSystem

@main def playground(): Unit =
  val actorSystem = ActorSystem("HelloAkka")
  println(actorSystem.name)
