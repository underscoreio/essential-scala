---
layout: page
title: Modelling Errors
---

In this previous section we saw how we can use `Option` (or `Maybe` as we called it) to handle errors by returning the empty case. This unfortunately throws away useful information, namely the cause of the error. It would be nice to preserve this information so we can take appropriate action.

Exceptions are the way we handle errors in Java. However, exceptions suffer from the same problem as `null`: there is nothing in the type system that will fail if we don't properly handle exceptions[^concurrency]. Scala doesn't have checked exceptions like Java. While it does have unchecked exceptions we would like a more functional way of modelling errors using types.

[^concurrency]: Exceptions are also a bad idea in concurrent systems where we don't have a single stack to capture the scope of the error.

## Try and Try Again

`Throwable` is the supertype of all the exceptions we should be catching. Implement a type `Try` that represents a successful computation or a `Throwable` on error.

<div class="alert alert-info">
Tip: Although we're using `Throwable` we aren't actually `throwing` any exceptions in this exercise. We are simply using the `Throwable` type to represent an error, wrapping them in an instance of `Try` to return them when an error happens.
</div>

<div class="solution">
```scala
sealed trait Try[+A]
final case class Success[A](elt: A) extends Try[A]
final case class Failure[A](error: Throwable) extends Try[A]
```

This is just like `Maybe` except we store a `Throwable` in the failure case. Using a `Throwable` as an error type has the advantage that we can print the location of the error using `throwable.printStackTrace`:

```scala
scala> val result: Try[Int] = Failure(new Exception("Boom!"))
result: Try[Int] = Failure(java.lang.Exception: Boom!)

scala> result match {
     |   case Success(elt)   => println(elt)
     |   case Failure(error) => error.printStackTrace
     | }
java.lang.Exception: Boom!
  // and so on...
```
</div>

### Exercise: Using Try

In the previous section we saw that `fold`, `map`, and `flatMap` were useful mehtods on `Maybe`. There are also useful methods on `Try`. Let's implement them.

<div class="solution">
```scala
sealed trait Try[+A] {
  def fold[B](failure: Throwable => B, success: A => B): B

  def map[B](f: A => B): Try[B] =
    flatMap[B](e => Success(f(e)))

  def flatMap[B](f: A => Try[B]): Try[B] =
    fold[Try[B]](failure = t => Failure(t), success = e => f(e))

  def foreach(f: A => Unit): Unit = {
    map(f)
    ()
  }
}

final case class Failure[A](val error: Throwable) extends Try[A] {
  def fold[B](failure: Throwable => B, success: A => B): B =
    failure(error)
}

final case class Success[A](val elt: A) extends Try[A] {
  def fold[B](failure: Throwable => B, success: A => B): B =
    success(elt)
}
```

In this case I've implemented the auxillary functions in terms of `fold`, which saves on code duplication.
</div>

## Try in Scala

Scala (2.9.3+) comes with `scala.util.Try`, which works the same as our `Try` with the exception that, like `Option`, it lacks a `fold` method and instead uses `map` and `getOrElse`.
