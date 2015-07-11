final case class Rational(numerator: Int, denominator: Int)

object RationalLessThanOrdering {
  implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
    (x.numerator.toDouble / x.denominator.toDouble) < 
    (y.numerator.toDouble / y.denominator.toDouble)
  )
}

object RationalGreaterThanOrdering {
  implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
    (x.numerator.toDouble / x.denominator.toDouble) > 
    (y.numerator.toDouble / y.denominator.toDouble)
  )
}