---
layout: page
title: Variance
---
In this section we cover **variance annotations**, which allow us to control subclass relationships between types with type parameters. To motivate this, let's look again at our invariant generic sum type pattern.

The invariant generic sum type pattern is a bit unsatisfying. Recall our `Maybe` type, which we defined as

~~~ scala
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]
~~~

Ideally we would like to drop the unused type parameter on `Empty` and write something like

~~~ scala
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[???]
~~~

However, objects can't have type parameters. In order to make `Empty` an object we need to provide a concrete type in the `extends Maybe` part of the definition. But what type parameter should we use? In the absence of a preference for a particular data type, we could use something like `Unit` or `Nothing`. However this leads to type errors:

~~~ scala
scala> :paste
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]
^D

defined trait Maybe
defined class Full
defined module Empty

scala> val possible: Maybe[Int] = Empty
<console>:9: error: type mismatch;
 found   : Empty.type
 required: Maybe[Int]
       val possible: Maybe[Int] = Empty
~~~

The problem here is that `Empty` is a `Maybe[Nothing]` and a `Maybe[Nothing]` is not a subtype of `Maybe[Int]`. To overcome this issue we need to introduce variance annotations.

However, we now know how to overcome this problem: we can use a variance annotation, which we learned about in the last section. Covariance is the most natural variance in this case.

In the Scala standard library, this abstraction is called `Option`, with cases `Some` and `None`.

~~~ scala
sealed trait Maybe[+A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]

object division {
  def apply(num: Int, den: Int) =
    if(den == 0) Empty else Full(num / den)
}
~~~

This pattern is the most commonly used one with generic sum types.

<div class="callout callout-info">
#### Covariant Generic Sum Type Pattern

If `A` of type `T` is a `B` or `C`, and `C` is not generic, write

~~~ scala
sealed trait A[+T]
final case class B[T](t: T) extends A[T]
final case object C extends A[Nothing]
~~~
</div>

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
