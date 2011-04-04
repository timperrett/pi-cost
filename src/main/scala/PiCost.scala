package com.earldouglas.picost

import akka.actor._
import annotation._

object Runner {

  def main(args: Array[String]) = run(10000, 10000)

  def run(iterations: Int, length: Int) = {
    val workers = List(
      //Actor.actorOf[Worker].start,
      Actor.actorOf[Worker].start)

    implicit val accumulator = Option(Actor.actorOf(new Accumulator(iterations)).start)

    val workersIterator = (for(s <- Stream.continually(); worker <- workers) yield worker).iterator
    for (x <- 0 until iterations) workersIterator.next ! ((x * length) to ((x + 1) * length - 1))
  }
}

class Accumulator(iterations: Int) extends Actor {

  var count: Int = _
  var pi: Double = _
  var start: Long = _

  def receive = {
    case result: Double =>
      pi += result;
      count += 1;
      if (count == iterations) Actor.registry.shutdownAll
  }

  override def preStart = {
    start = System.currentTimeMillis
  }

  override def postStop = {
    println("\n>>> result: " + pi)
    println(">>> run time: " + (System.currentTimeMillis - start) + " ms\n")
  }
}

class Worker extends Actor {
  def receive = {
    case range: Range => self.reply((for (k <- range) yield (4 * math.pow(-1, k) / (2 * k + 1))).sum)
  }
}
