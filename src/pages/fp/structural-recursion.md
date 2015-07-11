---
layout: page
title: Structural Recursion
---

In the previous sections of the course two themes have come up again and again:

- modelling data using traits and type classes; and
- operating on data using pattern matching and higher-order functions like `map` and `fold`.

You might have noticed that the way we define data and the way we use it follows a very regular pattern. This is no accident. The concept underlying this is called **structural recursion**. Structural recursion gives us a set of rules---we could implement it as a program---for constructing functions that always work correctly with a particular type of data known as **algebraic data types**.

## Algebraic Data Types

An algebraic data type consists of sum and product types:

* Sum types encode a choice, or logical or, or one-of. For example, a website visitor is anonymous *or* a user but not both.
* Product types encode composition, or logical and, or all-of. For example, a book is a publication *and* a manuscript.

In Scala an algebraic data type is any data defined using sealed traits and final case classes.

An example is the built-in `List`. A `List[A]` is either:

* the empty list `Nil`
* the pair `::[A](hd: A, tl: List[A])`

This is an algebraic data type: have a sum-type (`List`), a product type (`::`), and a finite number of cases.

By the way, if you're wondering how an expression like `1 :: Nil` works, `::` is also a method on `List` which constructs a `::` object. We can construct a `::` directly by putting the name in back-ticks like so `` `::`(1, Nil) ``.

## Constructing Algebraic Data Types

Modelling real world concepts accurately is one of the more difficult parts of programs, but once that is done constructing an algebraic data types is simple. Let's look at a simple example.

Imagine we are modelling the stock for a computer hardware retailer. We have decided on a representation of a computer as either a desktop or a laptop. A computer has RAM, a CPU, and a hard disk, and a laptop also has a screen and a form factor. Assuming we've already defined types for RAM, CPUs, and so on, we can immediately write down the following code.

~~~ scala
sealed trait Computer {
  def ram: Ram
  def cpu: Cpu
  def hdd: Hdd
}
final case class Desktop(ram: Ram, cpu: Cpu, hdd: Hdd) extends Computer
final case class Laptop(ram: Ram, cpu: Cpu, hdd: Hdd, screen: Screen, formFactor: FormFactor) extends Computer
~~~

The pattern is as follows:

* The data definition forms a tree. If `A` is a `B` or `C` (e.g. `Computer` is a `Desktop` or `Laptop`; a sum type) then `A` in the parent of `B` and `C` in the tree
* Leaf nodes in our data definition become `final` case classes. Other nodes become traits.
* Nodes `extend` their parent traits.
* Nodes that contain data (i.e. product types) declare that data as `def`s if they are traits and constructor arguments otherwise.

## Pattern Matching Algebraic Data Types

Pattern matching is the most basic method for dealing with algebraic data types. The rules for pattern matching are simple: you need one pattern for every leaf node in the data definition. The compiler will even complain if we miss out a case. For our computer example this means all our pattern matches need to look like

~~~ scala
computer match {
  case Desktop(ram, cpu, hdd) => ...
  case Laptop(ram. cpu, hdd, screen, formFactor) => ...
}
~~~

If we follow this pattern (pun most definitely intended) then our code will never go wrong, so long as do the right thing following each match.

## Folding Algebraic Data Types

Using a `fold` method is another way to deal with algebraic data types. For a given algebraic data type, the pattern matching statements are always the same and it's only the computation after the match that changes. We can abstract this into a method that itself takes functions, one for every case in the pattern matching expression. This function is `fold`.

For example, we could define a `fold` on our `Computer` type as follows

~~~ scala
sealed trait Computer {
  def ram: Ram
  def cpu: Cpu
  def hdd: Hdd

  def fold[A](desktop: (ram: Ram, cpu: Cpu, hdd: Hdd) => A,
              laptop: (ram: Ram, cpu: Cpu, hdd: Hdd, screen: Screen, formFactor: FormFactor) => A): A
}
final case class Desktop(ram: Ram, cpu: Cpu, hdd: Hdd) extends Computer {
  def fold[A](desktop: (ram: Ram, cpu: Cpu, hdd: Hdd) => A,
              laptop: (ram: Ram, cpu: Cpu, hdd: Hdd, screen: Screen, formFactor: FormFactor) => A): A =
    desktop(ram, cpu, hdd)
}
final case class Laptop(ram: Ram, cpu: Cpu, hdd: Hdd, screen: Screen, formFactor: FormFactor) extends Computer {
  def fold[A](desktop: (ram: Ram, cpu: Cpu, hdd: Hdd) => A,
              laptop: (ram: Ram, cpu: Cpu, hdd: Hdd, screen: Screen, formFactor: FormFactor) => A): A =
    laptop(ram, cpu, hdd)
}
~~~

