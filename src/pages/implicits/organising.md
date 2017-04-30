## Organising Type Class Instances

In section we'll learn about the places the compiler searches for type class instances (implicit values), known as the *implicit scope*, and we'll discuss how to organise type class instances to make their use more convenient.

### Implicit Scope

The compiler searches the implicit scope when it tries to find an implicit value to supply as an implicit parameter. The implicit scope is composed of several parts, and there are rules that prioritise some parts over others.

The first part of the implicit scope is the normal scope where other identifiers are found. This includes identifiers declared in the local scope, within any enclosing class, object, or trait, or `import`ed from elsewhere. An eligible implicit value must be a single identifier (i.e. `a`, not `a.b`). This is referred to as the *local scope*.

The implicit scope also includes the companion objects of types involved in the method call with the implicit parameter. Let's look at `sorted` for example. The signature for `sorted`, defined on `List[A]`, is

```scala
def sorted[B >: A](implicit ord: math.Ordering[B]): List[A]
```

The compiler will look in the following places for `Ordering` instances:

- the companion object of `List`;
- the companion object of `Ordering`; and
- the companion object of the type `B`, which is the type of elements in the list or any superclass.

The practical upshot is we can define type class instances in the companion object of our types (the type `A` in this example) and they will be found by the compiler without the user having to import them explicitly.

In the previous section we defined an `Ordering` for a `Rational` type we created. Let's see how we can use the companion object to make this `Ordering` easier to use.

First let's define the ordering in the local scope.

```tut:book:silent
final case class Rational(numerator: Int, denominator: Int)

object Example {
  def example() = {
    implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
      (x.numerator.toDouble / x.denominator.toDouble) <
      (y.numerator.toDouble / y.denominator.toDouble)
    )
    assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
           List(Rational(1, 3), Rational(1, 2), Rational(3, 4)))
  }
}
```

This works as we expect.

Now let's shift the type class instance out of the local scope and see that it doesn't compile.

```tut:book:silent
final case class Rational(numerator: Int, denominator: Int)

object Instance {
  implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
    (x.numerator.toDouble / x.denominator.toDouble) <
    (y.numerator.toDouble / y.denominator.toDouble)
  )
}
```

```tut:book:fail
object Example {
  def example =
    assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
           List(Rational(1, 3), Rational(1, 2), Rational(3, 4)))
}
```

Here I get an error at compilation time

```
No implicit Ordering defined for Rational.
assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
                                                            ^
```

Finally let's move the type class instance into the companion object of `Rational` and see that the code compiles again.

```tut:reset:invisible
// need to clear the previous Rational definitions...
```

```tut:book:silent
object wrapper {
final case class Rational(numerator: Int, denominator: Int)

object Rational {
  implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
    (x.numerator.toDouble / x.denominator.toDouble) <
    (y.numerator.toDouble / y.denominator.toDouble)
  )
}
}; import wrapper._
```

```tut:book:silent
object Example {
  def example() =
    assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
           List(Rational(1, 3), Rational(1, 2), Rational(3, 4)))
}
```

This leads us to our first pattern for packaging type class instances.

<div class="callout callout-info">
#### Type Class Instance Packaging: Companion Objects {-}

When defining a type class instance, if

1. there is a single instance for the type; and
2. you can edit the code for the type that you are defining the instance for

then *define the type class instance in the companion object of the type*.
</div>

### Implicit Priority

If we look in the [companion object for `Ordering`](http://www.scala-lang.org/api/current/#scala.math.Ordering$) we see some type class instances are already defined. In particular there is an instance for `Int`, yet we could define our own instances for `Ordering[Int]` (which we did in the previous section) and not have an issue with ambiguity.

To understand this we need to learn about the priority rules for selecting implicits. An ambiguity error is only raised if there are multiple type class instances with the same priority. Otherwise the highest priority implicit is selected.

The [full priority rules](http://eed3si9n.com/implicit-parameter-precedence-again) are rather complex, but that complexity has little impact in most cases. The practical implication is that the local scope takes precedence over instances found in companion objects. This means that implicits that the programmer explicitly pulls into scope, by importing or defining them in the local scope, will be used in preference.

Let's see this in practice, by defining an `Ordering` for `Rational` within the local scope.

```tut:reset:invisible
// need to clear the previous Rational definitions...
```

```tut:book:silent
object wrapper {
final case class Rational(numerator: Int, denominator: Int)

object Rational {
  implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
    (x.numerator.toDouble / x.denominator.toDouble) <
    (y.numerator.toDouble / y.denominator.toDouble)
  )
}
}; import wrapper._
```

```tut:book:silent
object Example {
  implicit val higherPriorityImplicit = Ordering.fromLessThan[Rational]((x, y) =>
      (x.numerator.toDouble / x.denominator.toDouble) >
      (y.numerator.toDouble / y.denominator.toDouble)
  )

  def example() =
    assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
           List(Rational(3, 4), Rational(1, 2), Rational(1, 3)))
}
```

Notice that `higherPriorityImplicit` defines a different ordering to the one defined in the companion object for `Rational`. We've also changed the expected ordering in `example` to match this new ordering. This code both compiles and runs correctly, illustrating the effect of the priority rules.

<div class="callout callout-info">
#### Type Class Instance Packaging: Companion Objects Part 2 {-}

When defining a type class instance, if

1. there is a single good default instance for the type; and
2. you can edit the code for the type that you are defining the instance for

then *define the type class instance in the companion object of the type*. This allows users to override the instance by defining one in the local scope whilst still providing sensible default behaviour.
</div>

### Packaging Implicit Values Without Companion Objects

If there is no good default instance for a type class instance, or if there are several good defaults, we should not place an type class instances in the companion object but instead require the user to explicitly import an instance into the local scope.

In this case, one simple way to package instances is to place each in its own object that the user can import into the local scope. For instance, we might define orderings for `Rational` as follows:

```tut:book:silent
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
```

In use the user would `import RationalLessThanOrdering._` or `import RationalGreaterThanOrdering._` as appropriate.

### Take Home Points

The compiler looks for type class instances (implicit values) in two places:

1. the local scope; and
2. the companion objects of types involved in the method call.

Implicits found in the local scope take precedence over those found in companion objects.

When packaging type class instances, if there is a single instance or a single good default we should put it in the companion object if possible. Otherwise, one way to package implicits is to place each one in an object and require the user to explicitly import them.

### Exercises

#### Ordering Orders

Here is a case class to store orders of some arbitrary item.

```tut:book:silent
final case class Order(units: Int, unitPrice: Double) {
  val totalPrice: Double = units * unitPrice
}
```

We have a requirement to order `Order`s in three different ways:

1. by `totalPrice`;
2. by number of `units`; and
3. by `unitPrice`.

Implement and package implicits to provide these orderings, and justify your packaging.

<div class="solution">
My implementation is below. I decided that ordering by `totalPrice` is likely to be the most common choice, and therefore should be the default. Thus I placed it in the companion object for `Order`. The other two orderings I placed in objects so the user could explicitly import them.

```tut:book:silent
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
```
</div>
