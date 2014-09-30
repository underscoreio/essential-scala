---
layout: page
title: Working with Sequences
---

In the [previous section](seq.html) with looked at the basic operations on sequences. Now we're going to look at practical aspects of working with sequences -- how functional programming allows us to process every element of a sequence at once in a terse and declarative style.

## Bulk Processing of Elements

When working with sequences we often want to deal with the collection as a whole, rather than accessing and manipulating individual elements. In Java we have to do this using loops. Scala gives us a number of powerful options that allow us to solve many problems more elegantly.

### map

Let's start with something simple -- suppose we want to double every element of a sequence. In Java we would do this using a `for` or a `while` loop. However, this requires writing several lines of looping machinery for only one line of actual doubling functionality.

In Scala we can use the `map` method that exists on every sequence. `map` takes a function and applies it to every element, creating a sequence of the results. To double every element we can write:

~~~ scala
scala> val sequence = Seq(1, 2, 3)
sequence: Seq[Int] = List(1, 2, 3)

scala> sequence.map(elt => elt * 2)
res0: Seq[Int] = List(2, 4, 6)
~~~

If we use *placeholder syntax* we can write this even more compactly:

~~~ scala
scala> sequence.map(_ * 2)
res1: Seq[Int] = List(2, 4, 6)
~~~

Given a sequence with type `Seq[A]`, the function we pass to `map` must have type `A => B` and we get a `Seq[B]` as a result. This isn't right for every situation. For example, suppose we have a sequence of strings, and we want to generate a sequence of all the permutations of those strings. We can call the `permutations` method on a string to get all permutations of it:

~~~ scala
scala> "dog".permutations
res12: Iterator[String] = non-empty iterator
~~~

This returns an `Iterable`, which is a bit like a Java `Iterator`. We're going to look at Iterables in more detail later. For now all we need to know is tha we can call the `toList` method to convert an `Iterable` to a `List`.

~~~ scala
scala> "dog".permutations.toList
res13: List[String] = List(dog, dgo, odg, ogd, gdo, god)
~~~

Thus we could write:

~~~ scala
scala> Seq("a", "wet", "dog").map(_.permutations.toList)
res14: Seq[List[String]] = List(List(a), List(wet, wte, ewt, etw, twe, tew), List(dog, dgo, odg, ogd, gdo, god))
~~~

but we end up with a sequence of sequences. Let's look at the types in more detail to see what's gone wrong:

| Method | We have       | We provide               | We get              |
|--------+---------------+--------------------------+---------------------|
| `map`  | `Seq[A]`      | `A => B`                 | `Seq[B]`            |
| `map`  | `Seq[String]` | `String => List[String]` | `Seq[List[String]]` |
| `???`  | `Seq[A]`      | `A => Seq[B]`            | `Seq[B]`            |
|=========================================================================|
{: .table .table-bordered }

What is the method `???` that we can use to collect a single flat sequence?

### flatMap

Our mystery method above is called `flatMap`. If we simply replace `map` with `flatMap` we get the answer we want:

~~~ scala
scala> Seq("a", "wet", "dog").flatMap(_.permutations.toList)
res15: Seq[String] = List(a, wet, wte, ewt, etw, twe, tew, dog, dgo, odg, ogd, gdo, god)
~~~

`flatMap` is similar to `map` except that it expects our function to return a sequence. The sequences for each input element are appended together. For example:

~~~ scala
scala> Seq(1, 2, 3).flatMap(num => Seq(num, num * 10))
res16: List[Int] = List(1, 10, 2, 20, 3, 30)
~~~

The end result is (nearly) always the same type as the original sequence: `aList.flatMap(...)` returns another `List`, `aVector.flatMap(...)` returns another `Vector`, and so on:

~~~ scala
scala> import scala.collection.immutable.Vector

scala> Vector(1, 2, 3).flatMap(num => Seq(num, num * 10))
res17: scala.collection.immutable.Vector[Int] = Vector(1, 10, 2, 20, 3, 30)
~~~

### foldLeft and foldRight

Now let's look at another kind of operation. Say we have a `Seq[Int]` and we want to add all the numbers together. `map` and `flatMap` don't apply here for two reasons:

 - they expect a *unary* function, whereas `+` is a *binary* operation;
 - they both return sequences of items, whereas we want to return a single `Int`.

