sealed trait LinkedList[A] {
  def length: Int =
    this match {
      case Pair(hd, tl) => 1 + tl.length
      case End() => 0
    }
  def contains(item: A): Boolean =
    this match {
      case Pair(hd, tl) =>
        if(hd == item)
          true
        else
          tl.contains(item)
      case End() => false
    }
  def apply(index: Int): A =
    this match {
      case Pair(hd, tl) =>
        if(index == 0)
          hd
        else
          tl(index - 1)
      case End() =>
        throw new Exception("Attempted to get element from an Empty list")
    }
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
final case class End[A]() extends LinkedList[A]

val example = Pair(1, Pair(2, Pair(3, End())))
assert(example.length == 3)
assert(example.tail.length == 2)
assert(End().length == 0)

assert(example.contains(3) == true)
assert(example.contains(4) == false)
assert(example.contains(5) == false)
assert(End().contains(0) == false)

assert(example(0) == 1)
assert(example(1) == 2)
assert(example(2) == 3)
assert(try {
  example(3)
  false
} catch {
  case e: Exception => true
})

object SafeIndex {
  sealed trait Result[A]
  case class Success[A](result: A) extends Result[A]
  case class Failure[A](reason: String) extends Result[A]

  sealed trait LinkedList[A] {
    def apply(index: Int): Result[A] =
      this match {
        case Pair(hd, tl) =>
          if(index == 0)
            Success(hd)
          else
            tl(index - 1)
        case End() =>
          Failure("Index out of bounds")
      }
  }
  final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
  final case class End[A]() extends LinkedList[A]

  val example = Pair(1, Pair(2, Pair(3, End())))
  assert(example(0) == Success(1))
  assert(example(1) == Success(2))
  assert(example(2) == Success(3))
  assert(example(3) == Failure("Index out of bounds"))
}

object Fold {
  sealed trait LinkedList[A] {
    def fold[B](end: B, f: (A, B) => B): B =
      this match {
        case End() => end
        case Pair(hd, tl) => f(hd, tl.fold(end, f))
      }
  }
  final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
  final case class End[A]() extends LinkedList[A]
}
