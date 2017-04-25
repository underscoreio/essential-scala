## Sequencing Computation

We have now mastered generic data and folding over algebraic data types. Now we will look as some other common patterns of computation that are 1) often more convenient to use than fold for algebraic data types and 2) can be implemented for certain types of data that do not support a fold. These methods are known as *map* and *flatMap*.

### Map

Imagine we have a list of `Int` user IDs, and a function which, given a user ID, returns a `User` record. We want to get a list of user records for all the IDs in the list. Written as types we have `List[Int]` and a function `Int => User`, and we want to get a `List[User]`.

Imagine we have an optional value representing a user record loaded from the database and a function that will load their most recent order. If we have a record we want to then lookup the user's most recent order. That is, we have a `Maybe[User]` and a function `User => Order`, and we want a `Maybe[Order]`.

Imagine we have a sum type representing an error message or a completed order. If we have a completed order we want to get the total value of the order. That is, we have a `Sum[String, Order]` and a function `Order => Double`, and we want `Sum[String, Double]`.

What these all have in common is we have a type `F[A]` and a function `A => B`, and we want a result `F[B]`. The method that performs this operation is called `map`.

Let's implement `map` for `LinkedList`. We start by outlining the types and adding the general structural recursion skeleton:

```tut:book:silent
object solution {
  sealed trait LinkedList[A] {
    def map[B](fn: A => B): LinkedList[B] =
      this match {
        case Pair(hd, tl) => ???
        case End() => ???
      }
  }
  final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
  final case class End[A]() extends LinkedList[A]
}
```

We know we can use the structural recursion pattern as we know that `fold` (which is just the structural recursion pattern abstracted) is the universal iterator for an algebraic data type. Thus:

- For `Pair` we have to combine `head` and `tail` to return a `LinkedList[B]` (as the types tell us) and we also know we need to recurse on `tail`. We can write

```scala
case Pair(hd, tl) => {
  val newTail: LinkedList[B] = tail.map(fn)
  // Combine newTail and head to create LinkedList[B]
}
```

We can convert `head` to a `B` using `fn`, and then build a larger list from `newTail` and our `B` giving us the final solution

```scala
case Pair(hd, tl) => Pair(fn(hd), tl.map(fn))
```

- For `End` we don't have any value of `A` to apply to the function. The only thing we can return is an `End`.

Therefore the complete solution is

```tut:book:silent
object solution {
  sealed trait LinkedList[A] {
    def map[B](fn: A => B): LinkedList[B] =
      this match {
        case Pair(hd, tl) => Pair(fn(hd), tl.map(fn))
        case End() => End[B]()
      }
  }
  case class Pair[A](hd: A, tl: LinkedList[A]) extends LinkedList[A]
  case class End[A]() extends LinkedList[A]
}
```

Notice how using the types and patterns guided us to a solution.

### FlatMap

Now imagine the following examples:

- We have a list of users and we want to get a list of all their orders. That is, we have `LinkedList[User]` and a function `User => LinkedList[Order]`, and we want `LinkedList[Order]`.

- We have an optional value representing a user loaded from the database, and we want to lookup their most recent order---another optional value. That is, we have `Maybe[User]` and `User => Maybe[Order]`, and we want `Maybe[Order]`.

- We have a sum type holding an error message or an `Order`, and we want to email an invoice to the user. Emailing returns either an error message or a message ID. That is, we have `Sum[String, Order]` and a function `Order => Sum[String, Id]`, and we want `Sum[String, Id]`.

What these all have in common is we have a type `F[A]` and a function `A => F[B]`, and we want a result `F[B]`. The method that performs this operation is called `flatMap`.

Let's implement `flatMap` for `Maybe` (we need an append method to implement `flatMap` for `LinkedList`). We start by outlining the types:

```tut:book:silent
sealed trait Maybe[A] {
  def flatMap[B](fn: A => Maybe[B]): Maybe[B] = ???
}
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]
```

We use the same pattern as before: it's a structural recursion and our types guide us in filling in the method bodies.

