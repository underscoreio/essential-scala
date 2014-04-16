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

scala> Some(0).flatMap(x => if(x == 0) None else Some(10/x))
res2: Option[Int] = None
~~~

A third way is using for comprehensions, as we shall see.

The implementation of `map` and `flatMap` deserve a little explanation before we move on. The implementations on `Some` behave as you might expect -- they appliy the relevant function to the contents of the option and return the result. The implementations on `None` are simply noops, always returning `None`.

## Exercises

### Some more

Start with this definition:

~~~
val anOption: Option[Int] = Some(1)
~~~

Add 41 to `anOption` creating a new `Option[Int]` with the total. Use pattern matching or `map` as you prefer.

<div class="solution">
Using pattern matching:

~~~ scala
anOption match {
  case Some(x) => Some(x + 41)
  case None    => None
}
~~~

Using map:

~~~ scala
anOption.map(_ + 41)
~~~
</div>

### No more

Replace the definition of `anOption` with

~~~
val anOption: Option[Int] = None
~~~

Run your code again. What result do you get?

<div class="solution">
You should get `None` now.
</div>

### Adding Options

Start with the following definitions:

~~~ scala
val opt1: Option[Int] = Some(1)
val opt2: Option[Int] = Some(2)
~~~

Add together the values in the options, creating a new `Option[Int]`. Use whichever technique you prefer.

<div class="solution">
We need to use `flatMap` and `map` in sequence here.

~~~ scala
opt1.flatMap(x => opt2.map(y => x + y))
~~~

Alternatively we can use pattern matching.

~~~ scala
opt1 match {
  case Some(x) =>
    opt2 match {
      case Some(y) => Some(x + y)
      case None    => None
    }
  case None =>
    None
}
~~~
</div>

### More Adding!

Now take three options and add them together.

~~~ scala
val opt1: Option[Int] = Some(1)
val opt2: Option[Int] = Some(2)
val opt3: Option[Int] = Some(3)
~~~

<div class="solution">
It's the same pattern, but more of it. It's getting a bit painful now.

~~~ scala
opt1.flatMap(x => opt2.flatMap(y => opt3.map(z => x + y + z)))
~~~

Or,

~~~ scala
opt1 match {
  case Some(x) =>
    opt2 match {
      case Some(y) =>
        opt3 match {
          case Some(z) => Some(x + y + z)
          case None    => None
        }
      case None => None
    }
  case None => None
}
~~~
</div>

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

You should recognise this style of code from the exercises. It quickly gets cumbersome. With a for comprehension we can simply write:

~~~ scala
for {
  x <- computationOne()
  y <- computationTwo(x)
  ...
} yield ...
~~~

Here we've done something completely different to our previous examples using collections. Yet the same basic abstraction applies! The more you look the more examples of monads you'll find, and for comprehensions give us a powerful and generic way to work with them.

## Exercises

### Adding Like a Machine

Using a for comprehension add together the options

~~~ scala
val opt1: Option[Int] = Some(1)
val opt2: Option[Int] = Some(2)
val opt3: Option[Int] = Some(3)
~~~

<div class="solution">
~~~ scala
for {
  x <- opt1
  y <- opt2
  z <- opt3
} yield x + y + z
~~~

Wasn't that easier?
</div>

## What's in a Monad?

The concept of a monad is notoriously difficult to explain because it is so general. We can get a good intuitive understanding by comparing some of the types of monad that we will deal with on a regular basis.

Broadly speaking, a monad is a generic type that allows us to sequence computations while abstracting away some technicality. We do the sequencing using *for comprehensions*, worrying only about the programming logic we care about. The code hidden in the monad's `map` and `flatMap` methods does all of the plumbing for us. For example:

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

This third example asynchronously calculates the sum of two numbers that can only be obtained asynchronously (all without blocking):

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

## Exercises

### Adding All the Things

Use the following definitions:

~~~ scala
import scala.util.Try

val opt1 = Some(1)
val opt2 = Some(2)
val opt3 = Some(3)

val seq1 = Seq(1)
val seq2 = Seq(2)
val seq3 = Seq(3)

val try1 = Try(1)
val try2 = Try(2)
val try3 = Try(3)
~~~

Add together all the options to create a new option. Add together all the sequences to create a new sequence. Add together all the trys to create a new try. Use a for comprehension, like a boss.

<div class="solution">
~~~ scala
for {
 x <- opt1
 y <- opt2
 z <- opt3
} yield x + y +z

for {
 x <- seq1
 y <- seq2
 z <- seq3
} yield x + y +z

for {
 x <- try1
 y <- try2
 z <- try3
} yield x + y +z
~~~

How's that for a cut-n-paste job?
</div>
