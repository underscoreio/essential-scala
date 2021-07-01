# Sequencing Computations

In this section we're going to look at two more language features, _generics_ and _functions_, and see some abstractions we can build using these features: _functors_, and _monads_.

Our starting point is code that we developed in the previous section. We developed `IntList`, a list of integers, and wrote code like the following:

```scala mdoc:silent
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
final case class Pair(head: Int, tail: IntList) extends IntList
```

There are two problems with this code. The first is that our list is restricted to storing `Int`s. The second problem is that here is a lot of repetition. The code has the same general structure, which is unsurprising given we're using our structural recursion pattern, and it would be nice to reduce the amount of duplication.

We will address both problems in this section. For the former we will use generics to _abstract over types_, so we can create data that works with user specified types. For the latter we will use functions to _abstract over methods_, so we can reduce duplication in our code.

As we work with these techniques we'll see some general patterns emerge. We'll name and investigate these patterns in more detail at the end of this section.
