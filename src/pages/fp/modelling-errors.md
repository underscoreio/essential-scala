---
layout: page
title: Modelling Errors
---

In this previous section we saw how we can use `Option` (or `Maybe` as we called it) to handle errors by returning the empty case. This unfortunately throws away useful information, namely the cause of the error. It would be nice to preserve this information so we can take appropriate action.

Exceptions are the Java way of handling errors. Java's checked exceptions are generally considered a mistake, and Scala doesn't have checked exceptions. Exceptions suffer from the same problem as `null`: there is nothing in the type system that will fail if we don't properly handle exceptions[^concurrency]. We'd like to preserve the information contained in exceptions, such as the stack traces, but we would like to find a better way than throwing exceptions to communicate errors.

[^concurrency]: Exceptions are also a bad idea in concurrent systems. In a single threaded system an exception will halt the program and be printed on the console. In a concurrent programs exceptions can just silently kill threads.

## Try and Try Again

`Throwable` is the supertype of all the exceptions we should be catching. Implement a type `Try` that represents a successful computation or a `Throwable` on error.

<div class="solution">
~~~ scala
sealed trait Try[+A]
final case class Failure[A](val error: Throwable) extends Try[A]
final case class Success[A](val elt: A) extends Try[A]
~~~

This is just like `Maybe` except we store a `Throwable` in the failure case.
</div>

## Using Try

In the previous section we saw that `fold`, `map`, and `flatMap` were useful mehtods on `Maybe`. There are also useful methods on `Try`. Implement them.

<div class="solution">
~~~ scala
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
~~~

In this case I've implemented the auxillary functions in terms of `fold`, which saves on code duplication.
</div>

## Try in Scala

Scala (2.9.3+) comes with `scala.util.Try`, which works the same as our `Try` with the exception that, like `Option`, it lacks a `fold` method and instead uses `map` and `getOrElse`.
