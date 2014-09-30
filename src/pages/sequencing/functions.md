---
layout: page
title: Functions
---

Functions allow us to **abstract over methods**, turning methods into values that we can pass around and manipulate within our programs.

Let's look at three methods we wrote that manipulate `IntList`. I have chosen the pattern matching implementations as it is easier to see the duplication in these examples.

~~~ scala
def sum(list: IntList): Int =
  list match {
    case End => 0
    case Pair(hd, tl) => hd + sum(tl)
  }

def length(list: IntList): Int =
  list match {
    case End => 0
    case Pair(hd, tl) => 1 + length(tl)
  }

def product(list: IntList): Int =
  list match {
    case End => 1
    case Pair(hd, tl) => hd * product(tl)
  }
~~~

All of these methods:

- accept an `IntList` as a parameter;
- return an `Int`;
- use the same patterns;
- return an `Int` for the `End` case;
- have the same general shape for the `Pair` case: the head of the list is combined with the result of a recursive call.

How can we remove this duplication? We want to write a method like

~~~ scala
def abstraction(list: IntList, f: ???, end: Int): Int =
  list match {
    case End => end
    case Pair(hd, tl) => f(hd, abstraction(tl, f, end))
  }
~~~

I've used `f` to denote some kind of object that does the combination of the head and recursive call for the `Pair` case. At the moment we don't know how to write down the type of this value, or how to construct one. However, we can guess from the title of this section that what we want is a function!

A function is like a method: we can call it with parameters and it evaluates to a result. Unlike a method a function is value. We can pass a function to a method or to another function. We can return a function from a method, and so on.

Much earlier in this course we introduced the `apply` method, which lets us treat objects as functions in a syntactic sense:

~~~ scala
scala> object add1 {
         def apply(in: Int) = in + 1
       }
defined module add1

scala> add1(2)
res2: Int = 3
~~~

This is a big step towards doing real functional programming in Scala but we're missing one important component: *types*.

As we have seen, types allow us to abstract across values. We've seen all sorts of special case functions like `Adders` and `ActionListeners`, but what we really want is a generalised set of types that allow us to represent computations of any kind.

Enter Scala's `Function` types.

## Function Types

We write a function type like `(A, B) => C` where `A` and `B` are the types of the parameters and `C` is the result type. The same pattern generalises from function of no arguments to an arbitrary number of arguments.

In our example above we want `f` to be a function that accepts two `Int`s as parameters and returns an `Int`. Thus we can write it as `(Int, Int) => Int`.

<div class="callout callout-info">
#### Function Type Declaration Syntax

To declare a function type, write

~~~ scala
(A, B, ...) => C
~~~

where

- `A, B, ...` are the types of the input parameters; and
- `C` is the type of the result.

If a function only has one parameter the parentheses may be dropped:

~~~ scala
A => B
~~~
</div>


## Function literals

Scala also gives us a **function literal syntax** specifically for creating new functions. Here are some example function literals:

~~~ scala
scala> val sayHi = () => "Hi!"
sayHi: () => String = <function0>

scala> sayHi()
res1: String = Hi!

scala> val add1 = (x: Int) => x + 1
add1: Int => Int = <function1>

scala> add1(10)
res2: Int = 11

scala> val sum = (x: Int, y:Int) => x + y
sum: (Int, Int) => Int = <function2>

scala> sum(10, 20)
res3: Int = 30
~~~

In code where we know the argument types, we can sometimes **drop the type annotations** and allow Scala to infer them[^parens]. There is no syntax for declaring the result type of
a function and it is normally inferred, but if we find ourselves need to do this we can put a type on the function's body expression:

~~~ scala
(x: Int) => (x + 1): Int
~~~

[^parens]: Note that we only can drop the parentheses around the argument list on single-argument functions -- we still have to write `() => foo` and `(a, b) => foo` on functions of other arities.

<div class="callout callout-info">
#### Function Literal Syntax

The syntax for declaring a function literal is

~~~ scala
(parameter: type, ...) => expression
~~~

where
- the optional `parameter`s are the names given to the function parameters;
- the `type`s are the types of the function parameters; and
- the `expression` determines the result of the function.
</div>

### Placeholder syntax

In very simple situations we can write inline functions using an extreme shorthand called **placeholder syntax**. It looks like this:

~~~ scala
scala> ((_: Int) * 2)
res23: Int => Int = <function1>
~~~

`(_: Int) * 2` is expanded by the compiler to `(a: Int) => a * 2`. It is more idiomatic to use the placeholder syntax only in the cases where the compiler can infer the types. Here are a few more examples:

~~~ scala
_ + _     // expands to `(a, b) => a + b`
foo(_)    // expands to `(a) => foo(a)`
foo(_, b) // expands to `(a) => foo(a, b)`
_(foo)    // expands to `(a) => a(foo)`
// and so on...
~~~

Placeholder syntax, while wonderfully terse, can be confusing for large expressions and should only be used for very small functions.

### Converting methods to functions

Scala contains one final feature that is directly relevant to this section -- the ability to convert method calls to functions. This is closely related to placeholder syntax -- simply follow a method with an underscore:

