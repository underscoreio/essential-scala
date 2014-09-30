sealed trait IntList {
  def fold(f: (Int, Int) => Int, empty: Int): Int
  def double: IntList
  def product: Int
  def sum: Int
  def length: Int
}
final case object End extends IntList {
  def fold(f: (Int, Int) => Int, empty: Int) =
    empty
  def double: IntList =
    End
  def product: Int =
    fold(_ * _, 1)
  def sum: Int =
    fold(_ + _, 0)
  def length: Int =
    fold((hd, tl) => 1 + tl, 0)
}
final case class Pair(head: Int, tail: IntList) extends IntList {
  def fold(f: (Int, Int) => Int, empty: Int) =
    f(head, tail.fold(f, empty))
  def double: IntList =
    Pair(head * 2, tail.double)
  def product: Int =
    fold(_ * _, 1)
  def sum: Int =
    fold(_ + _, 0)
  def length: Int =
    fold((hd, tl) => 1 + tl, 0)
}

Pair(1, Pair(2, Pair(3, End)))

object TreeOps {
  def fold(list: IntList, f: (Int, Int) => Int, empty: Int): Int =
    list match {
      case End => empty
      case Pair(hd, tl) => f(hd, fold(tl, f, empty))
    }

  def sum(list: IntList): Int =
    fold(list, _ + _, 0)

  def length(list: IntList): Int =
    fold(list, (hd, tl) => 1 + tl, 0)

  def product(list: IntList): Int =
    fold(list, _ * _, 1)

  def double(list: IntList): IntList =
    list match {
      case End => End
      case Pair(hd, tl) => Pair(hd * 2, double(tl))
    }
}

val example = Pair(1, Pair(2, Pair(3, End)))
assert(End.sum == 0)
assert(example.sum == 6)
assert(example.tail.sum == 5)
assert(TreeOps.sum(End) == 0)
assert(TreeOps.sum(example.tail) == 5)
assert(TreeOps.sum(End) == 0)

assert(End.double == End)
assert(TreeOps.double(End) == End)

assert(Pair(1, End).double == Pair(2, End))
assert(TreeOps.double(Pair(1, End)) == Pair(2, End))

assert(Pair(2, Pair(1, End)).double == Pair(4, Pair(2, End)))
assert(TreeOps.double(Pair(2, Pair(1, End))) == Pair(4, Pair(2, End)))

object GenericFold {
  sealed trait IntList {
    def fold[A](f: (Int, A) => A, empty: A): A
    def double: IntList
    def product: Int
    def sum: Int
    def length: Int
  }
  final case object End extends IntList {
    def fold[A](f: (Int, A) => A, empty: A): A =
      empty
    def double: IntList =
      fold[IntList](((hd, tl) => Pair(hd * 2, tl)), End)
    def product: Int =
      fold[Int](_ * _, 1)
    def sum: Int =
      fold[Int](_ + _, 0)
    def length: Int =
      fold[Int]((hd, tl) => 1 + tl, 0)
  }
  final case class Pair(head: Int, tail: IntList) extends IntList {
    def fold[A](f: (Int, A) => A, empty: A): A =
      f(head, tail.fold(f, empty))
    def double: IntList =
      fold[IntList](((hd, tl) => Pair(hd * 2, tl)), End)
    def product: Int =
      fold[Int](_ * _, 1)
    def sum: Int =
      fold[Int](_ + _, 0)
    def length: Int =
      fold[Int]((hd, tl) => 1 + tl, 0)
  }

  def fold[A](list: IntList, f: (Int, A) => A, empty: A): A =
    list match {
      case End => empty
      case Pair(hd, tl) => f(hd, fold(tl, f, empty))
    }

  def double(list: IntList): IntList =
    fold[IntList](list, (hd, tl) => Pair(hd * 2, tl), End)
}
