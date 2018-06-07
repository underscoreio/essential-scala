## Functions

Functions allow us to *abstract over methods*, turning methods into values that we can pass around and manipulate within our programs.

Let's look at three methods we wrote that manipulate `IntList`.

```tut:book:silent
object wrapper {
  sealed trait IntList {
    def length: Int =
      this match {
        case End => 0
        case Pair(hd, tl) => 1 + tl.length
      }
    def double: IntList =
      this match {
        case End => End
        case Pair(hd, tl) => Pair(hd * 2, tl.double)
      }
    def product: Int =
      this match {
        case End => 1
        case Pair(hd, tl) => hd * tl.product
      }
    def sum: Int =
      this match {
        case End => 0
        case Pair(hd, tl) => hd + tl.sum
      }
  }

  case object End extends IntList
  case class Pair(hd: Int, tl: IntList) extends IntList
}; import wrapper._
```

All of these methods have the same general pattern, which is not surprising as they all use structural recursion. It would be nice to be able to remove the duplication.

Let's start by focusing on the methods that return an `Int`: `length`, `product`, and `sum`.
We want to write a method like

```scala
def abstraction(end: Int, f: ???): Int =
  this match {
    case End => end
    case Pair(hd, tl) => f(hd, tl.abstraction(end, f))
  }
```

I've used `f` to denote some kind of object that does the combination of the head and recursive call for the `Pair` case. At the moment we don't know how to write down the type of this value, or how to construct one. However, we can guess from the title of this section that what we want is a function!

A function is like a method: we can call it with parameters and it evaluates to a result. Unlike a method a function is value. We can pass a function to a method or to another function. We can return a function from a method, and so on.

Much earlier in this course we introduced the `apply` method, which lets us treat objects as functions in a syntactic sense:

```tut:book:silent
object add1 {
  def apply(in: Int) = in + 1
}
```

```tut:book
add1(2)
```

This is a big step towards doing real functional programming in Scala but we're missing one important component: *types*.

As we have seen, types allow us to abstract across values. We've seen special case functions like `Adders`, but what we really want is a generalised set of types that allow us to represent computations of any kind.

Enter Scala's `Function` types.

### Function Types

We write a function type like `(A, B) => C` where `A` and `B` are the types of the parameters and `C` is the result type. The same pattern generalises from functions of no arguments to an arbitrary number of arguments.

In our example above we want `f` to be a function that accepts two `Int`s as parameters and returns an `Int`. Thus we can write it as `(Int, Int) => Int`.

<div class="callout callout-info">
#### Function Type Declaration Syntax {-}

To declare a function type, write

```scala
(A, B, ...) => C
```

where

- `A, B, ...` are the types of the input parameters; and
- `C` is the type of the result.

If a function only has one parameter the parentheses may be dropped:

```scala
A => B
```
</div>


### Function literals

Scala also gives us a *function literal syntax* specifically for creating new functions. Here are some example function literals:

```tut:book
val sayHi = () => "Hi!"

sayHi()

val add1 = (x: Int) => x + 1

add1(10)

val sum = (x: Int, y:Int) => x + y

sum(10, 20)
```

In code where we know the argument types, we can sometimes *drop the type annotations* and allow Scala to infer them[^parens]. There is no syntax for declaring the result type of
a function and it is normally inferred, but if we find ourselves needing to do this we can put a type on the function's body expression:

```tut:book:silent
(x: Int) => (x + 1): Int
```

[^parens]: Note that we only can drop the parentheses around the argument list on single-argument functions---we still have to write `() => foo` and `(a, b) => foo` on functions of other arities.

<div class="callout callout-info">
#### Function Literal Syntax {-}

The syntax for declaring a function literal is

```scala
(parameter: type, ...) => expression
```

where
- the optional `parameter`s are the names given to the function parameters;
- the `type`s are the types of the function parameters; and
- the `expression` determines the result of the function.
</div>

### Exercises

#### A Better Abstraction

