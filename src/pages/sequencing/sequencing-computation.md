---
layout: page
title: Sequencing Computation
---

We have now mastered generic data and folding over algebraic data types. For the final section of this chapter we will look as some other common patterns of computation that are 1) often more convenient to use than fold for algebraic data types and 2) can be implemented for certain types of data that do not support a fold. These methods are known as **map** and **flatMap**.

## Map

Imagine we have a list of `Int` user IDs, and a function to go from user ID to a `User` record. We want to get a list of user records for all the IDs in the list. Written as types we have `List[Int]` and a function `Int => User`, and we want to get a `List[User]`.

Imagine we have an optional value representing a user record loaded from the database and a function that will load their most recent order. If we have a record we want to then lookup the user's most recent order. That is, we have a `Maybe[User]` and a function `User => Order`, and we want a `Maybe[Order]`.

Imagine we have a sum type representing an error message or a completed order. If we have a completed order we want to get the total value of the order. That is, we have a `Sum[String, Order]` and a function `Order => Double`, and we want `Sum[String, Double]`.

What these all have in common is we have a type `F[A]` and a function `A => B`, and we want a result `F[B]`. The method that performs this operation is called `map`.

Let's implement `map` for `LinkedList`. We start by outlining the types:

~~~ scala
sealed trait LinkedList[+A] {
  def map[B](fn: A => B): LinkedList[B]
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def map[B](fn: A => B): LinkedList[B] =
    ???
}
final case object Empty extends LinkedList[Nothing] {
  def map[B](fn: A => B): LinkedList[B] =
    ???
}
~~~

We know we can use the structural recursion pattern as we know that `fold` (which is just the structural recursion pattern abstracted) is the universal iterator for an algebraic data type. Thus:

- For `Pair` we have to combine `head` and `tail` to return a `LinkedList[B]` (as the types tell us) and we also know we need to recurse on `tail`. We can write

  ~~~ scala
  def map[B](fn: A => B): LinkedList[B] = {
    val newTail: LinkedList[B] = tail.map(fn)
    // Combine newTail and head to create LinkedList[B]
  }
  ~~~

  We can convert `head` to a `B` using `fn`, and then build a larger list from `newTail` and our `B` giving us the final solution

  ~~~ scala
  def map[B](fn: A => B): LinkedList[B] =
    Pair(fn(head), tail.map(fn))
  ~~~

- For `Empty` we don't have any value of `A` to apply to the function. The only thing we can return is an `Empty`.

Therefore the complete solution is

~~~ scala
sealed trait LinkedList[+A] {
  def map[B](fn: A => B): LinkedList[B]
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def map[B](fn: A => B): LinkedList[B] =
    Pair(fn(head), tail.map(fn))
}
final case object Empty extends LinkedList[Nothing] {
  def map[B](fn: Nothing => B): LinkedList[B] =
    Empty
}
~~~

The astute reader will note that for the `Empty` case I've changed the type of `fn` to `Nothing => B`. Using the covariant sum type pattern the `Empty` case doesn't have a generic type variable. We can get around the problem by giving `fn` type `Nothing => B` and, because functions are contravariant in their parameters, any function is acceptable as all types are supertypes of `Nothing`.


## FlatMap

Now imagine the following examples:

- We have a list of users and we want to get a list of all their orders. That is, we have `LinkedList[User]` and a function `User => LinkedList[Order]`, and we want `LinkedList[Order]`.

- We have an optional value representing a user loaded from the database, and we want to lookup their most recent order -- another optional value. That is, we have `Maybe[User]` and `User => Maybe[Order]`, and we want `Maybe[Order]`.

What these all have in common is we have a type `F[A]` and a function `A => F[B]`, and we want a result `F[B]`. The method that performs this operation is called `flatMap`.

Let's implement `flatMap` for `Maybe` (we need an append method to implement `flatMap` for `LinkedList`). We start by outlining the types:

~~~ scala
sealed trait Maybe[+A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B]
}
final case class Full[A](value: A) extends Maybe[A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
    ???
}
final case object Empty extends Maybe[Nothing] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
    ???
}
~~~

We use the same pattern as before: it's a structural recursion and our types guide us in filling in the method bodies.

~~~ scala
sealed trait Maybe[+A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B]
}
final case class Full[A](value: A) extends Maybe[A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
    fn(value)
}
final case object Empty extends Maybe[Nothing] {
  def flatMap[B](fn: Nothing => Maybe[B]): Maybe[B] =
    Empty
}
~~~

## Functors and Monads

A type like `F[A]` with a `map` method is called a *functor*. If a functor also has a `flatMap` method it is called a *monad*.

