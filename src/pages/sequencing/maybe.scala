object Invariant {
  sealed trait Maybe[A] {
    def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
      this match {
        case Full(v) => fn(v)
        case Empty() => Empty[B]()
      }
    def map[B](fn: A => B): Maybe[B] =
      this match {
        case Full(v) => Full(fn(v))
        case Empty() => Empty[B]()
      }
  }
  final case class Full[A](value: A) extends Maybe[A]
  final case class Empty[A]() extends Maybe[A]
}

object Covariant {
  sealed trait Maybe[+A] {
    def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
      this match {
        case Full(v) => fn(v)
        case Empty => Empty
      }
    def map[B](fn: A => B): Maybe[B] =
      this match {
        case Full(v) => Full(fn(v))
        case Empty => Empty
      }
  }
  final case class Full[A](value: A) extends Maybe[A]
  final case object Empty extends Maybe[Nothing]
}
