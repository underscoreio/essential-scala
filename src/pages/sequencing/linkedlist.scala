sealed trait LinkedList[A]
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
final case class End[A]() extends LinkedList[A]

sealed trait LinkedList[A] {
  def length: Int
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def length: Int =
    1 + tail.length
}
final case class End[A]() extends LinkedList[A] {
  def length: Int =
    0
}

val example = Pair(1, Pair(2, Pair(3, End())))
assert(example.length == 3)
assert(example.tail.length == 2)
assert(End().length == 0)


sealed trait LinkedList[A] {
  def contains(item: A): Boolean
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def contains(item: A): Boolean =
    if(head == item)
      true
    else
      tail.contains(item)
}
final case class End[A]() extends LinkedList[A] {
  def contains(item: A): Boolean =
    false
}

val example = Pair(1, Pair(2, Pair(3, End())))
assert(example.contains(3) == true)
assert(example.contains(4) == false)
assert(End().contains(0) == false)


sealed trait LinkedList[A] {
  def apply(index: Int): A
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def apply(index: Int): A =
    if(index == 0)
      head
    else
      tail(index - 1)
}
final case class End[A]() extends LinkedList[A] {
  def apply(index: Int): A =
    throw new Exception("Attempted to get element from empty list")
}

val example = Pair(1, Pair(2, Pair(3, End())))
assert(example(0) == 1)
assert(example(1) == 2)
assert(example(2) == 3)
assert(try {
  example(3)
  false
} catch {
  case e: Exception => true
})

sealed trait Result[A]
case class Success[A](result: A) extends Result[A]
case class Failure[A](reason: String) extends Result[A]

sealed trait LinkedList[A] {
  def apply(index: Int): Result[A]
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def apply(index: Int): Result[A] =
    if(index == 0)
      Success(head)
    else
      tail(index - 1)
}
final case class End[A]() extends LinkedList[A] {
  def apply(index: Int): Result[A] =
    Failure("Index out of bounds")
}

val example = Pair(1, Pair(2, Pair(3, End())))
assert(example(0) == Success(1))
assert(example(1) == Success(2))
assert(example(2) == Success(3))
assert(example(3) == Failure("Index out of bounds"))
