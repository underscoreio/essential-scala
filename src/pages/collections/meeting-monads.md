---
layout: page
title: Meeting Monads
---

We've seen that by implementing a few methods (`map`, `flatMap`, and optionally `filter` and `foreach`) we can use any class with a for comprehension, and that this is a useful abstraction. This abstraction is called a monad and has extremely wide application. For example, suppose we have a number of computations that could fail. Let's model this by having them return an `Option`[^try].

[^try]: If you were doing this for real you'd want some information on why it failed. Look at `Either` or `Try`, the later being Scala 2.10 and above.

## An Overview of Option

We've seen `Option` before in passing. Here's a bit more detail.

`Option` is a sealed trait with two subtypes, `Some` and `None`. The `None` type indicates no value is available. This could be the case when getting a value from a `Map` and there is nothing stored, or it might indicate a computation that failed. The `Some` type stores a value of some generic type `A`.

We can process `Option`s using pattern matching, but also with `map`, `flatMap`, and other functions familiar from collections.

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

Or we could write

~~~ scala
computationOne() flatMap {
  x => computationTwo(x) flatMap {
    y => ...
  }
}
~~~

Both are rather cumbersome. With a for comprehension we can simply write

~~~ scala
for {
  x <- computationOne()
  y <- computationTwo(x)
  ...
} yield ...
~~~

Here we've done something completely different to our previous examples using collections. Yet the same basic abstraction applies! The more you look the more examples of monads you'll find, and for comprehensions give us a powerful and generic way to work with them.