There are also two further wrinkles:

 - what result do we expect if the sequence is empty? If we're adding items together then `0` seems like a natural result, but what is the answer in general?
 - although `+` is associative (i.e. `a+b == b+a`), in general we may need to specify an order in which to pass arguments to our binary function.

Let's make another type table to see what we're looking for:

| Method | We have    | We provide                  | We get   |
|--------+------------+-----------------------------+----------|
| `???`  | `Seq[Int]` | `0` and `(Int, Int) => Int` | `Int`    |
|==============================================================|
{: .table .table-bordered }

The methods that fit the bill are called folds, with two common cases `foldLeft` and `foldRight` correspond to the order the fold is applied. The job of these methods is to traverse a sequence and accumulate a result. The types are as follows:

| Method      | We have  | We provide            | We get   |
|-------------+----------+-----------------------+----------|
| `foldLeft`  | `Seq[A]` | `B` and `(B, A) => B` | `B`      |
| `foldRight` | `Seq[A]` | `B` and `(A, B) => B` | `B`      |
|===========================================================|
{: .table .table-bordered }

Given the sequence `Seq(1, 2, 3)`, `0`, and `+` the methods calculate the following:

| Method                              | Operations               | Notes                          |
|-------------------------------------+--------------------------+--------------------------------|
| `Seq(1, 2, 3).foldLeft(0)(_ + _)`   | `(((0 + 1) + 2) + 3)`    | Evaluation starts on the left  |
| `Seq(1, 2, 3).foldRight(0)(_ + _)`  | `(1 + (2 + (3 + 0)))`    | Evaluation starts on the right |
|=================================================================================================|
{: .table .table-bordered }

As we know from studying algebraic data types, the fold methods are very flexible. We can write *any* transformation on a sequence in terms of fold.

### foreach

There is one more traversal method that is commonly used: `foreach`. Unlike `map`, `flatMap` and the `fold`s, `foreach` does not return a useful result -- we use it purely for its side-effects. The type table is:

| Method    | We have  | We provide  | We get   |
|-----------+----------+-------------+----------|
| `foreach` | `Seq[A]` | `A => Unit` | `Unit`   |
|===============================================|
{: .table .table-bordered }

A great example using `foreach` is printing the elements of a sequence:

~~~ scala
scala> List(1, 2, 3).foreach(num => println("And a " + num + "..."))
And a 1...
And a 2...
And a 3...
~~~

### Algebra of Transformations

We've seen the four major traversal functions, `map`, `flatMap`, `fold`, and `foreach`. It can be difficult to know which to use, but it turns out there is a simple way to decide: look at the types! The type table below gives the types for all the operations we've seen so far. To use it, start with the data you have (always a `Seq[A]` in the table below) and then look at the functions you have available and the result you want to obtain. The final column will tell you which method to use.

| We have  | We provide            | We want   | Method      |
|----------+-----------------------+-----------+-------------|
| `Seq[A]` | `A => Unit`           | `Unit`    | `foreach`   |
| `Seq[A]` | `A => B`              | `Seq[B]`  | `map`       |
| `Seq[A]` | `A => Seq[B]`         | `Seq[B]`  | `flatMap`   |
| `Seq[A]` | `B` and `(B, A) => B` | `B`       | `foldLeft`  |
| `Seq[A]` | `B` and `(A, B) => B` | `B`       | `foldRight` |
|============================================================|
{: .table }

This type of analysis may see foreign at first, but you will quickly get used to it. Your two steps in solving any problem with sequences should be: think about the types, and experiment on the REPL!

## Exercises

The goals of this exercise are for you to learn your way around the collections API, but more importantly to learn to use types to drive implementation. When approaching each exercise you should answer:

1. What is the type of the data we have available?
2. What is the type of the result we want?
3. What is the type of the operations we will use?

When you have answered these questions look at the type table above to find the correct method to use. Done in this way the actual programming should be straightforward.


## Exercises

### Heroes of the Silver Screen

These exercises re-use the example code from the *Directorial Debut* exercise from the previous section:

