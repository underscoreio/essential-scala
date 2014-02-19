---
layout: page
title: Modelling with Types
---

In this section we're going to explore some common functional programming patterns building on structural recursion. We will see how we can use the type system to our advantage to ensure our code maintains certain properties. We're going to concentrate on two cases: handling values that may be missing (where we'd use `null` in Java), and enforcing error checking without the pain of checked exceptions.

## Making Null Optional

There are many times that our programs must deal with missing data. For example, when parsing HTTP request parameters we should consider the possibility that the parameter may be missing. More concretely, when we try to get an element from a `Map` and that element doesn't exist, what should we do? It's not an error to ask for an element that doesn't exist, so we shouldn't raise an exception. Instead we should return some special value to indicate this condition. In Java we return `null`. `Null` is a member of every type so we can return it no matter what type we are expecting. There is a problem with this, however. If we forget to check for `null` we will receive no warning. We'll just have buggy code waiting to break. Let's do better.

We're going to approach this problem using structural recursion. What are the types of data we're dealing with? There are clearly two cases: empty and not-empty. Thus we have a sum type.

## Exercises

#### Call Me Maybe

Write the code for our sum type `Maybe`.

Hint: Remember that `Nothing` is a sub-type of every type.

<div class="solution">
~~~ scala
sealed trait Maybe[+A]
final case object Empty extends Maybe[Nothing]
final case class Full[A](val elt: A) extends Maybe[A]
~~~

The two cases are:

* The non-empty case, which holds an element. Thus element type is not specified in advance and so it must be generic.
* The empty case, which holds no data and thus can be a case object.

`Maybe` is covariant so sub-types of `A` are allowed in a `Maybe[A]`. This allows `Empty` to extends `Maybe[Nothing]` and be the empty element for any `Maybe[A]`. We could instead declare `Empty` as a case class with a generic type `A` and thus avoid making `Maybe` covariant.
</div>


#### Better Than Maybe

What functions should our `Maybe` type have to be generally useful?

<div class="solution">
We should have `fold`, which is the generic traversal operator. From `fold` we can derive `map`, `flatMap`, and `foreach`, which are the functions expected by for comprehensions. (We should also consider implementing `filter` but I've skipped it here. Feel free to implement it yourself!)
</div>

Implement these functions as methods on `Maybe`.

<div class="solution">
~~~ scala
sealed trait Maybe[+A] {
  def fold[B](full: A => B, empty: B): B
  def map[B](f: A => B): Maybe[B]
  def flatMap[B](f: A => Maybe[B]): Maybe[B]
  def foreach(f: A => Unit): Unit
}

final case object Empty extends Maybe[Nothing] {
  def fold[B](full: Nothing => B, empty: B): B =
    empty
  def map[B](f: Nothing => B): Maybe[B] =
    Empty
  def flatMap[B](f: Nothing => Maybe[B]): Maybe[B] =
    Empty
  def foreach(f: Nothing => Unit): Unit =
    ()
}

final case class Full[A](val elt: A) extends Maybe[A] {
  def fold[B](full: A => B, empty: B): B =
    full(elt)
  def map[B](f: A => B): Maybe[B] =
    Full(f(elt))
  def flatMap[B](f: A => Maybe[B]): Maybe[B] =
    f(elt)
  def foreach(f: A => Unit): Unit =
    f(elt)
}
~~~
</div>

#### Using Maybe

Our `Maybe` type provides a type-safe way to handle missing values. If we have a `Maybe` we *must* say how we're going to deal with missing values to get a value out of our `Maybe`. Imagine, for example, we have a `Maybe[Int]`. If there is no value we want the empty string. Otherwise we want to convert the `Int` to a string. To do this is simple using `fold`.

~~~ scala
scala> val full = Full(1)
val full = Full(1)
full: Full[Int] = Full(1)

scala> val empty: Maybe[Int] = Empty
val empty: Maybe[Int] = Empty
empty: Maybe[Int] = Empty

scala> full fold (full = x => x.toString, empty = "")
full fold (full = x => x.toString, empty = "")
res17: String = 1

scala> empty fold (full = x => x.toString, empty = "")
empty fold (full = x => x.toString, empty = "")
res18: String = ""
~~~

The following method converts a `String` to a `Maybe[Int]`.

~~~ scala
def stringToMaybeInt(in: String): Maybe[Int] =
  try {
    Full(in.toInt)
  } catch {
    case e: NumberFormatException => Empty
  }
~~~

Use it to write code to convert a `String` to an `Int`, returning 0 if the `String` does not represent an `Int`.

<div class="solution">
~~~ scala
def stringToInt(in: String): Int =
  stringToMaybeInt(in) fold (full = x => x, empty = 0)
~~~
</div>

## Option in Scala

Our `Maybe` type is called `Option` in Scala. It's two cases are called `Some` and `None`.  It is ubiquitous in Scala, and works just like `Maybe` except it doesn't provide a `fold` method[^scalaz]. To write the equivalent of `fold` we use a combination of `map` and `getOrElse` as illustrated below.

~~~ scala
scala> val some: Option[Int] = Some(1)
val some: Option[Int] = Some(1)
some: Option[Int] = Some(1)

scala> val none: Option[Int] = None
val none: Option[Int] = None
none: Option[Int] = None

scala> some map (x => x.toString) getOrElse ("")
some map (x => x.toString) getOrElse ("")
res21: String = 1

scala> none map (x => x.toString) getOrElse ("")
none map (x => x.toString) getOrElse ("")
res22: String = ""
~~~

[^scalaz]: A `fold` method for `Option` is provided by the Scalaz library.