This is quite a simple case. More complex data, such as `List`, is defined recursively. Recall that a `List[A]` is either:

* the empty list `Nil`
* the pair `::[A](hd: A, tl: List[A])`

The pair is a recursive case, as it contains a `List`. The pattern for `fold` in this case is to recursively call `fold`. Once again the code mirrors the data definition.

~~~ scala
sealed trait List[A] {
  def fold[B](zero: B)(f: (A, B) => B): B
}
final case class ::[A](hd: A, tl: List[A]) extends List[A] {
  def fold[B](zero: B)(f: (A, B) => B): B =
    f(hd, tl.fold(zero)(f))
}
final case class Nil[A] extends List[A] {
  def fold[B](zero: B)(f: (A, B) => B): B =
    zero
}
~~~

Fold has a universal property meaning that any transformation of an algebraic data type can be written using that data type's fold.

## Exercises

### Tree Algebraic Data Type

A binary tree can be defined as follows:

* A `Tree` is a `Node` with a left and right `Tree` or a `Leaf` with an element of type `A`.

Implement this algebraic data type.

<div class="solution">
~~~ scala
sealed trait Tree[A]
final case class Node[A](val l: Tree[A], val r: Tree[A]) extends Tree[A]
final case class Leaf[A](val elt: A) extends Tree[A]
~~~
</div>


### Tree Fold

Write a `fold` for `Tree`.

<div class="solution">
~~~ scala
sealed trait Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B
}
final case class Node[A](val l: Tree[A], val r: Tree[A]) extends Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B =
    node(l, r)
}
final case class Leaf[A](val elt: A) extends Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B =
    leaf(elt)
}
~~~
</div>


### Basic Origami

Use your `fold` method to calculate the sum and product of the tree `Node(Leaf(1), Node(Node(Leaf(2), Leaf(3)), Leaf(4)))`

<div class="solution">
~~~ scala
scala> val tree = Node(Leaf(1), Node(Node(Leaf(2), Leaf(3)), Leaf(4)))
tree: Node[Int] = Node(Leaf(1),Node(Node(Leaf(2),Leaf(3)),Leaf(4)))

scala> val sum = tree.fold((l: Int, r: Int) => l + r, (x: Int) => x, tree)
sum: Int = 10

scala> val product = tree.fold((l: Int, r: Int) => l * r, (x: Int) => x, tree)
product: Int = 24
~~~
</div>


### Map

Using your `fold`, write `map`.

<div class="solution">
~~~ scala
sealed trait Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B
  def map[B](f: A => B): Tree[B] =
    fold(Node.apply[B] _, (elt: A) => Leaf(f(elt)))
}
final case class Node[A](val l: Tree[A], val r: Tree[A]) extends Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B =
    node(l, r)
}
final case class Leaf[A](val elt: A) extends Tree[A] {
  def fold[B](node: (B, B) => B, leaf: A => B): B =
    leaf(elt)
}
~~~

As `map` is defined entirely in terms of `fold` we don't need to implement it on `Node` and `Leaf`. Notice how I've used the companion class constructor, `Node.apply`, in the call to `fold`.
</div>

## Folding vs Pattern Matching

We now have two ways to dealing with algebraic data types: pattern matching and higher-order functions such as `fold` and `map`. Which should we prefer? Pattern matching, being more concrete, is easier for most beginners. Pattern matching is also normally a bit faster, and we will see later that pattern matching is extensible. Finally, for data types that do not have any generic types it would be very unusual to use higher-order functions.

For data that does involve generics, experienced Scala programmers tend to prefer higher order functions. As they are more abstract they are a bit more resilient to change. Recall for-comprehensions from the section on collections. They work with any type implementing `map`, `flatMap`, and `foreach`. As we have seen, lots of data types implement these methods. Code written to this interface can change its underlying representation without requiring wholesale code changes. This is not the case with pattern matching as we're matching on the concrete types in the implementation.
