import scala.math.Ordering

val minOrdering = Ordering.fromLessThan[Int](_ < _)

val maxOrdering = Ordering.fromLessThan[Int](_ > _)

object ImplicitValueExample {
  implicit val ordering = Ordering.fromLessThan[Int](_ < _)
  // Not how we don't supply an ordering to `sorted`. The compiler provides it for us
  List(2, 4, 3).sorted
  List(1, 7 ,5).sorted
}

object AmbiguousExample {
  implicit val minOrdering = Ordering.fromLessThan[Int](_ < _)
  implicit val maxOrdering = Ordering.fromLessThan[Int](_ > _)
  List(3,4,5).sorted
}
