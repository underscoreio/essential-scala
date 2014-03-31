---
layout: page
title: Sequences
---

A *sequence* is a collection of items with a defined and stable order. Sequences are one of the most common data structures. In this section we're going to look at the basics of sequences: creating them, key methods on sequences, and the distinction between mutable and immutable sequences.

Here's how you create a sequence in Scala:

~~~ scala
scala> val sequence = Seq(1, 2, 3)
sequence: Seq[Int] = List(1, 2, 3)
~~~

This immediately shows off a key feature of Scala's collections, the **separation between interface and implementation**. In the above, the value has type `Seq[Int]` but is implemented by a `List`.

## Basic operations

Sequences implement [many methods](http://docs.scala-lang.org/overviews/collections/seqs.html). Let's look at some of the most common.

### Accessing elements

We can access the elements of a sequence using its `apply` method, which accepts an `Int` index as a parameter. Indices start from `0`.

~~~ scala
scala> sequence.apply(0)
res0: Int = 1

scala> sequence(0) // sugared syntax
res1: Int = 1
~~~

An exception is raised if we use an index that is out of bounds:

~~~ scala
scala> sequence(3)
java.lang.IndexOutOfBoundsException: 3
...
~~~

### Sequence length

Fortunately, finding the length of a sequence is straightforward:

~~~ scala
scala> sequence.length
res3: Int = 3
~~~

### Membership

The `contains` method tells us a sequence contains an element:

~~~ scala
scala> sequence.contains("a")
res2: Boolean = true
~~~

### Prepending/appending elements

There are many ways to add elements to a sequence. We can prepend an element with the `+:` method.

~~~ scala
scala> sequence.+:(0)
res4: Seq[Int] = List(0, 1, 2, 3)
~~~

It is more idiomatic to call `+:` as an infix operator, where the trailing colon makes it right associative:

~~~ scala
scala> 0 +: sequence
res5: Seq[Int] = List(0, 1, 2, 3)
~~~

To append an element we use the `:+` method. Again it is idiomatic to write `:+` as an operator:

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

### Updating elements

The `updated` method replaces the *nth* item in a sequence with a new value:

~~~ scala
scala> sequence.updated(0, 5)
res11: Seq[Int] = List(5, 2, 3)
~~~


## Mutable and Immutable Sequences

None of the methods we have covered so far have any side-effects -- like the `copy` method on a case class, they return a new copy of the sequence.

The default implementations are immutable throughout Scala's collections framework for the reasons we have discussed many times already.

Sometimes, however, we need mutable collections for our codebase. Fortunately, Scala provides two parallel collections hierarchies, one in the `scala.collection.mutable` package and one in the `scala.collection.immutable` package.

The default `Seq` is defined to be `scala.collection.immutable.Seq`. If we want a mutable sequence we can use `scala.collection.mutable.Seq`.

~~~ scala
scala> val mutable = scala.collection.mutable.Seq(1, 2, 3)
mutable: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)
~~~

Note that the concrete implementation class is now an `ArrayBuffer` and not a `List`.

### Destructive update

In addition to all the methods of an immutable sequence, a mutable sequence can be updated using the `update` method. Note that `update` returns `Unit`, so no value is printed in the REPL after this call. When we print the original sequence we see it is changed:

~~~ scala
scala> mutable.update(0, 5)

scala> mutable
res14: scala.collection.mutable.Seq[Int] = ArrayBuffer(5, 2, 3)
~~~

A more idiomatic way of calling `update` is to use **assignment operator syntax**, which is another special syntax built in to Scala, similar to [infix operator syntax](../intro/compound.html) and [function application syntax](../objects/functions.html):

~~~ scala
scala> mutable(1) = 7

scala> mutable
res16: scala.collection.mutable.Seq[Int] = ArrayBuffer(5, 7, 3)
~~~

### Immutable methods on mutable sequences

Methods defined on both mutable and immutable sequences will never perform destructive updates. For example, `:+` always returns a new copy of the sequence without updating the original:

~~~ scala
scala> val mutable = scala.collection.mutable.Seq[Int](1, 2, 3)
mutable: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)

scala> mutable :+ 4
res10: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3, 4)

scala> mutable
res11: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)
~~~

<div class="alert alert-info">
**Mutable collections tip:** Scala programmers tend to favour immutable collections and only bring in mutable ones in specific circumastances. Using `import scala.collection.mutable._` at the top of a file tends to create a whole series of naming collisions that we have to work around.

To work around this, I suggest importing the `mutable` package iteself rather than its contents. We can then explicitly refer to any mutable collection using the package name as a prefix, leaving the unprefixed names referring to the immutable versions:

~~~ scala
scala> import scala.collection.mutable
import scala.collection.mutable

scala> mutable.Seq(1, 2, 3)
res0: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)

scala> Seq(1, 2, 3)
res1: Seq[Int] = List(1, 2, 3)
~~~
</div>

## In summary

Here is a type table of all the methods we have seen so far:

|-------------+------------+--------------------+-------------|
| Method      | We have    | We provide         | We get      |
|-------------+------------+--------------------+-------------|
| `Seq(...)`  |            | `[A]`, ...         | `Seq[A]`    |
| `apply`     | `Seq[A]`   | `Int`              | `A`         |
| `:+`, `+:`  | `Seq[A]`   | `A`                | `Seq[A]`    |
| `++`, `++:` | `Seq[A]`   | `Seq[A]`           | `Seq[A]`    |
| `contains`  | `Seq[A]`   | `A`                | `Boolean`   |
| `size`      | `Seq[A]`   |                    | `Int`       |
|=============================================================|
{: .table .table-bordered .table-responsive }

and the extras for mutable sequences:

|------------+------------+-------------------+-------------|
| Method     | We have    | We provide        | We get      |
|------------+------------+-------------------+-------------|
| `update`   | `Seq[A]`   | `Int`, `A`        | `Unit`      |
|===========================================================|
{: .table .table-bordered .table-responsive }

## Exercises

### Animals

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

What happens if you prepend the `Int` `2` to `animals`? Why? Try it out... were you correct?

<div class="solution">
The returned sequence has type `Seq[Any]`.  It is perfectly valid to return a supertype (in this case `Seq[Any]`) from a non-destructive operation.

~~~ scala
scala> 2 +: animals
res7: Seq[Any] = List(2, cat, dog, penguin)
~~~

You might expect a type error here, but Scala is capable of determining the least upper bound of `String` and `Int` and setting the type of the returned sequence accordingly.

In most real code appending an `Int` to a `Seq[String]` would be an error. In practice, the type annotations we place on methods and fields protect against this kind of type error, but be aware of this behaviour just in case.

Note that if we try to mutate a sequence (obviously we have to use a mutable sequence), we *do* get a type error:

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
