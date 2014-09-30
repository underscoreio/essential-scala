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
    case Pair(hd, tl) => f(hd, fold(tl, f, empty))
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

The right-hand side of pattern matching cases, or the polymorphic methods as appropriate, consists of calls to the appropriate function.
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
def fold[A, B](list: LinkedList[A], pair: ???, empty: ???): B =
  list match {
    case Empty => ???
    case Pair(hd, tl) => ???
  }
~~~

From the rules for the function types:

- `empty` has no parameters (as `Empty` stores no values) and returns `B`. Thus its type is `() => B`, which we can optimise to just a value of type `B`; and
- `pair` has two parameters, one for the list head and one for the tail. The argument for the head has type `A`, and the tail is recursive call and thus has type `B`. The final type is therefore `(A, B) => B`.

Substituting in we get

~~~ scala
def fold[A, B](list: LinkedList[A], pair: (A, B) => B, empty: B): B =
  list match {
    case Empty => empty
    case Pair(hd, tl) => pair(hd, fold(tl, pair, empty))
  }
~~~

## Exercises

#### Folding Maybe

In the last section we implemented a sum type for modelling optional data:

~~~ scala
sealed trait Maybe[+A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]
~~~

Implement fold for this type.

<div class="solution">
The code is very similar to the implementation for `LinkedList`. I choose polymorphism for my solution. I belive it's more idiomatic and it provides an example to contrast with the pattern matching solution for `LinkedList`.

~~~ scala
sealed trait Maybe[+A] {
  def fold[B](full: A => B, empty: B): B
}
final case class Full[A](value: A) extends Maybe[A] {
  def fold[B](full: A => B, empty: B): B =
    full(value)
}
final case object Empty extends Maybe[Nothing] {
  def fold[B](full: A => B, empty: B): B =
    empty
}
~~~
</div>

#### Folding Sum

In the previous section we implemented a generic sum type:

~~~ scala
sealed trait Sum[A, B]
final case class Left[A, B](value: A) extends Sum[A, B]
final case class Right[A, B](value: B) extends Sum[A, B]
~~~

Implement `fold` for `Sum`.

<div class="solution">
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
</div>


#### Tree

A binary tree can be defined as follows:

A `Tree` of type `A` is a `Node` with a left and right `Tree` or a `Leaf` with an element of type `A`.

Implement this algebraic data type along with a fold method.

<div class="solution">
This is a tricker example than the previous two exercises as we have a recursive data type. Follow the patterns and you should be ok.

~~~ scala
sealed trait Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B
}
final case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B =
    node(left.fold(node, leaf), right.fold(node, leaf))
}
final case class Leaf[A](value: A) extends Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B =
    leaf(value)
}
~~~
</div>

Using `fold` convert the following `Tree` to a `String`

~~~ scala
val tree: Tree[String] =
  Node(Node(Leaf("To"), Leaf("iterate")),
       Node(Node(Leaf("is"), Leaf("human,")),
            Node(Leaf("to"), Node(Leaf("recurse"), Leaf("divine")))))
~~~

Remeber you can append `String`s using the `+` method.

<div class="solution">
Note it is necessary to instantiate the generic type variable for `fold`. Type inference fails in this case.

~~~ scala
tree.fold[String]((a, b) => a + " " + b, str => str)
~~~
</div>
