---
layout: page
title: Meeting Monads
---

We've seen that by implementing a few methods (`map`, `flatMap`, and optionally `filter` and `foreach`), we can use any class with a **for comprehension**. This is an incredibly useful abstraction called a **monad** that goes way beyond iterating over sequences. For example, suppose we have a number of computations that could fail. Let's model this by having them return an `Option`[^try].

[^try]: If you were doing this for real, you may want some information on why the computation failed. In this case you may look at the `Either` or `Try` classes in the Scala standard library.

## An Overview of Option

We've seen `Option` before in passing. Here's a bit more detail.

`Option` is a sealed trait with two subtypes, `Some` and `None`:

~~~ scala
sealed trait Option[+A]
case class Some[A](value: A) extends Option[A]
case object None extends Option[Nothing]
~~~

The `Some` type stores a value of some generic type `A`, whereas the `None` type indicates no value is available. This could be the case when getting a value from a `Map` and there is nothing stored, or it might indicate a computation that failed.

We have a couple of ways of processing `Options`. One is using pattern matching:

~~~ scala
someOption match {
  case Some(value) => // do something with `value`
  case None =>        // do something else
}
~~~

Another is using `map` and `flatMap`:

~~~ scala
scala> Some(123).map(_.toString)
res0: Option[String] = Some(123)

scala> None.map(_.toString)
res1: Option[String] = None
~~~

## Sequencing Computations

Returning to our example, imagine we have a series of computations that could fail. Each computation returns an `Option` to indicate success or failure. We'd like to sequence these computations returning a `None` as soon as we encounter failure, otherwise returning a `Some` with the final value.

We could write this:

~~~ scala
computationOne() match {
  case Some(x) =>
    computationTwo(x) match {
      case Some(y) => ...
      case None => None
    }
  case None => None
}
~~~

Or we could write:

~~~ scala
computationOne() flatMap { x =>
  computationTwo(x) flatMap { y =>
    // ...
  }
}
~~~

Both are rather cumbersome. With a for comprehension we can simply write:

~~~ scala
for {
  x <- computationOne()
  y <- computationTwo(x)
  ...
} yield ...
~~~

Here we've done something completely different to our previous examples using collections. Yet the same basic abstraction applies! The more you look the more examples of monads you'll find, and for comprehensions give us a powerful and generic way to work with them.
