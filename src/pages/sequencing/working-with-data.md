---
layout: page
title: Working With Generic Data
---

We've seen that when we define a class with generic data, we cannot implement very many methods on that class. The user supplies the generic type, and thus we must ask the user to supply functions that work with that type. Nonetheless, there are some common patterns for using generic data, which is what we explore in this section. We have already seen **fold**. Here we will explore fold in more detail and also some other functions, namely **map** and **flatMap**, the are useful in many situations.


## Fold

Last time we saw fold we were working with a list of integers. Let's generalise to a list of a generic type. We're already see all the tools we need. First our data definition, in this instance slightly modified to use the covariant sum type pattern.

~~~ scala
sealed trait LinkedList[+A]
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
final case object Empty extends LinkedList[Nothing]
~~~

The last version of `fold` that we saw was

~~~ scala
def fold[A](list: IntList, f: (Int, A) => A, empty: A): A =
  list match {
    case Empty => empty
    case Cell(hd, tl) => f(hd, fold(tl, f, empty))
  }
~~~

It's reasonably straightforward to extend this to `LinkedList[A]`.

~~~ scala
def fold[A, B](list: LinkedList[A], f: (A, B) => B, empty: B): B =
  list match {
    case Empty => empty
    case Pair(hd, tl) => f(hd, fold(tl, f, empty))
  }
~~~

Fold is just an adaptation of structural recursion where we allow the user to pass in the functions we apply at each case. As structural recursion is the generic pattern for writing any function that transforms an algebraic datatype, fold is the concrete realisation of this generic pattern. That is, fold is the generic transformation or iteration method. Any function you care to write on an algebraic datatype can be written in terms of fold.

<div class="callout callout-info">
#### Fold Pattern

For an algebraic datatype `A`, fold converts it to a generic type `B`. Fold is a structural recursion with:

- one function parameter for each class in `A`;
- each function takes as parameters the fields for its associated class;
- if `A` is recursive, any function parameters that refer to a recursive field take a parameter of type `B`.
</div>

Let's apply the pattern to derive the `fold` method above. We start with our basic template:

~~~ scala
def fold[A, B](list: LinkedList[A]): B =
  list match {
    case Empty => ???
    case Pair(hd, tl) => ???
  }
~~~

This is just the structural recursion template with the addition of a generic type parameter for the return type.

Now we add one function for each of the two classes in `LinkedList`.

~~~ scala
def fold[A, B](list: LinkedList[A], pair: ???, ): B =
  list match {
    case Empty => ???
    case Pair(hd, tl) => ???
  }
~~~
