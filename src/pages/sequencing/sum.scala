object Invariant {
  sealed trait Sum[A, B] {
    def fold[C](error: A => C, success: B => C): C =
      this match {
        case Failure(v) => error(v)
        case Success(v) => success(v)
      }
    def map[C](f: B => C): Sum[A, C] =
      this match {
        case Failure(v) => Failure(v)
        case Success(v) => Success(f(v))
      }
    def flatMap[C](f: B => Sum[A, C]) =
      this match {
        case Failure(v) => Failure(v)
        case Success(v) => f(v)
      }
  }
  final case class Failure[A, B](value: A) extends Sum[A, B]
  final case class Success[A, B](value: B) extends Sum[A, B]

}

object Covariant {
  sealed trait Sum[+A, +B] {
    def fold[C](error: A => C, success: B => C): C =
      this match {
        case Failure(v) => error(v)
        case Success(v) => success(v)
      }
    def map[C](f: B => C): Sum[A, C] =
      this match {
        case Failure(v) => Failure(v)
        case Success(v) => Success(f(v))
      }
    def flatMap[C](f: B => Sum[A, C]) =
      this match {
        case Failure(v) => Failure(v)
        case Success(v) => f(v)
      }
  }
  final case class Failure[A](value: A) extends Sum[A, Nothing]
  final case class Success[B](value: B) extends Sum[Nothing, B]
}
