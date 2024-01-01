import akka.actor._

class HelloActor(actor_name: String) extends Actor {
  def receive = {
    case "hello" => println("Hello, it's %s! How are you?".format(actor_name))
    case _       => println("%s doesn`t understand you".format(actor_name))
  }
}

object AkkaActor extends App {
  val system = ActorSystem("HelloSystem")
  val helloActor = system.actorOf(Props(new HelloActor("Some person")), name = "helloactor")
  helloActor ! "hello"
  helloActor ! "Everything is ok"
}