sealed trait IntList {
  def fold(end: Int, f: (Int, Int) => Int): Int =
    this match {
      case End => end
      case Pair(hd, tl) => f(hd, tl.fold(end, f))
    }
  def length: Int =
    fold(0, (_, tl) => 1 + tl)
  def product: Int =
    fold(1, (hd, tl) => hd * tl)
  def sum: Int =
    fold(0, (hd, tl) => hd + tl)
}
final case object End extends IntList
final case class Pair(head: Int, tail: IntList) extends IntList

val example = Pair(1, Pair(2, Pair(3, End)))

assert(example.length == 3)
assert(example.tail.length == 2)
assert(End.length == 0)

assert(example.product == 6)
assert(example.tail.product == 6)
assert(End.product == 1)

assert(example.sum == 6)
assert(example.tail.sum == 5)
assert(End.sum == 0)

object GenericFold {
  sealed trait IntList {
    def fold[A](end: A, f: (Int, A) => A): A =
      this match {
        case End => end
        case Pair(hd, tl) => f(hd, tl.fold(end, f))
      }
    def length: Int =
      fold[Int](0, (_, tl) => 1 + tl)
    def product: Int =
      fold[Int](1, (hd, tl) => hd * tl)
    def sum: Int =
      fold[Int](0, (hd, tl) => hd + tl)
    def double: IntList =
      fold[IntList](End, (hd, tl) => Pair(hd * 2, tl))
  }
  final case object End extends IntList
  final case class Pair(head: Int, tail: IntList) extends IntList
}