We started developing an abstraction over `sum`, `length`, and `product` which we sketched out as

```scala
def abstraction(end: Int, f: ???): Int =
  this match {
    case End => end
    case Pair(hd, tl) => f(hd, tl.abstraction(end, f))
  }
```

Rename this function to `fold`, which is the name it is usually known as, and finish the implementation.

<div class="solution">
Your `fold` method should look like this:

```tut:book:silent
object wrapper {
  sealed trait IntList {
    def fold(end: Int, f: (Int, Int) => Int): Int =
      this match {
        case End => end
        case Pair(hd, tl) => f(hd, tl.fold(end, f))
      }

    // other methods...
  }
  case object End extends IntList
  final case class Pair(head: Int, tail: IntList) extends IntList
}; import wrapper._
```
</div>

Now reimplement `sum`, `length`, and `product` in terms of `fold`.

<div class="solution">
```tut:book:silent
object wrapper {
  sealed trait IntList {
    def fold(end: Int, f: (Int, Int) => Int): Int =
      this match {
        case End => end
        case Pair(hd, tl) => f(hd, tl.fold(end, f))
      }
    def length: Int =
      fold(0, (_, tl) => 1 + tl)
    def product: Int =
      fold(1, (hd, tl) => hd * tl)
    def sum: Int =
      fold(0, (hd, tl) => hd + tl)
  }
  case object End extends IntList
  final case class Pair(head: Int, tail: IntList) extends IntList
}; import wrapper._
```
</div>

Is it more convenient to rewrite methods in terms of `fold` if they were implemented using pattern matching or polymorphic? What does this tell us about the best use of `fold`?

<div class="solution">
When using `fold` in polymorphic implementations we have a lot of duplication; the polymorphic implementations without `fold` were simpler to write. The pattern matching implementations benefitted from `fold` as we removed the duplication in the pattern matching.

In general `fold` makes a good interface for users *outside* the class, but not necessarily for use *inside* the class.
</div>

Why can't we write our `double` method in terms of `fold`? Is it feasible we could if we made some change to `fold`?

<div class="solution">
The types tell us it won't work. `fold` returns an `Int` and `double` returns an `IntList`. However the general structure of `double` is captured by `fold`. This is apparent if we look at them side-by-side:

```scala
def double: IntList =
  this match {
    case End => End
    case Pair(hd, tl) => Pair(hd * 2, tl.double)
  }

def fold(end: Int, f: (Int, Int) => Int): Int =
  this match {
    case End => end
    case Pair(hd, tl) => f(hd, tl.fold(end, f))
  }
```

If we could generalise the types of `fold` from `Int` to some general type then we could write `double`. And that, dear reader, is what we turn to next.
</div>

Implement a generalised version of `fold` and rewrite `double` in terms of it.

<div class="solution">
We want to generalise the return type of `fold`. Our starting point is

```scala
def fold(end: Int, f: (Int, Int) => Int): Int
```

Replacing the return type and tracing it back we arrive at

```scala
def fold[A](end: A, f: (Int, A) => A): A
```

where we've used a generic type on the method to capture the changing return type. With this we can implement `double`. When we try to do so we'll see that type inference fails, so we have to give it a bit of help.

```tut:book:silent
object wrapper {
  sealed trait IntList {
    def fold[A](end: A, f: (Int, A) => A): A =
      this match {
        case End => end
        case Pair(hd, tl) => f(hd, tl.fold(end, f))
      }
    def length: Int =
      fold[Int](0, (_, tl) => 1 + tl)
    def product: Int =
      fold[Int](1, (hd, tl) => hd * tl)
    def sum: Int =
      fold[Int](0, (hd, tl) => hd + tl)
    def double: IntList =
      fold[IntList](End, (hd, tl) => Pair(hd * 2, tl))
  }
  case object End extends IntList
  final case class Pair(head: Int, tail: IntList) extends IntList
}; import wrapper._
```
</div>
