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
