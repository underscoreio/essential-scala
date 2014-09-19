sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Number(value: Int) extends Expression

sealed trait Expression {
  def eval: Double
}
final case class Addition(left: Expression, right: Expression) extends Expression {
  def eval: Double =
    (left.eval + right.eval)
}
final case class Subtraction(left: Expression, right: Expression) extends Expression {
  def eval: Double =
    (left.eval - right.eval)
}
final case class Number(value: Int) extends Expression {
  def eval: Double =
    value
}

sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Int) extends Expression

sealed trait Calculation
final case class Success(result: Double) extends Calculation
final case class Failure(reason: String) extends Calculation

sealed trait Expression {
  def eval: Calculation
}
final case class Addition(left: Expression, right: Expression) extends Expression {
  def eval: Calculation =
    left.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        right.eval match {
          case Failure(reason) => Failure(reason)
          case Success(r2) => Success(r1 + r2)
        }
    }
}
final case class Subtraction(left: Expression, right: Expression) extends Expression {
  def eval: Calculation =
    left.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        right.eval match {
          case Failure(reason) => Failure(reason)
          case Success(r2) => Success(r1 - r2)
        }
    }
}
final case class Division(left: Expression, right: Expression) extends Expression {
  def eval: Calculation =
    left.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        right.eval match {
          case Failure(reason) => Failure(reason)
          case Success(r2) =>
            if(r2 == 0)
              Failure("Division by zero")
            else
              Success(r1 / r2)
        }
    }
}
final case class SquareRoot(value: Expression) extends Expression {
  def eval: Calculation =
    value.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        if(r1 < 0)
          Failure("Square root of negative number")
        else
          Success(Math.sqrt(r1))
    }
}
final case class Number(value: Int) extends Expression {
  def eval: Calculation =
    Success(value)
}

assert(Addition(SquareRoot(Number(4)), Number(2)).eval == Success(4.0))
assert(Addition(SquareRoot(Number(-1)), Number(2)).eval ==
       Failure("Square root of negative number"))