Although the most immediate applications of `map` and `flatMap` are in collection classes like lists, the bigger picture is sequencing computations. Imagine we have a number of computations that can fail. For instance

~~~ scala
def mightFail1: Maybe[Int] =
  Full(1)

def mightFail2: Maybe[Int] =
  Full(2)

def mightFail3: Maybe[Int] =
  Empty // This one failed
~~~

We want to sequence all these computations. If any one of them fails the whole computation fails. Otherwise we'll add up all the numbers we get. We can do this with `flatMap` as follows.

~~~ scala
mightFail1 flatMap { x =>
  mightFail2 flatMap { y =>
    mightFail3 flatMap { z =>
      Full(x + y + z)
    }
  }
}
~~~

The result of this is `Empty`. If we drop `mightFail3`, leaving just

~~~ scala
mightFail1 flatMap { x =>
  mightFail2 flatMap { y =>
    Full(x + y)
  }
}
~~~

the computation succeeds and we get `Full(3)`.

The general idea is a monad represents a value in some context. The context depends on the monad we're using. We've seen examples where the context is:

- an optional value, such as we might get when retrieving a value from a database;
- an sum of values, which might represent a error message and a value we're computing with; and
- a list of values.

We use `map` when we want to transform the value within the context to a new value, while keeping the context the same. We use `flatMap` when we want to transform the value *and* possibly provide a new context.

## Exercises

### Mapping Lists

Given the following list

~~~ scala
val list: LinkedList[Int] = Pair(1, Pair(2, Pair(3, Empty)))
~~~

- double all the elements in the list;
- add one to all the elements in the list; and
- divide by three all the elements in the list.

<div class="solution">
These exercises just get you used to using `map`.

~~~ scala
list.map(_ * 2)
list.map(_ + 1)
list.map(_ / 3)
~~~
</div>

### Mapping Maybe

Implement `map` for `Maybe`.

<div class="solution">
~~~ scala
sealed trait Maybe[+A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B]
  def map[B](fn: A => B): Maybe[B]
}
final case class Full[A](value: A) extends Maybe[A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
    fn(value)
  def map[B](fn: A => B): Maybe[B]
    Full(fn(value))
}
final case object Empty extends Maybe[Nothing] {
  def flatMap[B](fn: Nothing => Maybe[B]): Maybe[B] =
    Empty
  def map[B](fn: A => B): Maybe[B] =
    Empty
}
~~~
</div>

For bonus points, implement `map` in terms of `flatMap`. Hint: you should just implement `map` on the `Maybe` trait.

<div class="solution">
~~~ scala
sealed trait Maybe[+A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B]
  def map[B](fn: A => B): Maybe[B] =
    this.flatMap(a => Full(fn(a)))
}
final case class Full[A](value: A) extends Maybe[A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
    fn(value)
}
final case object Empty extends Maybe[Nothing] {
  def flatMap[B](fn: Nothing => Maybe[B]): Maybe[B] =
    Empty
}
~~~
</div>


### Sequencing Computations

Both our `LinkedList` and `Maybe` declare a class `Empty` so we're going to use Scala's builtin `List` class for this exercise. It has the same `map` we defined for `LinkedList`, and also a `flatMap` method.

Given this list

~~~ scala
val list = List(1, 2, 3)
~~~

return a `List[Int]` containing both all the elements and their negation. Order is not important. Hint: Given an element create a list containing it and its negation.

<div class="solution">
~~~ scala
list.flatMap(x => List(x, -x))
~~~
</div>

Given this list

~~~ scala
val list = List(Full(3), Full(2), Full(1))
~~~

return a `List[Maybe[Int]]` containing `None` for the odd elements. Hint: If `x % 2 == 0` then `x` is even.

<div class="solution">
~~~ scala
list.map(maybe => maybe flatMap { x => if(x % 2 == 0) Full(x) else Empty })
~~~
</div>

### A Calculator, Again

We're going to return to the interpreter example we saw at the end of the last chapter. This time we're going to use the general abstractions we've created here, and our new knowledge of `map`, `flatMap`, and `fold`.

We're going to represent calculations as `Sum[String, Double]`, where the `String` is an error message. Last time we saw `Sum` we had this definition:

~~~ scala
sealed trait Sum[A, B] {
  def fold[C](left: A => C, right: B => C): C
}
final case class Left[A, B](value: A) extends Sum[A, B] {
  def fold[C](left: A => C, right: B => C): C =
    left(value)
}
final case class Right[A, B](value: B) extends Sum[A, B] {
  def fold[C](left: A => C, right: B => C): C =
    right(value)
}
~~~

Make the following changes to `Sum`:

- to prevent a name collision between the built-in `Either`, rename the `Left` and `Right` cases to `Failure` and `Success` respectively;
- use the covariant generic sum type pattern;
- reimplement `fold` using pattern matching.

