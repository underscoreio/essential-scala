---
layout: page
title: Structural Recursion
---

In the previous section we worked on some examples using higher-order functions. You will recall the remarkable regularity in the function definitions. This regularity is not an accident! This idea is called **structural recursion** and it gives us a formula for constructing functions that always work correctly with a particular type of data known as **algebraic data types**.

## Algebraic Data Types

An algebraic data type, in Scala, is any data defined using sealed traits and final case classes. The important properties are:

* it consists of sum types (choices between types) and product types (types holding data)
* it has a fixed number of members (hence the use of sealed and final).

An example is the built-in `List`. A `List[A]` is either:

* the empty list `Nil`
* the pair `::[A](hd: A, tl: List[A])`

This is an algebraic data type: have a sum-type (`List`), a product type (`::`), and a finite number of cases.

By the way, if you're wondering how an expression like `1 :: Nil` works, `::` is also a method on `List` which constructs a `::` object. We can construct a `::` directly by putting the name in back-ticks like so `` `::`(1, Nil) ``.

## Folding at Home

With an algebraic data type we can write a general traversal function, but convention called **fold**, with which we can express any iteration on our data type.

We saw this with `List` when we implemented `foldRight`, the code for which is

~~~ scala
def foldRight[A, B](f: (A, B) => B, zero: B, in: List[A]): B =
  in match {
    case Nil => zero
    case (x :: xs) => f(x,  foldRight(f, zero, xs))
  }
~~~

Remember that a list is constructed as `1 :: 2 :: Nil` or equivalently `` `::`(1, `::`(2, Nil)) ``. Notice that the definition of `foldRight` exactly matches this, replacing `Nil` with `zero` and `` `::` `` with `f`. So to add all the elements of a list we calculate `+(1, +(2, 0))` (where I've written `+` using prefix instead of infix style), to calculate the product `*(1, *(2, 1))`, and so on.

The general formula for a `fold` function is:

* `fold` takes a function for each case in the algebraic data type. If a case has no instance variable (i.e. is a case object) we can take a constant value instead. This is what we do in `foldRight` above where we take a constant for `Nil`.
* For each product type the function should accept as many values as there are in the particular type. If the value is recursive, as the second value of `::` is, the value should be obtained by recursing on `fold`.

## Exercise

A binary tree can be defined as follows:

~~~ scala
sealed trait Tree[A]
final case class Node[A](val l: Tree[A], val r: Tree[A]) extends Tree[A]
final case class Leaf[A](val elt: A) extends Tree[A]
~~~

Write a `fold` for `Tree`.

<div class="solution">
~~~ scala
def fold[A, B](node: (B, B) => B, leaf: A => B, tree: Tree[A]): B =
  tree match {
    case Node(l, r) => node(fold(node, leaf, l), fold(node, leaf, r))
    case Leaf(elt)  => leaf(elt)
  }
~~~
</div>

Use your `fold` function to calculate the sum and product of the tree `Node(Leaf(1), Node(Node(Leaf(2), Leaf(3)), Leaf(4)))`

<div class="solution">
~~~ scala
scala> val tree = Node(Leaf(1), Node(Node(Leaf(2), Leaf(3)), Leaf(4)))
tree: Node[Int] = Node(Leaf(1),Node(Node(Leaf(2),Leaf(3)),Leaf(4)))

scala> val sum = fold((l: Int, r: Int) => l + r, (x: Int) => x, tree)
sum: Int = 10

scala> val product = fold((l: Int, r: Int) => l * r, (x: Int) => x, tree)
product: Int = 24
~~~
</div>

Using your `fold`, write `map`.

<div class="solution">
~~~ scala
def map[A, B](f: A => B, tree: Tree[A]): Tree[B] =
  fold(Node.apply[B] _, (elt: A) => Leaf(f(elt)), tree)
~~~
</div>

## Folding vs Pattern Matching

We now have two ways to dealing with algebraic data types: pattern matching or higher-order functions such as `fold` and `map`. Which should we prefer? For most people just starting out pattern matching, being more concrete, is probably easier to deal with. Pattern matching will also normally be a bit faster. However experienced Scala programmers tend to prefer higher order functions. As they are more abstract they are a bit more resilient to change. Recall for-comprehensions from the section on collections. They work with any type implementing `map`, `flatMap`, and `foreach`. As we have seen, lots of data types implement these methods. Code written to this interface can change its underlying representation without requiring wholesale code changes. This is not the case with pattern matching as we're matching on the concrete types in the implementation.
