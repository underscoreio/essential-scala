---
layout: page
title: Modelling with Types
---

In the next sections we will explore some common functional programming patterns building on structural recursion. We will see how we can use the type system to our advantage to ensure our code maintains certain properties.

In section we're going to focus on modelling missing values using the `Maybe` type we introduced in the [Modelling Data](/objects/generics.html) exercises.

## Making Null Optional

If you remember, we implemented `Maybe` as a way of eliminating `nulls`. Looking at our code in a fresh light, we can see that it is a *sum type* with two cases -- `Full` and `Empty`:

~~~ scala
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]
~~~

`Maybe` is a simplified version of the `Option` type that ships with the core Scala libraries. The main difference is that `Option` is a *monad* with extra methods that we have not implemented. We can learn a lot about `Option` and structural recursion by implementing some of these methods ourselves. First, however, let's revisit the problem of how to eliminate the type parameter from `Empty[A]`.

## Covariance Redux

The way we use generics in `Maybe` is a bit inconvenient. We have to declare a generic type on the `Empty` case even though that case doesn't store any data. Ideally we would like to define `Empty` as a singleton object as follows:

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

The problem is that `Empty` is not a subtype of `Maybe[Int]`. This is because `Maybe[A]` is **invariant** in its type parameter `A`.

We touched on variance earlier in the course. Generic types in Scala are invariant by default, meaning that for a type `Foo[A]` neither subtypes nor supertypes of `A` make a subtype of `Foo[A]`. This is the behaviour we're seeing with `Maybe`.

**Covariance** is the behaviour most people expect from `Maybe`. For a covariant type `Foo` a subtype of `A` is a subtype of `Foo[A]`. We can make `Maybe` covariant by defining it as `Maybe[+A]` instead of `Maybe[A]`.

### Exercise: Covariant Maybe

Redefine `Maybe` to be covariant and redefine `Empty` to be a singleton object rather than a generic class. Verify that the covariance of `Maybe` fixes the type error above:

<div class="solution">
Here's the code:

~~~ scala
sealed trait Maybe[+A]
final case class Full[A](elt: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]
~~~

`Maybe` is covariant so sub-types of `A` are allowed in a `Maybe[A]`. This allows `Empty` to extend `Maybe[Nothing]` and be the empty element for any `Maybe[A]`.
</div>

## Not Just A Maybe

Now our `Maybe` type looks a lot like `Option` although we are still missing the methods we need to make it generally useful.

What methods should we add to `Maybe` to make it easier to work with (tip: think about collections and for comprehensions)?

<div class="solution">
We should have `fold`, which is the generic traversal operator. From `fold` we can derive `map`, `flatMap`, and `foreach`, which are the functions expected by for comprehensions. (We should also consider implementing `filter` but I've skipped it here. Feel free to implement it yourself!)
</div>

### Exercise: Methodical Maybe

Implement each of the methods above!

<div class="solution">

First let's look at `fold`. As per our recipe, the method needs to take arguments for each case of our `Maybe`. In this case our argument for `Full` is a function from `A` to a result and our argument for `Empty` is a simple value:

~~~ scala
sealed trait Maybe[+A] {
  def fold[B](full: A => B, accumulator: B): B
}

final case class Full[A](elt: A) extends Maybe[A] {
  def fold[B](full: A => B, empty: B): B =
    full(elt)
}

final case object Empty extends Maybe[Nothing] {
  def fold[B](full: Nothing => B, accumulator: B): B =
    accumulator
}
~~~

We could define all of the remaining methods in terms of `fold`, but it is probably marginally more efficient to define them directly.

Let's look at `map` and `flatMap`. In each case the `Full` implementation applies the argument to the value, while the `Empty` method simply returns `Empty`:

~~~ scala
sealed trait Maybe[+A] {
  def map[B](f: A => B): Maybe[B]

  def flatMap[B](f: A => Maybe[B]): Maybe[B]
}

final case class Full[A](elt: A) extends Maybe[A] {
  def map[B](f: A => B): Maybe[B] =
    Full(f(elt))

  def flatMap[B](f: A => Maybe[B]): Maybe[B] =
    f(elt)
}

final case object Empty extends Maybe[Nothing] {
  def map[B](f: Nothing => B): Maybe[B] =
    Empty

  def flatMap[B](f: Nothing => Maybe[B]): Maybe[B] =
    Empty
}
~~~

Finally, the implementation of `foreach` is simple: `Full` calls the function while `Empty` does nothing:

~~~ scala
sealed trait Maybe[+A] {
  def foreach(f: A => Unit): Unit
}

final case class Full[A](elt: A) extends Maybe[A] {
  def foreach(f: A => Unit): Unit =
    f(elt)
}

final case object Empty extends Maybe[Nothing] {
  def foreach(f: Nothing => Unit): Unit =
    ()
}
~~~
</div>

## Using Maybe

As we have seen previously, our `Maybe` type provides a type-safe way to handle missing values. If we have a `Maybe` we *must* say how we're going to deal with missing values to get a value out. We have already seen how to do this with pattern matching -- we now have `fold` as another means to do the same thing.

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

As a concrete example, here is a method that attempts to parse a `String` to a `Maybe[Int]`:

~~~ scala
def stringToMaybeInt(in: String): Maybe[Int] =
  try {
    Full(in.toInt)
  } catch {
    case exn: NumberFormatException =>
      Empty
  }
~~~

We can combine this method definition with `fold` to produce a variation on this method that returns `0` if the `String` is not a valid number:

<div class="solution">
~~~ scala
def stringToInt(in: String): Int =
  stringToMaybeInt(in) fold (full = x => x, empty = 0)
~~~
</div>

## Option in Scala

Our `Maybe` type is called `Option` in Scala. It's two cases are called `Some` and `None`.  It is ubiquitous in Scala, and works just like `Maybe` except it doesn't provide a `fold` method[^scalaz]. To write the equivalent of `fold` we use a combination of `map` and `getOrElse` as illustrated below:

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
