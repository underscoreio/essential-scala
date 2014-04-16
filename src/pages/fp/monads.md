---
layout: page
title: Monads
---

In the previous two sections we implemented a `Maybe` and a `Try` type. Both these types had the same interface of `fold`, `flatMap`, and `map`. The abstractions we've implemented are all **monads**. We encountered monads briefly in the section on collections. We've now seen other uses for types that implement the same simple interface. In this section we'll look at monads in more detail and discuss how we use them in Scala.

## Monads

A monad is nothing more than a type that implements a few methods which must obey a few laws. For a type `F[A]` the methods are:

* `flatMap` or `bind` with type `(f: A => F[B]): F[B]`; and
* `point` or `return` with type `(a: A): F[A]`.

In Scala we don't usually implement `point` directly, but instead use the relevant constructor.

The monad methods must also obey some laws. `point` must act as a neutral element:

* `point(a).flatMap(f)` equals `f(a)`
* `m.flatMap(point)` equals `m`

A sequence of `flatMap`s is the same as `flatMap`ping a derived function:

* `m.flatMap(f).flatMap(g)` equals `m.flatMap(x => f(x).flatMap(g))`

## Using Monads

There is no monad type per se in Scala (though there is in the Scalaz library) but for comprehensions work with any type implementing `flatMap` and `map`, and for comprehensions are the idiomatic way to use monads.

Remember that a for comprehension is compiled into a sequence of `flatMap` calls. For a monad of type `F[A]`, the type `A` can change in a `flatMap` but not the type `F`. Thus you can't mix several monads in a for comprehension. More concretely, the following will not work:

~~~ scala
scala> import scala.util.Try
import scala.util.Try

scala> for {
  x <- Some(1)
  y <- Try(2)
} yield x + y
<console>:11: error: type mismatch;
 found   : scala.util.Try[Int]
 required: Option[?]
                y <- Try(2)
                  ^
~~~

It's reasonably common to nest monads. For example, we might have a `Try`, representing a computation might fail, containing an `Option`, representing a value may be absent. To deal with nested monads we must nest for comprehensions. So the following works:

~~~ scala
scala> for {
  opt <- Try(Some(2))
} yield {
  for {
    v <- opt
  } yield v + 2
}
res3: scala.util.Try[Option[Int]] = Success(Some(4))
~~~

This nesting can get inconvenient, in which case we might turn to monad transformers to flatten our monads. Monad transformers are beyond the scope of this training material but you will want to learn about them if you get heavily into this style of programming.

## Other Monads

There are still more monads out there. For instance, we can represent concurrent programs using the `Future` monad. If you are interested in exploring monads further, I encourage you to look into the Scalaz library.

## Exercises

### So Many Options

Add up the sequence of options

~~~ scala
val options: Seq[Option[Int]] = Seq(Some(1), Some(2), Some(3), Some(4))
~~~

<div class="solution">
This is a fold, which we can implement using `foreach` and mutable state. Here I've used the for-comprehension equivalent of `foreach`.

~~~ scala
var sum = Some(0)
for {
  opt <- options
  x   <- opt
  y   <- sum
} sum = y + x
sum
~~~

Or we can implement it using `foldLeft` or `foldRight`.

~~~ scala
options.foldLeft(0){ (sum, opt) =>
  opt.map(_ + sum).getOrElse(sum)
}
~~~
</div>

### Abstracting All the Things

We've seen we can write the same code to add up the elements in a monad. Can we abstract this into a method that would work for any monad containing an `Int`? That is, could we write a method like:

~~~ scala
def addAllTheThings(monad1: ???[Int], monad2: ???[Int], monad3: ???[Int]): ???[Int] = {
  for {
    x <- monad1
    y <- monad2
    z <- monad3
  } yield x + y + z
}
~~~
Hint: we can add monads such as `Option[Int]`, `Seq[Int]`, and `Try[Int]`. Can we write a type that encompasses all of them?

<div class="solution">
This is a trick question. We can't write this method using the tools we have at this point. We need two more things: type classes and higher-kinded types.

Type classes allows us to note that a number of otherwise unrelated types (in this case 'Option', 'Seq', and 'Try') share a common interface (in this case 'flatMap').

Higher-kinded types allow us to abstract over a type constructor. The type `Option` is a type constructor: you supply a type to it, such as `Int`, to construct a concrete type (i.e. `Option[Int]`). This mirrors the way we supply a value (e.g. 2) to construct a concrete option (e.g. `Some(2)`). We know how to allow the type passed to the type constructor to vary -- we use a generic type. Higher-kinded types allow us to vary the type constructor as well.

The complete code is below. It's a bit verbose, but you'll see it works for `Option[Int]` and an other monad you define a `Monad` instance for (I've only defined `Option` below). E.g.

~~~ scala
scala> addAllTheThings(Some(1) : Option[Int], Some(2) : Option[Int], Some(3) : Option[Int])
res21: Option[Int] = Some(6)
~~~

(Note the type annotations on the options.)

At this point the code is probably hard to understand. That's ok. It's here more so you know this is possible than that we expect you to understand it.

~~~ scala
import scala.language.higherKinds

trait Monad[F[_]] {
  def flatMap[A, B](m: F[A], f: A => F[B]): F[B]
  def point[A](a: A): F[A]

  def map[A, B](m: F[A], f: A => B): F[B] =
    flatMap(m, (a: A) => point(f(a)))

  def foreach[A](m: F[A], f: A => Unit): Unit = {
    map(m, f)
    ()
  }
}

implicit object OptionMonad extends Monad[Option] {
  def flatMap[A, B](m: Option[A], f: A => Option[B]): Option[B] =
    m.flatMap(f)

  def point[A](a: A): Option[A] =
    Some(a)
}

implicit class AsMonad[F[_], A](m: F[A]) {

  def flatMap[B](f: A => F[B])(implicit i: Monad[F]): F[B] =
    i.flatMap(m, f)

  def point[A](a: A)(implicit i: Monad[F]): F[A] =
    i.point(a)

  def map[B](f: A => B)(implicit i: Monad[F]): F[B] =
    i.map(m, f)

  def foreach(f: A => Unit)(implicit i: Monad[F]): Unit =
    i.foreach(m, f)

}

def addAllTheThings[F[_]](monad1: F[Int], monad2: F[Int], monad3: F[Int])(implicit i: Monad[F]): F[Int] = {
  for {
    x <- monad1
    y <- monad2
    z <- monad3
  } yield x + y + z
}
~~~
</div>
