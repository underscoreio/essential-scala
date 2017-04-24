## Modelling Data with Traits

In this section we're going to shift our focus from language features to programming patterns. We're going to look at modelling data and learn a process for expressing in Scala any data model defined in terms of *logical ors and ands*. Using the terminology of object-oriented programming, we will express *is-a* and *has-a* relationships. In the terminology of functional programming we are learning about *sum* and *product types*, which are together called *algebraic data types*.

Our goal in this section is to see how to translate a data model into Scala code. In the next section we'll see patterns for code that uses algebraic data types.

### The Product Type Pattern

Our first pattern is to model data that contains other data. We might describe this as "`A` *has a* `B` *and* `C`". For example, a `Cat` has a colour and a favourite food; a `Visitor` has an id and a creation date; and so on.

The way we write this is to use a case class. We've already done this many times in exercises; now we're formalising the pattern.

<div class="callout callout-info">
#### Product Type Pattern {-}

If `A` has a `b` (with type `B`) and a `c` (with type `C`) write

```scala
case class A(b: B, c: C)
```

or

```scala
trait A {
  def b: B
  def c: C
}
```
</div>

## The Sum Type Pattern

Our next pattern is to model data that is two or more distinct cases. We might describe this as "`A` *is a* `B` *or* `C`". For example, a `Feline` is a `Cat`, `Lion`, or `Tiger`; a `Visitor` is an `Anonymous` or `User`; and so on.

We write this using the sealed trait / final case class pattern.

<div class="callout callout-info">
#### Sum Type Pattern {-}

If `A` is a `B` or `C` write

```scala
sealed trait A
final case class B() extends A
final case class C() extends A
```
</div>

### Algebraic Data Types

An algebraic data type is any data that uses the above two patterns. In the functional programming literature, data using the "has-a and" pattern is known as a *product type*, and the "is-a or" pattern is a *sum type*.

### The Missing Patterns

We have looked at relationships along two dimensions: is-a/has-a, and and/or. We can draw up a little table and see we only have patterns for two of the four table cells.

+-----------+--------------+----------+
|           | And          | Or       |
+===========+==============+==========+
| **Is-a**  |              | Sum type |
+-----------+--------------+----------+
| **Has-a** | Product type |          |
+-----------+--------------+----------+



What about the missing two patterns?

The "is-a and" pattern means that `A` is a `B` and `C`. This pattern is in some ways the inverse of the sum type pattern, and we can implement it as

```scala
trait B
trait C
trait A extends B with C
```

In Scala a trait can extend as many traits as we like using the `with` keyword like `A extends B with C with D` and so on. We aren't going to use this pattern in this course. If we want to represent that some data conforms to a number of different interfaces we will often be better off using a *type class*, which we will explore later. There are, however, several legitimate uses of this pattern:

- for modularity, using what's known as the [cake pattern](http://jonasboner.com/2008/10/06/real-world-scala-dependency-injection-di/); and
- sharing implementation across several classes where it doesn't make sense to make default implementations in the main trait.

The "has-a or" patterns means that `A` has a `B` or `C`. There are two ways we can implement this. We can say that `A` has a `d` of type `D`, where `D` is a `B` or `C`. We can mechanically apply our two patterns to implement this:

```scala
trait A {
  def d: D
}
sealed trait D
final case class B() extends D
final case class C() extends D
```

Alternatively we could implement this as `A` is a `D` or `E`, and `D` has a `B` and `E` has a `C`. Again this translates directly into code

```scala
sealed trait A
final case class D(b: B) extends A
final case class E(c: C) extends A
```

### Take Home Points

We have seen that we can mechanically translate data using the "has-a and" and "is-a or" patterns (or, more succintly, the product and sum types) into Scala code. This type of data is known as an algebraic data type. Understanding these patterns is very important for writing idiomatic Scala code.

### Exercises

#### Stop on a Dime

A traffic light is red, green, or yellow. Translate this description into Scala code.

<div class="solution">
This is a direct application of the sum type pattern.

```scala
sealed trait TrafficLight
final case object Red extends TrafficLight
final case object Green extends TrafficLight
final case object Yellow extends TrafficLight
```

As there are fields or methods on the three cases, and thus there is no need to create than one instance of them, I used case objects instead of case classes.
</div>

#### Calculator

A calculation may succeed (with an `Int` result) or fail (with a `String` message). Implement this.

<div class="solution">
```scala
sealed trait Calculation
final case class Success(result: Int) extends Calculation
final case class Failure(reason: String) extends Calculation
```
</div>

#### Water, Water, Everywhere

Bottled water has a size (an `Int`), a source (which is a well, spring, or tap), and a `Boolean` carbonated. Implement this in Scala.

<div class="solution">
Crank the handle on the product and sum type patterns.

```scala
final case class BottledWater(size: Int, source: Source, carbonated: Boolean)
sealed trait Source
final case object Well extends Source
final case object Spring extends Source
final case object Tap extends Source
```
</div>
