---
layout: page
title: Type Class Foundations
---

Type classes in Scala involve the interaction of a number of components. To simplify the presentation we are going to start by looking at *using* type classes before we look at how to *build them ourselves*.

## Ordering

A simple example of a type class is the `Ordering` trait in Scala. Imagine we want to sort a `List` of `Int`s. There are many different ways to sort such a list. For example, we could sort from highest to lowest, or we could sort from lowest to highest. There is a method `sorted` on `List` that will sort a list, but to use it we must pass in an `Ordering` to give the particular ordering we want.

Let's define some `Ordering`s and see them in action.

~~~ scala
scala> import scala.math.Ordering

scala> val minOrdering = Ordering.fromLessThan[Int](_ < _)
minOrdering: scala.math.Ordering[Int] = scala.math.Ordering$$anon$9@787f32b7

scala> val maxOrdering = Ordering.fromLessThan[Int](_ > _)
maxOrdering: scala.math.Ordering[Int] = scala.math.Ordering$$anon$9@4bf324f9

scala> List(3, 4, 2).sorted(minOrdering)
res9: List[Int] = List(2, 3, 4)

scala> List(3, 4, 2).sorted(maxOrdering)
res10: List[Int] = List(4, 3, 2)
~~~

Here we define two orderings: `minOrdering`, which sorts from lowest to highest, and `maxOrdering`, which sorts from highest to lowest. When we call `sorted` we pass the `Ordering` we want to use. These implementations of a type class are called **type class instances**.

**This is the basic usage pattern for type classes.** Everything else we will see just provides extra convenience.


## Implicit Values

It can be inconvenient to continually pass the type class instance to a method when we want to repeatedly use the same instance. Scala provides a convenience, called **implicit values**, that allows us to get the compiler to pass a type class instance to a method for us. Here's an example of use:

~~~ scala
scala> implicit val ordering = Ordering.fromLessThan[Int](_ < _)

scala> List(2, 4, 3).sorted
res1: List[Int] = List(2, 3, 4)

scala> List(1, 7 ,5).sorted
res2: List[Int] = List(1, 5, 7)
~~~

Note we didn't supply an ordering to `sorted`. Instead, the compiler provides it for us.

We have to tell the compiler which values it is allowed pass to methods for us. We do this by annotating a value with `implicit`, as in the declaration `implicit val order = ...`. The method must also indicate that it accepts implicit values. If you look at the [documentation for the `sorted` method on `List`](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List) you see that the single parameter is declared `implicit`. We'll talk more about implicit parameter lists in a bit. For now we just need to know that we can get the compiler to supply implicit values to parameters that are themselves marked implicit.

### Declaring Implicit Values

We can tag any `val`, `var`, `object` or zero-argument `def` with the `implicit` keyword, making it a potential candidate for an implicit parameter.

~~~ scala
implicit val exampleOne = ...
implicit var exampleTwo = ...
implicit def exampleThree = ...
~~~

### Implicit Value Ambiguity

What happens when multiple implicit values are in scope? Let's ask the console.

~~~ scala
scala> implicit val minOrdering = Ordering.fromLessThan[Int](_ < _)

scala> implicit val maxOrdering = Ordering.fromLessThan[Int](_ > _)

scala> List(3,4,5).sorted
 <console>:12: error: ambiguous implicit values:
 both value ordering of type => scala.math.Ordering[Int]
 and value minOrdering of type => scala.math.Ordering[Int]
 match expected type scala.math.Ordering[Int]
                List(3,4,5).sorted
                            ^
~~~

The rule is simple. The compiler will signal an error if there is any ambiguity in which implicit value should be used.


## Take Home Points

In this section we've seen the basics for using type classes. In Scala, a type class is just a trait. To use a type class we:

- create implementations of that trait, called type class instances; and
- typically we mark the type class instances as implicit values.

Marking values as implicit tells the compiler it can supply them to a parameter to a method call if none is explicitly given. For the compiler to supply a value:

1. the parameter must be marked implicit;
2. there must be an implicit value available of the same type of the parameter; and
3. there must be only one such implicit value available.

## Exercises

### More Orderings

Define an `Ordering` that orders `Int`s from lowest to highest by absolute value. The following test cases should pass.

~~~ scala
assert(List(-4, -1, 0, 2, 3).sorted(absOrdering) == List(0, -1, 2, 3, -4))
assert(List(-4, -3, -2, -1).sorted(absOrdering) == List(-1, -2, -3, -4))
~~~

<div class="solution">
~~~ scala 
val absOrdering = Ordering.fromLessThan[Int]{ (x, y) =>
  Math.abs(x) < Math.abs(y)
}
~~~
</div>

Now make your ordering an implicit value, so the following test cases work.

~~~ scala
assert(List(-4, -1, 0, 2, 3).sorted == List(0, -1, 2, 3, -4))
assert(List(-4, -3, -2, -1).sorted == List(-1, -2, -3, -4))
~~~

<div class="solution">
Simply mark the value as implicit (and make sure it is in scope)

~~~ scala
implicit val absOrdering = Ordering.fromLessThan[Int]{ (x, y) =>
  Math.abs(x) < Math.abs(y)
}
~~~
</div>

### Rational Orderings

Scala doesn't have a class to represent rational numbers, but we can easily implement one ourselves.

~~~ scala 
final case class Rational(numerator: Int, denominator: Int)
~~~

Implement an `Ordering` for `Rational` to order rationals from smallest to largest. The following test case should pass.

~~~ scala
assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
       List(Rational(1, 3), Rational(1, 2), Rational(3, 4)))
~~~

<div class="solution">
~~~ scala
implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
  (x.numerator.toDouble / x.denominator.toDouble) < 
  (y.numerator.toDouble / y.denominator.toDouble)
)
~~~
</div>