~~~ scala
scala> object Sum {
         def sum(x: Int, y: Int) = x + y
       }
defined module Sum

scala> Sum.sum
<console>:9: error: missing arguments for method sum in object Sum;
follow this method with `_' if you want to treat it as a partially applied function
              Sum.sum
                  ^

scala> (Sum.sum _)
res11: (Int, Int) => Int = <function2>
~~~

In situations where Scala can infer that we need a function, we can even drop the underscore and simply write the method name -- the compiler will promote the method to a function automatically:

~~~ scala
scala> object MathStuff {
         def add1(num: Int) = num + 1
       }
defined module MathStuff

scala> Counter(2).adjust(MathStuff.add1)
res12: Counter = Counter(3)
~~~

## Exercises

#### A Better Abstraction

We started developing an abstraction over `sum`, `length`, and `product` which we sketched out as

~~~ scala
def abstraction(list: IntList, f: ???, end: Int): Int =
  list match {
    case End => end
    case Pair(hd, tl) => f(hd, abstraction(tl, f, end))
  }
~~~

Rename this function to `fold`, which is the name it is usually known as, and finish the implementation.

<div class="solution">
~~~ scala
def fold(list: IntList, f: (Int, Int) => Int, end: Int): Int =
  list match {
    case End => end
    case Pair(hd, tl) => f(hd, fold(tl, f, end))
  }
~~~
</div>

Now reimplement the pattern matching variants of `sum`, `length`, and `product` in terms of `fold`.

<div class="solution">
~~~ scala
def sum(list: IntList): Int =
  fold(list, _ + _, 0)

def length(list: IntList): Int =
  fold(list, (hd, tl) => 1 + tl, 0)

def product(list: IntList): Int =
  fold(list, _ * _, 1)
~~~
</div>

Now implement `fold` using polymorphism and similarly reimplement the polymorphic versions of `sum`, `length`, and `product` in terms of the polymorphic `fold`.

<div class="solution">
~~~ scala
sealed trait IntList {
  def fold(f: (Int, Int) => Int, end: Int): Int
  def double: IntList
  def product: Int
  def sum: Int
  def length: Int
}
final case object End extends IntList {
  def fold(f: (Int, Int) => Int, end: Int) =
    end
  def double: IntList =
    End
  def product: Int =
    fold(_ * _, 1)
  def sum: Int =
    fold(_ + _, 0)
  def length: Int =
    fold((hd, tl) => 1 + tl, 0)
}
final case class Pair(head: Int, tail: IntList) extends IntList {
  def fold(f: (Int, Int) => Int, end: Int) =
    f(head, tail.fold(f, end))
  def double: IntList =
    Pair(head * 2, tail.double)
  def product: Int =
    fold(_ * _, 1)
  def sum: Int =
    fold(_ + _, 0)
  def length: Int =
    fold((hd, tl) => 1 + tl, 0)
}
~~~
</div>

Is `fold` more convenient to use in pattern matching or polymorpic situations? What does this tell us about the best use of `fold`?

<div class="solution">
When using `fold` in polymorphic implementations we have a lot of duplication; the polymorphic implementations without `fold` were simpler to write. The polymorpic implementations benefited from `fold` as we remove the duplication in the pattern matching.

In general `fold` makes a good interface for users *outside* the class, but not necessarily for use *inside* the class.
</div>

Why can't write `double` in terms of `fold`. Why not? Is it feasible we could if we made some change to `fold`?

<div class="solution">
The types tell us it won't work. `fold` returns an `Int` and `double` returns an `IntList`. However the general structure of `double` is captured by `fold`. This is apparent if we look at them side-by-side:

~~~ scala
def double(list: IntList): IntList =
  list match {
    case End => End
    case Pair(hd, tl) => Pair(hd * 2, double(tl))
  }

def fold(list: IntList, f: (Int, Int) => Int, end: Int): Int =
  list match {
    case End => end
    case Pair(hd, tl) => f(hd, fold(tl, f, end))
  }
~~~

If we could generalise the types of `fold` from `Int` to some general type then we could write `double`. And that, dear reader, is what we turn to next.
</div>

Implement a generalised version of `fold` and rewrite `double` in terms of it.

<div class="solution">
We want to generalise the return type of `fold`. Our starting point is

~~~ scala
def fold(list: IntList, f: (Int, Int) => Int, end: Int): Int
~~~

Replacing the return type and tracing it back we arrive at

~~~ scala
def fold[A](list: IntList, f: (Int, A) => A, end: A): A
~~~

where we've used a generic type on the method to capture the variable return type. With this we can implement `double`. When we try to do so we'll see that type inference fails, so we have to give it a bit of help.

~~~ scala
def fold[A](list: IntList, f: (Int, A) => A, end: A): A =
  list match {
    case End => end
    case Pair(hd, tl) => f(hd, fold(tl, f, end))
  }

def double(list: IntList): IntList =
  fold[IntList](list, (hd, tl) => Pair(hd * 2, tl), End)
~~~
</div>
