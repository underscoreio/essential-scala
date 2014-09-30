---
layout: page
title: Sequencing Computations
---

In this section we're going to look at two more language features, **generics** and **functions**, and see some abstractions we can build using these features: **functors**, and **monads**.

Our starting point is code that we developed in the previous section. We developed `IntList`, a list of integers, and wrote like the following:

~~~ scala
sealed trait IntList {
  def double: IntList
  def product: Int
  def sum: Int
}
final case object End extends IntList {
  def double: IntList =
    End
  def product: Int =
    1
  def sum: Int =
    0
}
final case class Pair(head: Int, tail: IntList) extends IntList {
  def double: IntList =
    Pair(head * 2, tail.double)
  def product: Int =
    head * tail.product
  def sum: Int =
    head + tail.sum
}
~~~

There are two problems with this code. The first is that our list is restricted to storing `Int`s. The second problem is that here is a lot of repetition. The code has the same general structure, which is unsurprising given we're using our structural recursion pattern, and it would be nice to reduce the amount of duplication.

We will address both problems in this section. For the former we will use generics to **abstract over types**, so we can create data that works with user specified types. For the latter we will use functions to **abstract over methods**, so we can reduce duplication in our code.

As we work with these techniques we'll see some general patterns emerge. We'll name and investigate these patterns in more detail at the end of his section.
