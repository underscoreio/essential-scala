---
layout: page
title: Sequence Implementations
---

We've seen that the Scala collections seperate interface from implementation. This means we can work with all collections in a generic manner. However different concrete implementations have different performance characteristics, so we must be aware of the available implementations so we can choose appropriately. Here we look at the mostly frequently used implementations of `Seq`. For full details on all the available implementation see [the docs](http://docs.scala-lang.org/overviews/collections/introduction.html).

## Peformance Characteristics

The collections framework distinguishes at the type level two general classes of sequences. Sequences implementing `IndexedSeq` has efficient `apply`, `length`, and (if mutable) `update` operations. `LinearSeq`s have efficient `head` and `tail` operations. Neither have any additional operations over `Seq`.

## Immutable Implementations

The main immutable `Seq` implementations are `List`, and `Stream`, and `Vector`.

### List

A `List` is a singly linked list. It has constant time access to the first element and remainder of the list (`head`, and `tail`) and is thus a `LinearSeq`. It also has constant time prepending to the front of the list, but linear time appending to the end. `List` is the default `Seq` implementation.

### Stream

A `Stream` is like a list except its elements are computed on demand, and thus it can have infinite size. Like other collections we can create streams by calling the `apply` method on the companion object.

~~~ scala
scala> Stream(1, 2, 3)
res17: scala.collection.immutable.Stream[Int] = Stream(1, ?)
~~~

Note that only the first element is printed. The others will be computed when we try to access them.

We can also use the `#::` method to construct a stream from individual elements, starting from `Stream.empty`.

~~~ scala
scala> Stream.empty.#::(3).#::(2).#::(1)
res18: scala.collection.immutable.Stream[Int] = Stream(1, ?)
~~~

We can also use the more natural operator syntax.

~~~ scala
scala> 1 #:: 2 #:: 3 #:: Stream.empty
res19: scala.collection.immutable.Stream[Int] = Stream(1, ?)
~~~

This method allows us to create a infinite stream. Here's an infinite stream of 1s:

~~~ scala
scala> def streamOnes: Stream[Int] = 1 #:: streamOnes
streamOnes: Stream[Int]

scala> streamOnes
res20: Stream[Int] = Stream(1, ?)
~~~

Because elements are only evaluated as requested calling `streamOnes` doesn't lead to infinte recursion. When we take the first five elements (and convert them to a `List`, so they'll all print out) we see we have what we want.

~~~ scala
scala> streamOnes.take(5).toList
res22: List[Int] = List(1, 1, 1, 1, 1)
~~~

### Vector

`Vector` is the final immutable sequence we'll consider. Unlike `Stream` and `List` it is an `IndexedSeq`, and thus offers fast random access and updates. It is the default immutable `IndexedSeq` which we can see if we create one.

~~~ scala
scala> scala.collection.immutable.IndexedSeq(1, 2, 3)
res23: scala.collection.immutable.IndexedSeq[Int] = Vector(1, 2, 3)
~~~

Vectors are a good choice if you want both random access and immutability.


## Mutable Implementations

The mutable collections are probably more familiar. In addition to linked lists and arrays (which have [their own section](arrays-and-strings.html)) there are a buffers which allow for efficient construction of certain data structures.

### Buffers

`Buffer`s are used when you want to create efficiently create a data structure an item at a time. An `ArrayBuffer` is an `IndexedSeq` which also has constant time appends. A `ListBuffer` is like a `List` with constant time prepend *and* append (though note it is mutable, unlike `List`).

Buffers add methods to support destructive prepends and appends. For example, the `+=` is destructive append.

~~~ scala
scala> val buffer = new scala.collection.mutable.ArrayBuffer[Int]()
buffer: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer()

scala> buffer += 1
res16: buffer.type = ArrayBuffer(1)

scala> buffer
res17: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(1)
~~~


### StringBuilder

A `StringBuilder` is essentially a buffer for building strings. It is mostly the same as Java's `StringBuilder` except that it implements standard Scala collections method where there is a conflict. So, for example, the `reverse` method creates a new `StringBuilder` unlike in Java.

### LinkedLists

Mutable singly `LinkedList`s and `DoubleLinkedList`s work for the most part just like `List`. A `DoubleLikeList` maintains both a `prev` and `next` pointer and so allows for efficient removal of an element.