#### Nolan Films

Create a list containing the names of the films directed by Christopher Nolan.

<div class="solution">
~~~ scala
nolan.films.map(_.name)
~~~
</div>

#### Cinephile

Create a list containing the names of all films by all directors.

<div class="solution">
~~~ scala
directors.flatMap(director => director.films.map(film => film.name))
~~~
</div>

#### Vintage McTiernan

Find the date of the earliest McTiernan film.

Tip: you can concisely find the minimum of two numbers `a` and `b` using `math.min(a, b)`.

<div class="solution">
~~~ scala
mcTiernan.films.foldLeft(9999) { (current, film) =>
  math.min(current, film.yearOfRelease)
}
~~~
</div>

#### High Score Table

Find all films sorted by descending IMDB rating:

<div class="solution">
~~~ scala
directors
  .flatMap(director => director.films)
  .sortWith((a, b) => a.imdbRating > b.imdbRating)
~~~
</div>

Now find the *average score* across all films:

<div class="solution">
We cache the list of films in a variable because we use it twice -- once to calculate the sum of the ratings and once to fetch the number of films:

~~~ scala
val films = directors.flatMap(director => director.films)

films.foldLeft(0)((a, b) => a.imdbRating + b.imdbRating) / films.length
~~~
</div>

#### Tonight's Listings

Print the following for every film: `"Tonight only! FILM NAME by DIRECTOR!"`

<div class="solution">
Println is used for its side-effects so we don't need to accumulate a result -- we use `println` as a simple iterator:

~~~ scala
directors.foreach { director =>
  director.films.foreach { film =>
    println(s"Tonight! ${film.name} by ${director.name}!")
  }
}
~~~
</div>

#### From the Archives

And now find the *earliest film* by any director:

<div class="solution">
Here's the solution:

~~~ scala
directors
  .flatMap(director => director.films)
  .sortWith((a, b) => a.yearOfRelease < b.yearOfRelease)
  .headOption
~~~
</div>

### Do-It-Yourself

Now we know the essential methods of `Seq`, we can write our own versions of some other library methods.

#### Minimum

Write a method to find the smallest element of a `Seq[Int]`.

<div class="solution">
This is another fold. We have a `Seq[Int]`, the minimum operation is `(Int, Int) => Int`, and we want an `Int`. The challenge is to find the zero value.

What is the identity for `min` so that `min(x, identity) = x`. It is positive infinity, which in Scala we can write as `Int.MaxValue` (see, fixed width numbers do have benefits).

Thus the solution is:

~~~ scala
def smallest(seq: Seq[Int]): Int =
  seq.foldLeft(Int.MaxValue)(math.min)
~~~
</div>

#### Unique

Given `Seq(1, 1, 2, 4, 3, 4)` create the sequence containing each number only once. Order is not important, so `Seq(1, 2, 4, 3)` or `Seq(4, 3, 2, 1)` are equally valid answers. Hint: Use `contains` to check if a sequence contains a value.

<div class="solution">
Once again we follow the same pattern. The types are:

1. We have a `Seq[Int]`
2. We want a `Seq[Int]`
3. Constructing the operation we want to use requires a bit more thought. The hint is to use `contains`. We can keep a sequence of the unique elements we've seen so far, and use `contains` to test if the sequence contains the current element. If we have seen the element we don't add it, otherwise we do. In code

   ~~~ scala
   def insert(seq: Seq[Int], elt: Int): Seq[Int] = {
     if(seq.contains(elt))
       seq
     else
       elt +: seq
   }
   ~~~

We these three pieces we can solve the problem. Looking at the type table we see we want a `fold`. Once again we must find the identity element. In this case the empty sequence is what we want. Why so? Think about what the answer should be if we try to find the unique elements of the empty sequence.

Thus the solution is

~~~ scala
def insert(seq: Seq[Int], elt: Int): Seq[Int] = {
  if(seq.contains(elt))
    seq
  else
    elt +: seq
}

def unique(seq: Seq[Int]): Seq[Int] = {
  seq.foldLeft(Seq.empty[Int]){ insert _ }
}

unique(Seq(1, 1, 2, 4, 3, 4))
~~~

