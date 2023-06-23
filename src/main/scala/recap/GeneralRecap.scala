package recap

import scala.util.Try
import scala.annotation.tailrec

object GeneralRecap extends App {
  val aCondition: Boolean = false
  // aCondition = true <== proibido

  var aVariable = 42
  aVariable += 1 // aVariable = 43

  // expressions
  val aConditionVal = if aCondition then 42 else 65

  val aCodeBlock =
    if aCondition then 74
    else 56

  // types
  // Unit
  val theUnit = println("Hello, Scala")

  // functions
  def aFunction(x: Int): Int = x + 1

  // recursion - TAIL recursion
  @tailrec
  def factorial(n: Int, acc: Int): Int =
    if n <= 0 then acc
    else factorial(n - 1, acc * n)

  // OOP
  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  trait Carnivore:
    def eat(a: Animal): Unit

  class Crocodile extends Animal with Carnivore:
    override def eat(a: Animal): Unit = println("Crunch!")

  // method notation
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog

  // anonymous classes
  val aCarnivore = new Carnivore:
    override def eat(a: Animal): Unit = println("roar")

  aCarnivore.eat(aDog)

  // generics
  abstract class MyList[+A]
  // companion objects
  object MyList

  // case classes
  case class Person(name: String, age: Int)

  // Exeptions
  val aPotentialFailure =
    try throw new RuntimeException("I'm innocent, i swear!")
    catch case e: Exception => "I caught an exception"
    finally println("some logs")

  // Function programming
  val incrementer = new Function1[Int, Int]:
    override def apply(v1: Int): Int = v1 + 1

  val incremented = incrementer(42) // 43
  // incrementer.apply(42)

  val anonymousIncrementer = (x: Int) => x + 1
  // Int => Int === Function1[Int, Int]

  // FP is all about working with functions as first-class
  List(1, 2, 3).map(incrementer)
  // map = HOF

  // for comprehensions
  val pairs = for
    num <- List(1, 2, 3, 4)
    char <- List('a', 'b', 'c', 'd')
  yield num + "-" + char
  // List(1,2,3,4).flatMap(num => List('a', 'b', 'c', 'd').map(char => num + "-" + char))

  // Seq, Array, List, Vector, Map, Tuples, Sets

  // "collections"
  // Option and Try
  val anOption = Some(2)
  val aTry = Try:
    throw new RuntimeException

  // pattern matching
  val unknown = 2
  val order = unknown match
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"

  val bob = Person("Bob", 21)
  val greeting = bob match
    case Person(n, _) => s"Hi, my name is $n"
    case null         => "I don't know my name"

    // ALL THE PATTERNS

}
