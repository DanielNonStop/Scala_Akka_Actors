import scala.actors.Actor
import scala.actors.Actor._

abstract class Producer[T] {

  private val Next = new Object
  private val Undefined = new Object
  private val Stop = new Object

  protected def produce(x: T) {
    coordinator ! Some(x)
    receive { case Next => }
  }

  protected def produceValues: Unit

  def iterator = new Iterator[T] {
    private var current: Any = Undefined
    private def lookAhead = {
      if (current == Undefined) current = coordinator !? Next
      current
    }

    def hasNext: Boolean = lookAhead match {
      case Some(x) => true
      case None => { coordinator ! Stop; false }
    }

    def next: T = lookAhead match {
      case Some(x) => current = Undefined; x.asInstanceOf[T]
    }
  }

  private val coordinator: Actor = actor {
    loop {
      react {//the event-based pendant of receive
        case Next =>
          producer ! Next
          reply {
            receive { case x: Option[_] => x }
          }
        case Stop =>
          exit('stop)
      }
    }
  }

  private val producer: Actor = actor {
    receive {
      case Next =>
        produceValues
        coordinator ! None
    }
  }
}

object producers extends App{

  class Tree(val left: Tree, val elem: Int, val right: Tree)
  def node(left: Tree, elem: Int, right: Tree): Tree = new Tree(left, elem, right)
  def node(elem: Int): Tree = node(null, elem, null)

  def tree = node(node(node(3), 7, node(9)), 15, node(node(19), 23, node(24)))

  class PreOrder(n: Tree) extends Producer[Int] {
    def produceValues = traverse(n)
    def traverse(n: Tree) {
      if (n != null) {
        produce(n.elem)
        traverse(n.left)
        traverse(n.right)
      }
    }
  }

  class PostOrder(n: Tree) extends Producer[Int] {
    def produceValues = traverse(n)
    def traverse(n: Tree) {
      if (n != null) {
        traverse(n.left)
        traverse(n.right)
        produce(n.elem)
      }
    }
  }

  class InOrder(n: Tree) extends Producer[Int] {
    def produceValues = traverse(n)
    def traverse(n: Tree) {
      if (n != null) {
        traverse(n.left)
        produce(n.elem)
        traverse(n.right)
      }
    }
  }

  actor {
    print("PreOrder:")
    for (x <- new PreOrder(tree).iterator) print(" "+x)
    print("\nPostOrder:")
    for (x <- new PostOrder(tree).iterator) print(" "+x)
    print("\nInOrder:")
    for (x <- new InOrder(tree).iterator) print(" "+x)
    print("\n")
  }
}