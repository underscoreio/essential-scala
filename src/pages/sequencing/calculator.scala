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
  def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => f(v)
    }
}
final case class Failure[A](value: A) extends Sum[A, Nothing]
final case class Success[B](value: B) extends Sum[Nothing, B]

sealed trait Expression {
  def eval: Sum[String, Double] =
    this match {
      case Addition(l, r) => lift2(l, r, _ + _)
      case Subtraction(l, r) => lift2(l, r, _ - _)
      case Division(l, r) => lift2(l, r, _ / _)
      case SquareRoot(v) =>
        v.eval flatMap { value =>
          if(value < 0)
            Failure("Square root of negative number")
          else
            Success(Math.sqrt(value))
        }
      case Number(v) => Success(v)
    }

  def lift2(l: Expression, r: Expression, f: (Double, Double) => Double) =
    l.eval flatMap { left =>
      r.eval map { right => f(left, right) }
    }
}
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Int) extends Expression