```tut:book:silent
object solution {
  sealed trait Maybe[A] {
    def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
      this match {
        case Full(v) => fn(v)
        case Empty() => Empty[B]()
      }
  }
  final case class Full[A](value: A) extends Maybe[A]
  final case class Empty[A]() extends Maybe[A]
}
```

### Functors and Monads

```tut:reset:invisible:silent
object solution {
  sealed trait Maybe[A] {
    def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
      this match {
        case Full(v) => fn(v)
        case Empty() => Empty[B]()
      }
  }
  final case class Full[A](value: A) extends Maybe[A]
  final case class Empty[A]() extends Maybe[A]
}
import solution._
```

A type like `F[A]` with a `map` method is called a *functor*. If a functor also has a `flatMap` method it is called a *monad*[^monads].

[^monads:] There is a little bit more to being a functor or monad.  For a monad we require a constructor, typically called `point`, and there are some algebraic laws that our `map` and `flatMap` operations must obey. A quick search online will find more information on monads, or they are covered in more detail in our "Advanced Scala" book.

Although the most immediate applications of `map` and `flatMap` are in collection classes like lists, the bigger picture is sequencing computations. Imagine we have a number of computations that can fail. For instance

```tut:book:silent
def mightFail1: Maybe[Int] =
  Full(1)

def mightFail2: Maybe[Int] =
  Full(2)

def mightFail3: Maybe[Int] =
  Empty() // This one failed
```

We want to run these computations one after another. If any one of them fails the whole computation fails. Otherwise we'll add up all the numbers we get. We can do this with `flatMap` as follows.

```tut:book:silent
mightFail1 flatMap { x =>
  mightFail2 flatMap { y =>
    mightFail3 flatMap { z =>
      Full(x + y + z)
    }
  }
}
```

The result of this is `Empty`. If we drop `mightFail3`, leaving just

```tut:book:silent
mightFail1 flatMap { x =>
  mightFail2 flatMap { y =>
    Full(x + y)
  }
}
```

the computation succeeds and we get `Full(3)`.

The general idea is a monad represents a value in some context. The context depends on the monad we're using. We've seen examples where the context is:

- an optional value, such as we might get when retrieving a value from a database;
- an sum of values, which might represent a error message and a value we're computing with; and
- a list of values.

We use `map` when we want to transform the value within the context to a new value, while keeping the context the same. We use `flatMap` when we want to transform the value *and* provide a new context.

### Exercises

#### Mapping Lists

Given the following list

```tut:invisible
object solution {
  sealed trait LinkedList[A] {
    def map[B](fn: A => B): LinkedList[B] =
      this match {
        case Pair(hd, tl) => Pair(fn(hd), tl.map(fn))
        case End() => End[B]()
      }
  }
  case class Pair[A](hd: A, tl: LinkedList[A]) extends LinkedList[A]
  case class End[A]() extends LinkedList[A]
}
import solution._
```

```tut:book:silent
val list: LinkedList[Int] = Pair(1, Pair(2, Pair(3, End())))
```

- double all the elements in the list;
- add one to all the elements in the list; and
- divide by three all the elements in the list.

<div class="solution">
These exercises just get you used to using `map`.

```tut:book:silent
list.map(_ * 2)
list.map(_ + 1)
list.map(_ / 3)
```
</div>

#### Mapping Maybe

Implement `map` for `Maybe`.

<div class="solution">
```tut:book:silent
object solution {
  sealed trait Maybe[A] {
    def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
      this match {
        case Full(v) => fn(v)
        case Empty() => Empty[B]()
      }
    def map[B](fn: A => B): Maybe[B] =
      this match {
        case Full(v) => Full(fn(v))
        case Empty() => Empty[B]()
      }
  }
  final case class Full[A](value: A) extends Maybe[A]
  final case class Empty[A]() extends Maybe[A]
}
```
</div>

For bonus points, implement `map` in terms of `flatMap`.

