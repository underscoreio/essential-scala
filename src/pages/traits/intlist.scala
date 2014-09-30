sealed trait IntList {
  def double: IntList
  def product: Int
  def sum: Int
}
final case object End extends IntList {
  def double: IntList =
    End
  def product: Int =
    1
  def sum: Int =
    0
}
final case class Pair(head: Int, tail: IntList) extends IntList {
  def double: IntList =
    Pair(head * 2, tail.double)
  def product: Int =
    head * tail.product
  def sum: Int =
    head + tail.sum
}

Pair(1, Pair(2, Pair(3, End)))


def sum(list: IntList): Int =
  list match {
    case End => 0
    case Pair(hd, tl) => hd + sum(tl)
  }

def length(list: IntList): Int =
  list match {
    case End => 0
    case Pair(hd, tl) => 1 + length(tl)
  }

def product(list: IntList): Int =
  list match {
    case End => 1
    case Pair(hd, tl) => hd * product(tl)
  }

def double(list: IntList): IntList =
  list match {
    case End => End
    case Pair(hd, tl) => Pair(hd * 2, double(tl))
  }

val example = Pair(1, Pair(2, Pair(3, End)))
assert(sum(example) == 6)
assert(sum(example.tail) == 5)
assert(sum(End) == 0)

assert(End.double == End)
assert(double(End) == End)

assert(Pair(1, End).double == Pair(2, End))
assert(double(Pair(1, End)) == Pair(2, End))

assert(Pair(2, Pair(1, End)).double == Pair(4, Pair(2, End)))
assert(double(Pair(2, Pair(1, End))) == Pair(4, Pair(2, End)))
