sealed trait IntList {
  def fold(f: (Int, Int) => Int, empty: Int): Int
  def double: IntList
  def product: Int
  def sum: Int
  def length: Int
}
final case object Empty extends IntList {
  def fold(f: (Int, Int) => Int, empty: Int) =
    empty
  def double: IntList =
    Empty
  def product: Int =
    fold(_ * _, 1)
  def sum: Int =
    fold(_ + _, 0)
  def length: Int =
    fold((hd, tl) => 1 + tl, 0)
}
final case class Cell(head: Int, tail: IntList) extends IntList {
  def fold(f: (Int, Int) => Int, empty: Int) =
    f(head, tail.fold(f, empty))
  def double: IntList =
    Cell(head * 2, tail.double)
  def product: Int =
    fold(_ * _, 1)
  def sum: Int =
    fold(_ + _, 0)
  def length: Int =
    fold((hd, tl) => 1 + tl, 0)
}

Cell(1, Cell(2, Cell(3, Empty)))

object TreeOps {
  def fold(list: IntList, f: (Int, Int) => Int, empty: Int): Int =
    list match {
      case Empty => empty
      case Cell(hd, tl) => f(hd, fold(tl, f, empty))
    }

  def sum(list: IntList): Int =
    fold(list, _ + _, 0)

  def length(list: IntList): Int =
    fold(list, (hd, tl) => 1 + tl, 0)

  def product(list: IntList): Int =
    fold(list, _ * _, 1)

  def double(list: IntList): IntList =
    list match {
      case Empty => Empty
      case Cell(hd, tl) => Cell(hd * 2, double(tl))
    }
}

val example = Cell(1, Cell(2, Cell(3, Empty)))
assert(Empty.sum == 0)
assert(example.sum == 6)
assert(example.tail.sum == 5)
assert(TreeOps.sum(Empty) == 0)
assert(TreeOps.sum(example.tail) == 5)
assert(TreeOps.sum(Empty) == 0)

assert(Empty.double == Empty)
assert(TreeOps.double(Empty) == Empty)

assert(Cell(1, Empty).double == Cell(2, Empty))
assert(TreeOps.double(Cell(1, Empty)) == Cell(2, Empty))

assert(Cell(2, Cell(1, Empty)).double == Cell(4, Cell(2, Empty)))
assert(TreeOps.double(Cell(2, Cell(1, Empty))) == Cell(4, Cell(2, Empty)))
