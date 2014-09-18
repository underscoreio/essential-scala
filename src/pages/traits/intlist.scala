sealed trait IntList {
  def double: IntList
  def product: Int
  def sum: Int
}
final case object Empty extends IntList {
  def double: IntList =
    Empty
  def product: Int =
    1
  def sum: Int =
    0
}
final case class Cell(head: Int, tail: IntList) extends IntList {
  def double: IntList =
    Cell(head * 2, tail.double)
  def product: Int =
    head * tail.product
  def sum: Int =
    head + tail.sum
}

Cell(1, Cell(2, Cell(3, Empty)))


def sum(list: IntList): Int =
  list match {
    case Empty => 0
    case Cell(hd, tl) => hd + sum(tl)
  }

def length(list: IntList): Int =
  list match {
    case Empty => 0
    case Cell(hd, tl) => 1 + length(tl)
  }

def product(list: IntList): Int =
  list match {
    case Empty => 1
    case Cell(hd, tl) => hd * product(tl)
  }

def double(list: IntList): IntList =
  list match {
    case Empty => Empty
    case Cell(hd, tl) => Cell(hd * 2, double(tl))
  }

val example = Cell(1, Cell(2, Cell(3, Empty)))
assert(sum(example) == 6)
assert(sum(example.tail) == 5)
assert(sum(Empty) == 0)

assert(Empty.double == Empty)
assert(double(Empty) == Empty)

assert(Cell(1, Empty).double == Cell(2, Empty))
assert(double(Cell(1, Empty)) == Cell(2, Empty))

assert(Cell(2, Cell(1, Empty)).double == Cell(4, Cell(2, Empty)))
assert(double(Cell(2, Cell(1, Empty))) == Cell(4, Cell(2, Empty)))