Note how I created the empty sequence. I could have written `Seq[Int]()` but in both cases I need to supply a type (`Int`) to help the type inference along.
</div>

#### Reverse

Write a function that reverses the elements of a sequence. Your output does not have to use the same concrete implementation as the input. Hint: use `foldLeft`.

<div class="solution">
In this exercise, and the ones that follow, using the types are particularly important. Start by writing down the type of `reverse`.

~~~ scala
def reverse[A, B](seq: Seq[A], f: A => B): Seq[B] = {
  ???
}
~~~

The hint says to use `foldLeft`, so let's go ahead and fill in the body as far as we can.

~~~ scala
def reverse[A](seq: Seq[A]): Seq[A] = {
  seq.foldLeft(???){ ??? }
}
~~~

We need to work out the function to provide to `foldLeft` and the zero or identity element. For the function, the type of `foldLeft` requires it is `(Seq[A], A) => Seq[A]`. If we flip the types around the `+:` method on `Seq` has the right types.

For the zero element we know that it must have the same type as the return type of `reverse` (because the result of the fold is the result of `reverse`). Thus it's a `Seq[A]`. Which sequence? There are a few ways to answer this:

- The only `Seq[A]` we can create in this method, before we know what `A` is, is the empty sequence `Seq.empty[A]`.
- The identity element is one such that `x +: zero = Seq(x)`. Again this must be the empty sequence.

So we now we can fill in the answer.

~~~ scala
def reverse[A](seq: Seq[A]): Seq[A] = {
  seq.foldLeft(Seq.empty[A]){ (seq, elt) => elt +: seq }
}
~~~
</div>

#### Map

Write `map` in terms of `foldRight`.

<div class="solution">
Follow the same process as before: write out the type of the method we need to create, and fill in what we know. We start with `map` and `foldRight`.

~~~ scala
def map[A, B](seq: Seq[A], f: A => B): Seq[B] = {
  seq.foldRight(???){ ??? }
}
~~~

As usual we need to fill in the zero element and the function. The zero element must have type `Seq[B]`, and the function has type `(A, Seq[B]) => Seq[B])`. The zero element is straightforward: `Seq.empty[B]` is the only sequence we can construct of type `Seq[B]`. For the function, we clearly have to convert that `A` to a `B` somehow. There is only one way to do that, which is with the function supplied to `map`. We then need to add that `B` to our `Seq[B]`, for which we can use the `+:` method. This gives us our final result.

~~~ scala
def map[A, B](seq: Seq[A], f: A => B): Seq[B] = {
  seq.foldRight(Seq.empty[B]){ (elt, seq) => f(elt) +: seq }
}
~~~
</div>

#### Fold Left

Write your own implementation of `foldLeft` that uses `foreach` and mutable state. Remember you can create a mutable variable using the `var` keyword, and assign a new value using `=`. For example

~~~ scala
scala> var mutable = 1
var mutable = 1
mutable: Int = 1

scala> mutable = 2
mutable = 2
mutable: Int = 2
~~~

<div class="solution">
Once again, write out the skeleton and then fill in the details using the types. We start with

~~~ scala
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  seq.foreach { ??? }
}
~~~

Let's look at what we have need to fill in. `foreach` returns `Unit` but we need to return a `B`. `foreach` takes a function of type `A => Unit` but we only have a `(B, A) => B` available. The `A` can come from `foreach` and by now we know that the `B` is the intermediate result. We have the hint to use mutable state and we know that we need to keep a `B` around and return it, so let's fill that in.

~~~ scala
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  var result: B = ???
  seq.foreach { (elt: A) => ??? }
  result
}
~~~

At this point we can just follow the types. `result` must be initially assigned to the value of `zero` as that is the only `B` we have. The body of the function we pass to `foreach` must call `f` with `result` and `elt`. This returns a `B` which we must store somewhere -- the only place we have to store it is in `result`. So the final answer becomes

~~~ scala
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  var result = zero
  seq.foreach { elt => result = f(result, elt) }
  result
}
~~~
</div>

There are many other methods on sequences. Consult the [API documentation](http://www.scala-lang.org/api/current/scala/collection/Seq.html) for the `Seq` trait for more information.
