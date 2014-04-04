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
        at ...
~~~

### Sequence length

Fortunately, finding the length of a sequence is straightforward:

~~~ scala
scala> sequence.length
res3: Int = 3
~~~

### Searching for elements

There are a few ways of searching for elements. The `contains` method tells us whether a sequence contains an element (using `==` for comparison):

~~~ scala
scala> sequence.contains(2)
res4: Boolean = true
~~~

The `find` method is like a generalised version of `contains` - we provide a test function and the sequence returns the first item for which the test returns `true`:

~~~ scala
scala> sequence.find(_ == "a")
res5: Option[Int] = Some(3)

scala> sequence.find(_ > 4)
res6: Option[Int] = None
~~~

The `Option` class here is Scala's built-in equivalent of our `PossibleResult` class from earlier. It has two subtypes -- `Some` and `None` -- representing the presence and absence of a value respectively.

The `filter` method is a variant of `find` that returns *all* the matching elements in the sequence:

~~~ scala
scala> sequence.filter(_ > 1)
res7: Seq[Int] = List(2, 3)
~~~

### Appending/prepending elements

There are many ways to add elements to a sequence. We can append an element with the `:+` method:

~~~ scala
scala> sequence.:+(4)
res6: Seq[Int] = List(1, 2, 3, 4)
~~~

It is more idiomatic to call `:+` as an infix operator:

~~~ scala
scala> sequence :+ 4
res7: Seq[Int] = List(1, 2, 3, 4)
~~~

We can similarly *prepend* an element using the `+:` method:

~~~ scala
scala> sequence.+:(0)
res4: Seq[Int] = List(0, 1, 2, 3)
~~~

Again, it is more idiomatic to call `+:` as an infix operator. Here **the trailing colon makes it right associative**, so we write the operator-style expression the other way around:

~~~ scala
scala> 0 +: sequence
res5: Seq[Int] = List(0, 1, 2, 3)
~~~

Finally we can concatenate entire sequences using the `++` method.

~~~ scala
scala> sequence ++ Seq(4, 5, 6)
res10: Seq[Int] = List(1, 2, 3, 4, 5, 6)
~~~

<div class="alert alert-info">
**Syntax tip:** Any Scala method ending with a `:` character becomes right associative when written as an infix operator.

This is another of Scala's general syntax rules. In this case the rule is designed to replicate Haskell-style operators for things like list prepend (`::`) and list concatenation (`:::`). For example:

~~~ scala
scala> 1 :: 2 :: 3 :: 4 :: 5 :: Nil
res1: List[Int] = List(1, 2, 3, 4, 5)

scala> List(1, 2, 3) ::: List(4, 5, 6)
res2: List[Int] = List(1, 2, 3, 4, 5, 6)
~~~

The `::` and `:::` methods are specific to lists whereas `+:`, `:+` and `++` work on any type of sequence. We recommend using the general methods wherever possible because make it easy to swap sequence implementations for different performance characteristics.
</div>

### Updating elements

The `updated` method replaces the *nth* item in a sequence with a new value:

~~~ scala
scala> sequence.updated(0, 5)
res11: Seq[Int] = List(5, 2, 3)
~~~

## In summary

Here is a type table of all the methods we have seen so far:

|-------------+------------+--------------------+-------------|
| Method      | We have    | We provide         | We get      |
|-------------+------------+--------------------+-------------|
| `Seq(...)`  |            | `[A]`, ...         | `Seq[A]`    |
| `apply`     | `Seq[A]`   | `Int`              | `A`         |
| `length`    | `Seq[A]`   |                    | `Int`       |
| `contains`  | `Seq[A]`   | `A`                | `Boolean`   |
| `find`      | `Seq[A]`   | `A => Boolean`     | `Option[A]` |
| `filter`    | `Seq[A]`   | `A`                | `Seq[A]`    |
| `:+`, `+:`  | `Seq[A]`   | `A`                | `Seq[A]`    |
| `++`        | `Seq[A]`   | `Seq[A]`           | `Seq[A]`    |
| `updated`   | `Seq[A]`   | `Int` `A`          | `Seq[A]`    |
|=============================================================|
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
</div>