<div class="solution">
```tut:book:silent
object solution {
  sealed trait Maybe[A] {
    def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
      this match {
        case Full(v) => fn(v)
        case Empty() => Empty[B]()
      }
    def map[B](fn: A => B): Maybe[B] =
      flatMap[B](v => Full(fn(v)))
  }
  final case class Full[A](value: A) extends Maybe[A]
  final case class Empty[A]() extends Maybe[A]
}
```
</div>


#### Sequencing Computations

```tut:reset:invisible
object solution {
  sealed trait Maybe[A] {
    def flatMap[B](fn: A => Maybe[B]): Maybe[B] =
      this match {
        case Full(v) => fn(v)
        case Empty() => Empty[B]()
      }
    def map[B](fn: A => B): Maybe[B] =
      flatMap[B](v => Full(fn(v)))
  }
  final case class Full[A](value: A) extends Maybe[A]
  final case class Empty[A]() extends Maybe[A]
}
import solution._
```

We're going to use Scala's builtin `List` class for this exercise as it has a `flatMap` method.

Given this list

```tut:book:silent
val list = List(1, 2, 3)
```

return a `List[Int]` containing both all the elements and their negation. Order is not important. Hint: Given an element create a list containing it and its negation.

<div class="solution">
```tut:book:silent
list.flatMap(x => List(x, -x))
```
</div>

Given this list

```tut:book:silent
val list: List[Maybe[Int]] = List(Full(3), Full(2), Full(1))
```

return a `List[Maybe[Int]]` containing `None` for the odd elements. Hint: If `x % 2 == 0` then `x` is even.

<div class="solution">
```tut:book:silent
list.map(maybe => maybe.flatMap[Int] { x => if (x % 2 == 0) Full(x) else Empty() })
```
</div>

#### Sum

Recall our `Sum` type.

```tut:book:silent
object solution {
  sealed trait Sum[A, B] {
    def fold[C](left: A => C, right: B => C): C =
      this match {
        case Left(a) => left(a)
        case Right(b) => right(b)
      }
  }
  final case class Left[A, B](value: A) extends Sum[A, B]
  final case class Right[A, B](value: B) extends Sum[A, B]
}
```

To prevent a name collision between the built-in `Either`, rename the `Left` and `Right` cases to `Failure` and `Success` respectively.

<div class="solution">
```tut:book:silent
object solution {
  sealed trait Sum[A, B] {
    def fold[C](error: A => C, success: B => C): C =
      this match {
        case Failure(v) => error(v)
        case Success(v) => success(v)
      }
  }
  final case class Failure[A, B](value: A) extends Sum[A, B]
  final case class Success[A, B](value: B) extends Sum[A, B]
}
```
</div>

Now things are going to get a bit trickier. We are going to implement `map` and `flatMap`, again using pattern matching in the `Sum` trait. Start with `map`. The general recipe for `map` is to start with a type like `F[A]` and apply a function `A => B` to get `F[B]`. `Sum` however has two generic type parameters. To make it fit the `F[A]` pattern we're going to fix one of these parameters and allow `map` to alter the other one. The natural choice is to fix the type parameter associated with `Failure` and allow `map` to alter a `Success`. This corresponds to "fail-fast" behaviour. If our `Sum` has failed, any sequenced computations don't get run.

In summary `map` should have type

```scala
def map[C](f: B => C): Sum[A, C]
```

<div class="solution">
```tut:book:silent
object solution {
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
  final case class Failure[A, B](value: A) extends Sum[A, B]
  final case class Success[A, B](value: B) extends Sum[A, B]
}
```
</div>

Now implement `flatMap` using the same logic as `map`.

<div class="solution">
```tut:book:silent
object solution {
  sealed trait Sum[A, B] {
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
    def flatMap[C](f: B => Sum[A, C]) =
      this match {
        case Failure(v) => Failure(v)
        case Success(v) => f(v)
      }
  }
  final case class Failure[A, B](value: A) extends Sum[A, B]
  final case class Success[A, B](value: B) extends Sum[A, B]
}
```
</div>
