---
layout: page
title: "This contains That: Generics"
---

In this section we'll look at data that contains other data. We already know how to this in simple cases using fields. Here we are going to give the user freedom about the types we contain in our fields. In other words, we are going to look at **generic types**.

## Generic Types

Generic types naturally arise in collections, so let's consider a really simple collection -- a box that stores a single value. We don't care what type is stored in the box, but we want to make sure we preserve that type when we get the value out of the box. To do this we use a generic type.

~~~ scala
scala> case class Box[A](val value: A)
defined class Box

scala> Box(2)
res0: Box[Int] = Box(2)

scala> res0.value
res1: Int = 2

scala> Box("hi") // if we omit the type parameter, scala will infer its value
res2: Box[String] = Box(hi)

scala> res2.value
res3: String = hi
~~~

The syntax `[A]` is called a **type parameter** -- it binds a name to a type. Wherever `A` occurs in our class definition we will substitute in the same type. This works in the same way that binding a name to a value (using `val`) allows us to substitute in the value wherever the name occurs. The only difference is that we're operating on types rather than values.

We can also add type parameters to methods, which limits the scope of the parameter to the method declaration and body:

~~~ scala
scala> def generic[A](in: A): A = in
generic: [A](in: A)A

scala> generic[String]("foo")
res10: String = foo

scala> generic(1) // again, if we omit the type parameter, scala will infer it
res11: Int = 1
~~~

## Exercises

### Pairs

Implement a class called `Pair` that stores two value `one` and `two`. `Pair` should be generic in both arguments. Example usage:

~~~ scala
scala> val pair = Pair("hi", 2)
pair: Pair[String,Int] = Pair(hi,2)

scala> pair.one
res13: String = hi

scala> pair.two
res14: Int = 2
~~~

<div class="solution">
If one type parameter is good, two type parameters are better:

~~~ scala
case class Pair[A, B](val one: A, val two: B)
~~~
</div>

## Tuples

A *tuple* is the generalisation of a pair to any number of terms. Scala includes built-in generic tuple types with up to 22 elements, along with special syntax for creating them. The classes are called `Tuple1[A]` through to `Tuple22[A, B, C, ...]` but they can also be written in the sugared form `(A, B, C, ...)`. For example:

~~~ scala
scala> Tuple2("hi", 1) // unsugared syntax
res19: (String, Int) = (hi,1)

scala> ("hi", 1) // sugared syntax
res19: (String, Int) = (hi,1)

scala> ("hi", 1, true)
res20: (String, Int, Boolean) = (hi,1,true)
~~~

We can define methods that accept tuples as parameters using the same syntax:

~~~ scala
scala> def tuplized[A, B](in: (A, B)) = in._1
tuplized: [A, B](in: (A, B))A

scala> tuplized(("a", 1))
res21: String = a
~~~

We can also pattern match on tuples as follows:

~~~ scala
scala> (1, "a") match {
     |   case (a, b) => a + b
     | }
res3: String = 1a
~~~

Although pattern matching is the natural way to deconstruct a tuple, each class also has a compliment of fields named `_1`, `_2` and so on:

~~~ scala
scala> val x = (1, "b", true)
x: (Int, String, Boolean) = (1,b,true)

scala> x._1
res5: Int = 1

scala> x._3
res6: Boolean = true
~~~

## Exercises

### Generic Sum Type

A **sum type** is a useful tool that allows us to represent values that could be of one type or another. Let's implement this now.

Implement a trait called `Sum` with two subtypes `Left` and `Right`. Create type parameters so that `Left` and `Right` can wrap up values of two different types.

Hint: you will need to put both type parameters on all three types.

Example usage:

~~~ scala
scala> Left[Int, String](1).value
res24: Int = 1

scala> Right[Int, String]("foo").value
res25: String = foo

scala> val sum: Sum[Int, String] = Right("foo")
sum: Sum[Int,String] = Right(foo)

scala> sum match {
     |   case Left(x) => x.toString
     |   case Right(x) => x
     | }
res26: String = foo
~~~

<div class="solution">
The code is very similar to `Box`, except that we need both type parameters:

~~~ scala
sealed trait Sum[A, B]
final case class Left[A, B](val value: A) extends Sum[A, B]
final case class Right[A, B](val value: B) extends Sum[A, B]
~~~

Scala has the generic sum type `Either` for two cases, but it does not have types for more cases.
</div>

### Generic Error Handling

In a previous exercise we "solved" the problem of dividing by zero by defining a type called `DivisionResult`. This forced us to handle the possibility of a division by zero in order to access the value.

With our knowledge of generics we can now generalise `DivisionResult` to encapsulate potential errors of any type. Modify `DivisionResult` to create a generic trait called `PossibleResult` with two subtypes, `ActualResult` and `NoResult`. Rewrite `divide` to return a `PossibleResult[Int]`. Example usage:

~~~ scala
divide(1, 0) match {
  case ActualResult[Int](value) => println(s"It's finite: ${value}")
  case NoResult[Int]()          => println(s"It's infinite")
}
~~~

<div class="solution">
Ideally we would like to write something like this:

~~~ scala
sealed trait PossibleResult[A]
final case class ActualResult[A](val value: A) extends PossibleResult[A]
final case object NoResult extends PossibleResult[???]
~~~

However, objects can't have type parameters. In order to make `NoResult` an object we need to provide a concrete type in the `extends PossibleResult` part of the definition. But what type should we use? In the absence of a preference for a particular data type, we could use something like `Unit` or `Nothing`. However this leads to type errors:

