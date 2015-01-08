final case class Order(units: Int, unitPrice: Double) {
  val totalPrice: Double = units * unitPrice
}

object Order {
  implicit val lessThanOrdering = Ordering.fromLessThan[Order]{ (x, y) =>
    x.totalPrice < y.totalPrice
  }
}

object OrderUnitPriceOrdering {
  implicit val unitPriceOrdering = Ordering.fromLessThan[Order]{ (x, y) =>
    x.unitPrice < y.unitPrice
  }
}

object OrderUnitsOrdering {
  implicit val unitsOrdering = Ordering.fromLessThan[Order]{ (x, y) =>
    x.units < y.units
  }
}