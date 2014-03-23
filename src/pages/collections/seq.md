---
layout: page
title: Sequences
---

A sequence is a collection of items with a defined and stable order. Sequences are one of the most common data structures. In this section we're going to look at the basics of sequences: creating them, key methods on sequences, and the distinction between mutable and immutable sequences.

Here's how you create a sequence in Scala:

~~~ scala
scala> val sequence = Seq(1, 2, 3)
sequence: Seq[Int] = List(1, 2, 3)
~~~

This immediately shows off a key feature of Scala's collections, the **separation between interface and implementation**. In the above, the value has type `Seq[Int]` but is implemented by a `List`.

## Basic operations

Sequences implement [many methods](http://docs.scala-lang.org/overviews/collections/seqs.html). Let's look at some of the most common.

### Accessing elements

Accessing an element is done by calling the `apply` method with an index. Indices start from 0. As you'd expect, the element at the index is returned.

~~~ scala
scala> sequence.apply(0)
res0: Int = 1
~~~

We can also use the shortcut syntax for `apply`.

~~~ scala
scala> sequence(0)
res1: Int = 1
~~~

An exception is raised if we use an index that is out of bounds.

~~~ scala
scala> sequence(3)
java.lang.IndexOutOfBoundsException: 3
...
~~~

### Determining membership

The `contains` method determines whether a sequence contains an element:

~~~ scala
scala> sequence.contains("a")
res2: Boolean = true
~~~

### Determining size

Finding the size (length) of a sequence is straightforward.

~~~ scala
scala> sequence.size
res3: Int = 3
~~~

### Adding elements to a sequence

There are many ways to add elements to a sequence. We can prepend an element with the `+:` method.

~~~ scala
scala> sequence.+:(0)
res4: Seq[Int] = List(0, 1, 2, 3)
~~~

It is more idiomatic to call `+:` as an operator, where the trailing colon makes it right associative.

~~~ scala
scala> 0 +: sequence
res5: Seq[Int] = List(0, 1, 2, 3)
~~~

To append an element we use the `:+` method. Again it is more idiomatic to call `:+` as an operator.

~~~ scala
scala> sequence.:+(4)
res6: Seq[Int] = List(1, 2, 3, 4)

scala> sequence :+ 4
res7: Seq[Int] = List(1, 2, 3, 4)
~~~

Finally we can append and prepend entire sequences using the `++` and `++:` methods.

~~~ scala
scala> Seq(-2, -1, 0) ++: sequence
res8: Seq[Int] = List(-2, -1, 0, 1, 2, 3)

scala> sequence.++:(Seq(-2, -1, 0))
res9: Seq[Int] = List(-2, -1, 0, 1, 2, 3)

scala> sequence ++ Seq(4, 5, 6)
res10: Seq[Int] = List(1, 2, 3, 4, 5, 6)
~~~

## Mutable and Immutable Sequences

The astute will have noticed that none of the operations above modified our original sequence. In fact we haven't even show a method to modify a `Seq` -- this is because there is none! Throughout the collections framework, the default implementations are immutable. Immutability makes reasoning about programs easier, and plays well with concurrency.

A sequence can be functionality updated. That is, we can create a copy with a single element changed.

~~~ scala
scala> sequence.updated(0, 5)
res11: Seq[Int] = List(5, 2, 3)
~~~

Sometimes, however, mutable collections are desirable. Scala provides two parallel collections hierarchies, `scala.collection.mutable` and `scala.collection.immutable`. The default `Seq` is defined to be `scala.collection.immutable.Seq`. If we want a mutable sequence we can use `scala.collection.mutable.Seq`.

~~~ scala
scala> val mutable = scala.collection.mutable.Seq(1, 2, 3)
mutable: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)
~~~

Note that the concrete implementation class is now an `ArrayBuffer` not a `List`.

In addition to all the methods on an immutable sequence, a mutable sequence can be updated using the `update` method. Note that `update` returns `Unit`, so no value is printed in the REPL after this call. When we print the original sequence we see it is changed.

~~~ scala
scala> mutable.update(0, 5)

scala> mutable
res14: scala.collection.mutable.Seq[Int] = ArrayBuffer(5, 2, 3)
~~~

More idiomatic is to use the operator form of `update`.

~~~ scala
scala> mutable(0) = 7

scala> mutable
res16: scala.collection.mutable.Seq[Int] = ArrayBuffer(7, 2, 3)
~~~

As with `apply`, this assignment syntax is just syntactic sugar built on top of the `update` method.

Methods defined on both mutable and immutable sequences will never perform destructive updates. For example, appending an element with `:+` will never modify the original sequence.

~~~ scala
scala> val mutable = scala.collection.mutable.Seq[Int](1, 2, 3)
mutable: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)

scala> mutable :+ 4
res10: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3, 4)

scala> mutable
res11: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)
~~~

## In summary

Here is a type table of all the methods we have seen so far:

|------------+------------+--------------------+-------------|
| Method     | We have    | We provide         | We get      |
|------------+------------+--------------------+-------------|
| `Seq(...)` |            | `[A]`, ...         | `Seq[A]`    |
| `apply`    | `Seq[A]`   | `Int`              | `A`         |
| `:+`, `+:` | `Seq[A]`   | `A`                | `Seq[A]`    |
| `++`       | `Seq[A]`   | `Seq[A]`           | `Seq[A]`    |
| `contains` | `Seq[A]`   | `A`                | `Boolean`   |
| `size`     | `Seq[A]`   |                    | `Int`       |
|============================================================|
{: .table }

and the extras for mutable sequences:

|------------+------------+-------------------+-------------|
| Method     | We have    | We provide        | We get      |
|------------+------------+-------------------+-------------|
| `+=`       | `Seq[A]`   | `A`               | `Seq[A]`    |
| `-=`       | `Seq[A]`   | `A`               | `Seq[A]`    |
| `update`   | `Seq[A]`   | `Int`, `A`        | `Unit`      |
|===========================================================|
{: .table }

## Exercises

Here are a few simple exercises to familiarise yourself with the sequence API.

Create a `Seq` containing the `String`s `"cat"`, `"dog"`, and `"penguin"`. Bind it to the name `animals`.

<div class="solution">
~~~ scala
scala> val animals = Seq("cat", "dog", "penguin")
animals: Seq[String] = List(cat, dog, penguin)
~~~
</div>

Append the element `"tyrannosaurus"` to `animals` and prepend the element `"mouse"`.

<div class="solution">
~~~ scala
scala> "mouse" +: animals :+ "tyrannosaurus"
res6: Seq[String] = List(mouse, cat, dog, penguin, tyrannosaurus)
~~~
</div>

What will happen if you prepend the `Int` `2` to `animals`? Why? Try it out. Where you correct?

<div class="solution">
The returned sequence has type `Seq[Any]`.  It is perfectly valid to return a supertype (`Seq[Any]`) from a non-destructive operation.

~~~ scala
scala> 2 +: animals
res7: Seq[Any] = List(2, cat, dog, penguin)
~~~

You might expect a type error here, and in more real code this would be an error, so be aware of this one case where the type system doesn't protect you. Notice that if we try to mutate a sequence (obviously we have to use a mutable sequence) it is a type error:

~~~ scala
scala> val mutable = scala.collection.mutable.Seq("cat", "dog", "elephant")
mutable: scala.collection.mutable.Seq[String] = ArrayBuffer(cat, dog, elephant)

scala> mutable(0) = 2
<console>:9: error: type mismatch;
 found   : Int(2)
 required: String
              mutable(0) = 2
                           ^
~~~
</div>
