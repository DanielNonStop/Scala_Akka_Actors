import scala.actors.Actor
import scala.actors.Actor._

case object Ping
case object Pong
case object Stop

class Ping(pings_count: Int, pong: Actor) extends Actor {
  def act() {
    var pingsLeft = pings_count - 1
    pong ! Ping
    loop {
      react {
        case Pong =>
          if (pingsLeft % 100 == 0)
            Console.println("Ping actor: sending Ping message...")
          if (pingsLeft > 0) {
            pong ! Ping
            pingsLeft -= 1
          } else {
            Console.println("Ping actor: terminating...")
            pong ! Stop
            exit()
          }
      }
    }
  }
}

class Pong extends Actor {
  def act() {
    var pongCount = 0
    loop {
      react {
        case Ping =>
          if (pongCount % 100 == 0)
            Console.println("Pong actor: sending Pong message..." + pongCount)
          sender ! Pong
          pongCount = pongCount + 1
        case Stop =>
          Console.println("Pong actor: terminating...")
          exit()
      }
    }
  }
}

object PingPong extends App {
  val pong_actor = new Pong
  val ping_actor = new Ping(1000, pong_actor)
  ping_actor.start
  pong_actor.start
}