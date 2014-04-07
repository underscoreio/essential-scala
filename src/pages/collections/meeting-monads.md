---
layout: page
title: Meeting Monads
---

We've seen that by implementing a few methods (`map`, `flatMap`, and optionally `filter` and `foreach`), we can use any class with a **for comprehension**. This is an incredibly useful abstraction called a **monad** that goes way beyond iterating over sequences. For example, suppose we have a number of computations that could fail. Let's model this by having them return an `Option`[^try].

[^try]: If you were doing this for real, you may want some information on why the computation failed. In this case you may look at the `Either` or `Try` classes in the Scala standard library.

## An Overview of Option

We've seen `Option` before in passing. Here's a bit more detail.

`Option` is a sealed trait with two subtypes, `Some` and `None`. The `Some` type stores a value of some generic type `A`, whereas the `None` type indicates no value is available. This could be the case when getting a value from a `Map` and there is nothing stored, or it might indicate a computation that failed.

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

A third way is using for comprehensions, as we shall see.

The implementation of `map` and `flatMap` deserve a little explanation before we move on. The implementations on `Some` behave as you might expect -- they appliy the relevant function to the contents of the option and return the result. The implementations on `None` are simply noops, always returning `None`.

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

## What's in a Monad?

The concept of a monad is notoriously difficult to explain because it is so general. We recommend ignoring the functional programming literature on the subject unles you find it particularly fascinating.

Broadly speaking, a monad is a generic type that allows us to sequence computations while abstracting away some technicality. We do the sequencing using *for comprehensions*, worrying only about the programming logic we care about, and the code in the monad's `map` and `flatMap` methods does all of the plumbing for us. For example:

 - `Option` is a monad that allows us to sequence computations on optional values without worrying about the fact that they may or may not be present;

 - `Seq` is a monad that allows us to sequence computations that return multiple possible answers without worrying about the fact that there are lots of possible combinations involved;

 - `Future` is another popular monad that allows us to sequence asynchronous computations without worrying about the fact that they are asynchronous.

To demonstrate the generality of this principle, here are some examples. This first example calculates the sum of two numbers that may or may not be there:

~~~ scala
for {
  a <- getFirstNumber  // getFirstNumber  returns Option[Int]
  b <- getSecondNumber // getSecondNumber returns Option[Int]
} yield a + b

// The final result is an Option[Int] -- the result of
// applying `+` to `a` and `b` if both values are present
~~~

This second example calculate the sums of all possible pairs of numbers from two sequences:

~~~ scala
for {
  a <- getFirstNumbers  // getFirstNumbers  returns Seq[Int]
  b <- getSecondNumbers // getSecondNumbers returns Seq[Int]
} yield a + b

// The final result is a Seq[Int] -- the results of
// applying `+` to all combinations of `a` and `b`
~~~

This third example asynchronously calculates the sum ot two numbers that can only be obtained asynchronously (all without blocking):

~~~ scala
for {
  a <- getFirstNumber   // getFirstNumber  returns Future[Int]
  b <- getSecondNumber  // getSecondNumber returns Future[Int]
} yield a + b

// The final result is a Future[Int] -- a data structure
// that will eventually allow us to access the result of
// applying `+` to `a` and `b`
~~~

The important point here is that, if we ignore the comments, **these three examples look identical**. Monads allow us to forget about one part of the problem at hand -- optional values, multiple values, or asynchronously available values -- and focus on just the part we care about -- adding two numbers together.

There are many other monads that can be used to simplify problems in different circumstances. You may come across some of them in your future use of Scala. In this course we will concentrate entirely on `Seq` and `Option`.
