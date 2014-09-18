---
layout: page
title: Sequencing Computations
---

In this section we're going to look at two more language features, **functions** and **generics**, and see some abstractions we can build using these features: **functors**, and **monads**.

Our starting point is code that we developed in the previous section. We developed `IntList`, a list of integers, and wrote like the following:

~~~ scala
sealed trait IntList {
  def double: IntList
  def product: Int
  def sum: Int
}
final case object Empty extends IntList {
  def double: IntList =
    Empty
  def product: Int =
    1
  def sum: Int =
    0
}
final case class Cell(head: Int, tail: IntList) extends IntList {
  def double: IntList =
    Cell(head * 2, tail.double)
  def product: Int =
    head * tail.product
  def sum: Int =
    head + tail.sum
}

def sum(list: IntList): Int =
  list match {
    case Empty => 0
    case Cell(hd, tl) => hd + sum(tl)
  }

def length(list: IntList): Int =
  list match {
    case Empty => 0
    case Cell(hd, tl) => 1 + length(tl)
  }

def product(list: IntList): Int =
  list match {
    case Empty => 1
    case Cell(hd, tl) => hd * product(tl)
  }

def double(list: IntList): IntList =
  list match {
    case Empty => Empty
    case Cell(hd, tl) => Cell(hd * 2, double(tl))
  }
~~~

There are two problems with this code. The first is that there is a lot of repetition. The code has the same general structure, which is unsurprising given we're using our structural recursion pattern, and it would be nice to reduce the amount of duplication. The second problem is that our list is restricted to storing `Int`s.

We will address both problems in this section. For the former we will use functions to **abstract over methods**, so we can remove duplication in the code. For the latter we will use generic types to **abstract over types**, so we can create data that works with user specified types.

As we work with these techniques we'll see some general patterns emerge. We'll name and investigate these patterns in more detail at the end of this section.