~~~ scala
scala> :paste
sealed trait PossibleResult[A]
final case class ActualResult[A](val value: A) extends PossibleResult[A]
final case object NoResult extends PossibleResult[Nothing]
^D

defined trait PossibleResult
defined class ActualResult
defined module NoResult

scala> val possible: PossibleResult[Int] = NoResult
<console>:9: error: type mismatch;
 found   : NoResult.type
 required: PossibleResult[Int]
       val possible: PossibleResult[Int] = NoResult
~~~

The problem here is that `NoResult` is a `PossibleResult[Nothing]` and a `PossibleResult[Nothing]` is not a `PossibleResult[Int]`.

We'll see how to overcome this limitation in a moment. In the meantime we can define `NoResult` as a class with a type parameter as a stop-gap solution:

~~~ scala
sealed trait PossibleResult[A]
final case class ActualResult[A](val value: A) extends PossibleResult[A]
final case class NoResult[A]() extends PossibleResult[A]

object division {
  def apply(num: Int, den: Int) =
    if(den == 0) NoResult[Int] else ActualResult(num / den)
}
~~~

Regardless, `PossibleResult` is a powerful concept -- it allows us to represent objects that may or may not contain a value. We can see this as an alternative to using `null`, except that the compiler can verify that we never get a `NullPointerException`.

In fact, `PossibleResult` is such a useful concept that Scala defines a core class called `Option` for this very purpose. `Option` is a trait with two subtypes, `Some` for storing a value and `None` representing an empty value.
</div>

{% comment %}

### Generic Functions

Add a method `map` to `Box` that takes a function of type `A => B` and returns a `Box[B]`. Example usage:

~~~ scala
scala> Box(2) map (x => x.toString)
res27: Box[String] = Box(2)
~~~

<div class="solution">
~~~ scala
case class Box[A](val value: A) {
  def map[B](f: A => B): Box[B] =
      Box(f(get))
}
~~~
</div>

{% endcomment %}

## Type Bounds

It is sometimes useful to constrain a generic type. We can do this with type bounds indicating that a generic type should be a sub- or super-type of some given types. The syntax is `A <: Type` to declare `A` must be a subtype of `Type` and `A >: Type` to declare a supertype.

For example, the following type allows us to store a `Visitor` or any subtype:

~~~ scala
case class WebAnalytics[A <: Visitor](
  visitor: A,
  pageViews: Int,
  searchTerms: List[String],
  isOrganic: Boolean
)
~~~

## Invariance and Covariance

Consider our `Box` type and the type `Foo` declared below.

~~~ scala
sealed trait Foo()
final case class Ex1() extends Foo
final case class Ex2() extends Foo
~~~

Is a `Box[Ex1]` a subtype of `Box[Foo]`? Let's ask the REPL.

~~~ scala
scala> def fooIt(in: Box[Foo]): Foo = in.value
fooIt: (in: Box[Foo])Foo

scala> val box = Box(Ex1())
box: Box[Ex1] = Box(Ex1())

scala> fooIt(box)
<console>:15: error: type mismatch;
 found   : Box[Ex1]
 required: Box[Foo]
Note: Ex1 <: Foo, but class Box is invariant in type A.
You may wish to define A as +A instead. (SLS 4.5)
              fooIt(box)
                    ^
~~~

This interaction might be surprising. `Ex1` is a subtype of `Foo` so we might expect `Box[Ex1]` to be a subtype of `Box[Foo]`. Subtyping relationships with generic types are subtle. By default generic types in Scala are **invariant**, meaning that for a type `F[A]` neither subtypes nor supertypes of `A` make a subtype of `F[A]`. This is the behaviour we're seeing with `Box`.

**Covariance** is the behaviour most people expect. For a covariant type `F` a subtype of `A` is a subtype of `F[A]`. We can make a generic type covariant by introducing a generic type as `+A` instead of `A`.

**Contravariance** is the opposite of covariance. For a contravariant type `F` a supertype of `A` is a subtype of `F[A]`. Why would we ever want contravariance? The main example is in function types, which we'll see in the next section.

There is no doubt that variance is confusing to many. The good news is it hardly ever comes up in application code. We can typically settle for invariant types at the cost of a few type declarations to keep the compiler happy. For example:

~~~ scala
scala> val box : Box[Foo] = Box(Ex1())
box: Box[Foo] = Box(Ex1())

scala> fooIt(box)
res32: Foo = Ex1()
~~~

## Exercise

### Covariant Error Handling

Covariance is the solution to our problem with `PossibleResult`. Recall that we couldn't define `NoResult` as an object because we ended up with a type error:

~~~ scala
final case object NoResult extends PossibleResult[Nothing]

val possible: PossibleResult[Int] = NoResult // type error
~~~

The type error is this: *`NoResult` is a `PossibleResult[Nothing]`, which is not a `PossibleResult[Int]`*. How can we make fix this type error? What would the code look like?

Hint: `Nothing` is a subtype of `Int`!

<div class="solution">
The solution involves making `PossibleResult[Nothing]` a subtype of `PossibleResult[Int]`. `Unit` is a subtype of `Int`, so if we make `PossibleResult` covariant we should be fine:

~~~ scala
sealed trait PossibleResult[+A]
final case class ActualResult[A](val value: A) extends PossibleResult[A]
final case object NoResult extends PossibleResult[Nothing]

val possible: PossibleResult[Int] = NoResult // no type errors!
~~~

This is more-or-less exactly how Scala's `Option` is defined. Here's a synopsis:

~~~ scala
sealed trait Option[+A]
final case class Some[A](get: A) extends Option[A] { /* ... */ }
final case object None extends Option[Nothing] { /* ... */ }
~~~
</div>