<div class="solution">
This should be familiar by now. When using covariant pattern it is simpler to implement `fold` using pattern matching. With the covariant pattern we no longer have both generic type parameters in the case classes. The method signature of `fold` mentions both type parameters; to write it using polymorphism we would have to change one of these parameters to `Nothing` in each case.

~~~ scala
sealed trait Sum[+A, +B] {
  def fold[C](error: A => C, success: B => C): C =
    this match {
      case Failure(v) => error(v)
      case Success(v) => success(v)
    }
}
final case class Failure[A](value: A) extends Sum[A, Nothing]
final case class Success[B](value: B) extends Sum[Nothing, B]
~~~
</div>

Now things are going to get a bit trickier. We are going to implement `map` and `flatMap`, again using pattern matching in the `Sum` trait. Start with `map`. The general recipe for `map` is to start with a type like `F[A]` and apply a function `A => B` to get `F[B]`. `Sum` however has two generic type parameters. To make it fit the `F[A]` pattern we're going to fix one of these parameters and allow `map` to alter the other one. The natural choice is to fix the type parameter associated with `Failure` and allow `map` to alter a `Success`. This corresponds to "fail-fast" behaviour. If our `Sum` has failed, any sequenced computations don't get run.

In summary `map` should have type

~~~ scala
def map[C](f: B => C): Sum[A, C] =
~~~

<div class="solution">
~~~ scala
sealed trait Sum[+A, +B] {
  def fold[C](error: A => C, success: B => C): C =
    this match {
      case Failure(v) => error(v)
      case Success(v) => success(v)
    }
  def map[C](f: B => C): Sum[A, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => Success(f(v))
    }
}
~~~
</div>

Now implement `flatMap` using the same logic as `map`. The obvious implementation should lead to an error

~~~ scala
error: covariant type A occurs in contravariant position in type B => Sum[A,C] of value f
~~~

This takes some explaining. Remember that functions are contravariant in their input parameters and covariant in their result. In this case `A` appears in the result so it isn't in contravariant position in the function we pass to `flatMap`. However, `flatMap` is a method and methods are like functions in terms of contra- and covariance. So the function we pass to `flatMap` is in a contravariant position, and this leads to error message we see. The solution is introduce a new type called, say, `AA` along with a type bound `AA >: A`. That is, `flatMap` should have declaration

~~~ scala
def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C]
~~~

<div class="solution">
~~~ scala
sealed trait Sum[+A, +B] {
  def fold[C](failure: A => C, success: B => C): C =
    this match {
      case Failure(v) => failure(v)
      case Success(v) => success(v)
    }
  def map[C](f: B => C): Sum[A, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => Success(f(v))
    }
  def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => f(v)
    }
}
~~~
</div>

That was involved, but we've now seen an important pattern that you'll find throughout the Scala standard library.

Now we're going to reimplement that calculator from last time. We have an abstract syntax tree defined via the following algebraic data type:

~~~ scala
sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Double) extends Expression
~~~

Now implement a method `eval: Sum[String, Double]` on `Expression`. Use `flatMap` and `map` on `Sum` and introduce any utility methods you see fit to make the code more compact. Here are some test cases:

~~~ scala
assert(Addition(Number(1), Number(2)).eval == Success(3))
assert(SquareRoot(Number(-1)).eval == Failure("Square root of negative number"))
assert(Division(Number(4), Number(0)).eval == Failure("Division by zero"))
assert(Division(Addition(Subtraction(Number(8), Number(6)), Number(2)), Number(2)).eval == Success(2.0))
~~~

<div class="solution">
Here's my solution. I used a helper method `lift2` to "lift" a function into the result of two expressions. I hope you'll agree the code is both more compact and easier to read than our previous solution!

~~~ scala
sealed trait Expression {
  def eval: Sum[String, Double] =
    this match {
      case Addition(l, r) => lift2(l, r, (left, right) => Success(left + right))
      case Subtraction(l, r) => lift2(l, r, (left, right) => Success(left - right))
      case Division(l, r) => lift2(l, r, (left, right) =>
        if(right == 0)
          Failure("Division by zero")
        else
          Success(left / right)
      )
      case SquareRoot(v) =>
        v.eval flatMap { value =>
          if(value < 0)
            Failure("Square root of negative number")
          else
            Success(Math.sqrt(value))
        }
      case Number(v) => Success(v)
    }

  def lift2(l: Expression, r: Expression, f: (Double, Double) => Sum[String, Double]) =
    l.eval flatMap { left =>
      r.eval flatMap { right =>
        f(left, right)
      }
    }
}
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Int) extends Expression
~~~
</div>
