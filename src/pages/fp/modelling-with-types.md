---
layout: page
title: Modelling with Types
---

In this section we're going to explore some common functional programming patterns building on structural recursion. We will see how we can use the type system to our advantage to ensure our code maintains certain properties. In section we're going to focus on handling missing values (where we'd use `null` in Java). This will introduce some new features of generics and give us more experience with monads, which we first saw in the [collections](/collections/meeting-monads.html).

Here we're going to concentrate on two cases: handling values that may be missing (where we'd use `null` in Java), and enforcing error checking without the pain of checked exceptions.

## Making Null Optional

There are many times that our programs must deal with missing data. For example, when we try to get an element from a `Map` and that element doesn't exist, what should we do? It's not an error to ask for an element that doesn't exist, so we shouldn't raise an exception. Instead we should return some special value to indicate this condition. In Java we return `null`. `Null` is a member of every type so we can return it no matter what type we are expecting. There is a problem with this, however. If we forget to check for `null` we will receive no warning. We'll just have buggy code waiting to break. Let's do better.

We're going to approach this problem using structural recursion. What are the types of data we're dealing with? There are clearly two cases: empty and not-empty. Thus we have a sum type.

#### Exercise: Call Me Maybe

Write the code for our sum type `Maybe`.

Hint: Remember that `Nothing` is a sub-type of every type.

<div class="solution">
~~~ scala
sealed trait Maybe[A]
final case class Empty[A] extends Maybe[A]
final case class Full[A](elt: A) extends Maybe[A]
~~~

The two cases are:

* The non-empty case, which holds an element.
* The empty case, which holds no data.
</div>


## Covariance

The way we use generics in `Maybe` is a bit inconvenient. We have to declare a generic type on the `Empty` case even though that case doesn't store any data. Recall that `Nothing` is a subtype of all types, so we could instead declare `Empty` as

~~~ scala
final case object Empty extends Maybe[Nothing]
~~~

However this will not work in the way we expect. Consider the following code, where we have a `val` of type `Maybe[Int]` that we try to assign to `Empty`:

~~~ scala
scala> val maybe: Maybe[Int] = Empty
<console>:9: error: type mismatch;
 found   : Empty.type
 required: Maybe[Int]
Note: Nothing <: Int (and Empty.type <: Maybe[Nothing]), but trait Maybe is invariant in type A.
You may wish to define A as +A instead. (SLS 4.5)
       val maybe: Maybe[Int] = Empty
~~~

The problem is that `Empty` is not a subtype of `Maybe[Int]`. Subtyping relationships with generic types are subtle. By default generic types in Scala are **invariant**, meaning that for a type `F[A]` neither subtypes nor supertypes of `A` make a subtype of `F[A]`. This is the behaviour we're seeing with `Maybe`.

**Covariance** is the behaviour most people expect. For a covariant type `F` a subtype of `A` is a subtype of `F[A]`. We can make a generic type covariant by introducing a generic type as `+A` instead of `A`.

#### Exercise: Covariant Maybe

Redefine `Maybe` to be covariant.

<div class="solution">
~~~ scala
sealed trait Maybe[+A]
final case object Empty extends Maybe[Nothing]
final case class Full[A](val elt: A) extends Maybe[A]
~~~


`Maybe` is covariant so sub-types of `A` are allowed in a `Maybe[A]`. This allows `Empty` to extend `Maybe[Nothing]` and be the empty element for any `Maybe[A]`.
</div>


## Contravariance

**Contravariance** is the opposite of covariance. For a contravariant type `F` a supertype of `A` is a subtype of `F[A]`.

Why would we ever want contravariance? The main example is in function types. Consider the `map` function on a `Seq[A]`. For concreteness imagine that `A` is type `Dog`. What is the type of functions can we pass to `map`? We can pass a function `Dog => String`. We can't pass a function `Collie => String` (`Collie` is a subtype of `Dog`) because we can't guarantee that all our dogs are collies. Thus subtypes of `A` (in this case `Dog`) aren't allowed. We can pass functions of type `Animal => String`, `Animal` being a supertype of `Dog`, because all our dogs are animals. Thus supertypes are ok. This is exactly contravariance. Thus functions are contravariant in their parameters (and covariant in their return type).

There is no doubt that variance is confusing to many. The good news is it hardly ever comes up in application code. We can typically settle for invariant types at the cost of a few type declarations to keep the compiler happy. As we saw with `Empty` we can add a nuisance type parameter to avoid variance annotations, and wherever we use the type we must fill in the nuisance parameter.

## Not Just A Maybe

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

We could define all these functions in terms of `fold`. It is probably marginally more efficient to define them directly, at the cost of more code.
</div>

## Using Maybe

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
