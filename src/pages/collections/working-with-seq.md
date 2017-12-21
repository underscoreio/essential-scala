## Working with Sequences

In the [previous section](#seq) we looked at the basic operations on sequences. Now we're going to look at practical aspects of working with sequences---how functional programming allows us to process sequences in a terse and declarative style.

### Bulk Processing of Elements

When working with sequences we often want to deal with the collection as a whole, rather than accessing and manipulating individual elements. Scala gives us a number of powerful options that allow us to solve many problems more directly.

### Map

Let's start with something simple---suppose we want to double every element of a sequence. You might wish to express this as a loop. However, this requires writing several lines of looping machinery for only one line of actual doubling functionality.

In Scala we can use the `map` method defined on any sequence. `Map` takes a function and applies it to every element, creating a sequence of the results. To double every element we can write:

```tut:book
val sequence = Seq(1, 2, 3)

sequence.map(elt => elt * 2)
```

If we use *placeholder syntax* we can write this even more compactly:

```tut:book
sequence.map(_ * 2)
```

Given a sequence with type `Seq[A]`, the function we pass to `map` must have type `A => B` and we get a `Seq[B]` as a result. This isn't right for every situation. For example, suppose we have a sequence of strings, and we want to generate a sequence of all the permutations of those strings. We can call the `permutations` method on a string to get all permutations of it:

```tut:book
"dog".permutations
```

This returns an `Iterable`, which is a bit like a Java `Iterator`. We're going to look at iterables in more detail later. For now all we need to know is that we can call the `toList` method to convert an `Iterable` to a `List`.

```tut:book
"dog".permutations.toList
```

Thus we could write

```tut:book
Seq("a", "wet", "dog").map(_.permutations.toList)
```

but we end up with a sequence of sequences. Let's look at the types in more detail to see what's gone wrong:

+--------+---------------+--------------------------+---------------------+
| Method | We have       | We provide               | We get              |
+========+===============+==========================+=====================+
| `map`  | `Seq[A]`      | `A => B`                 | `Seq[B]`            |
+--------+---------------+--------------------------+---------------------+
| `map`  | `Seq[String]` | `String => List[String]` | `Seq[List[String]]` |
+--------+---------------+--------------------------+---------------------+
| `???`  | `Seq[A]`      | `A => Seq[B]`            | `Seq[B]`            |
+--------+---------------+--------------------------+---------------------+

What is the method `???` that we can use to collect a single flat sequence?

### FlatMap

Our mystery method above is called `flatMap`. If we simply replace `map` with `flatMap` we get the answer we want:

```tut:book
Seq("a", "wet", "dog").flatMap(_.permutations.toList)
```

`flatMap` is similar to `map` except that it expects our function to return a sequence. The sequences for each input element are appended together. For example:

```tut:book
Seq(1, 2, 3).flatMap(num => Seq(num, num * 10))
```

The end result is (nearly) always the same type as the original sequence: `aList.flatMap(...)` returns another `List`, `aVector.flatMap(...)` returns another `Vector`, and so on:

```tut:book:silent
import scala.collection.immutable.Vector
```

```tut:book
Vector(1, 2, 3).flatMap(num => Seq(num, num * 10))
```

### Folds

Now let's look at another kind of operation. Say we have a `Seq[Int]` and we want to add all the numbers together. `map` and `flatMap` don't apply here for two reasons:

1. they expect a *unary* function, whereas `+` is a *binary* operation;
2. they both return sequences of items, whereas we want to return a single `Int`.

There are also two further wrinkles to consider.

1. What result do we expect if the sequence is empty? If we're adding items together then `0` seems like a natural result, but what is the answer in general?
2. Although `+` is commutative (i.e. `a+b == b+a`), in general we may need to specify an order in which to pass arguments to our binary function.

Let's make another type table to see what we're looking for:

+--------+------------+-----------------------------+----------+
| Method | We have    | We provide                  | We get   |
+========+============+=============================+==========+
| `???`  | `Seq[Int]` | `0` and `(Int, Int) => Int` | `Int`    |
+--------+------------+-----------------------------+----------+


The methods that fit the bill are called folds, with two common cases `foldLeft` and `foldRight` corresponding to the order the fold is applied. The job of these methods is to traverse a sequence and accumulate a result. The types are as follows:

+-------------+----------+-----------------------+----------+
| Method      | We have  | We provide            | We get   |
+=============+==========+=======================+==========+
| `foldLeft`  | `Seq[A]` | `B` and `(B, A) => B` | `B`      |
+-------------+----------+-----------------------+----------+
| `foldRight` | `Seq[A]` | `B` and `(A, B) => B` | `B`      |
+-------------+----------+-----------------------+----------+


Given the sequence `Seq(1, 2, 3)`, `0`, and `+` the methods calculate the following:

+-------------------------------------+--------------------------+--------------------------------+
| Method                              | Operations               | Notes                          |
+=====================================+==========================+================================+
| `Seq(1, 2, 3).foldLeft(0)(_ + _)`   | `(((0 + 1) + 2) + 3)`    | Evaluation starts on the left  |
+-------------------------------------+--------------------------+--------------------------------+
| `Seq(1, 2, 3).foldRight(0)(_ + _)`  | `(1 + (2 + (3 + 0)))`    | Evaluation starts on the right |
+-------------------------------------+--------------------------+--------------------------------+

As we know from studying algebraic data types, the fold methods are very flexible. We can write *any* transformation on a sequence in terms of fold.

### Foreach

There is one more traversal method that is commonly used: `foreach`. Unlike `map`, `flatMap` and the `fold`s, `foreach` does not return a useful result---we use it purely for its side-effects. The type table is:

+-----------+----------+-------------+----------+
| Method    | We have  | We provide  | We get   |
+===========+==========+=============+==========+
| `foreach` | `Seq[A]` | `A => Unit` | `Unit`   |
+-----------+----------+-------------+----------+

A common example of using `foreach` is printing the elements of a sequence:

```tut:book
List(1, 2, 3).foreach(num => println("And a " + num + "..."))
```

### Algebra of Transformations

We've seen the four major traversal functions, `map`, `flatMap`, `fold`, and `foreach`. It can be difficult to know which to use, but it turns out there is a simple way to decide: look at the types! The type table below gives the types for all the operations we've seen so far. To use it, start with the data you have (always a `Seq[A]` in the table below) and then look at the functions you have available and the result you want to obtain. The final column will tell you which method to use.

+----------+-----------------------+-----------+-------------+
| We have  | We provide            | We want   | Method      |
+==========+=======================+===========+=============+
| `Seq[A]` | `A => Unit`           | `Unit`    | `foreach`   |
+----------+-----------------------+-----------+-------------+
| `Seq[A]` | `A => B`              | `Seq[B]`  | `map`       |
+----------+-----------------------+-----------+-------------+
| `Seq[A]` | `A => Seq[B]`         | `Seq[B]`  | `flatMap`   |
+----------+-----------------------+-----------+-------------+
| `Seq[A]` | `B` and `(B, A) => B` | `B`       | `foldLeft`  |
+----------+-----------------------+-----------+-------------+
| `Seq[A]` | `B` and `(A, B) => B` | `B`       | `foldRight` |
+----------+-----------------------+-----------+-------------+

This type of analysis may see foreign at first, but you will quickly get used to it. Your two steps in solving any problem with sequences should be: think about the types, and experiment on the REPL!

### Exercises

The goals of this exercise are for you to learn your way around the collections API, but more importantly to learn to use types to drive implementation. When approaching each exercise you should answer:

1. What is the type of the data we have available?
2. What is the type of the result we want?
3. What is the type of the operations we will use?

When you have answered these questions look at the type table above to find the correct method to use. Done in this way the actual programming should be straightforward.


#### Heroes of the Silver Screen

These exercises re-use the example code from the *Intranet Movie Database* exercise from the previous section:

*Nolan Films*

Starting with the definition of `nolan`, create a list containing the names of the films directed by Christopher Nolan.

```tut:invisible
// some definitions from previous section
case class Film(name: String, yearOfRelease: Int, imdbRating: Double)
case class Director(firstName: String, lastName: String, yearOfBirth: Int, films: Seq[Film])
val mcTiernan = new Director("John", "McTiernan", 1951, Seq(
  Film("Predator", 1987, 7.9),
  Film("Die Hard", 1988, 8.3),
  Film("The Hunt for Red October", 1990, 7.6),
  Film("The Thomas Crown Affair", 1999, 6.8)
))
val nolan = new Director("Christopher", "Nolan", 1970, Seq.empty)
val someBody = new Director("Just", "Some Body", 1990, Seq.empty)
val directors = Seq(mcTiernan, nolan, someBody)
```

<div class="solution">
```tut:book:silent
nolan.films.map(_.name)
```
</div>

*Cinephile*

Starting with the definition of `directors`, create a list containing the names of all films by all directors.

<div class="solution">
```tut:book:silent
directors.flatMap(director => director.films.map(film => film.name))
```
</div>

*Vintage McTiernan*

Starting with `mcTiernan`, find the date of the earliest McTiernan film.

Tip: you can concisely find the minimum of two numbers `a` and `b` using `math.min(a, b)`.

<div class="solution">
There are a number of ways to do this. We can sort the list of films and then retrieve the smallest element.

```tut:book:silent
mcTiernan.films.sortWith { (a, b) =>
  a.yearOfRelease < b.yearOfRelease
}.headOption
```

We can also do this by using a `fold`.

```tut:book:silent
mcTiernan.films.foldLeft(Int.MaxValue) { (current, film) =>
  math.min(current, film.yearOfRelease)
}
```

**A quick aside:**

There's a far simpler solution to this problem using a convenient method on sequences called `min`. This method finds the smallest item in a list of naturally comparable elements. We don't even need to sort them:

```tut:book:silent
mcTiernan.films.map(_.yearOfRelease).min
```

We didn't introduce `min` in this section because our focus is on working with general-purpose methods like `map` and `flatMap`. However, you may come across `min` in the documentation for the Scala standard library, and you may wonder how it is implemented.

Not all data types have a natural sort order. We might naturally wonder how `min` would work on a list of values of an unsortable data type. A quick experiment shows that the call doesn't even compile:

```tut:book:fail
mcTiernan.films.min
```

The `min` method is a strange beast---it only compiles when it is called on a list of *sortable values*. This is an example of something called the *type class pattern*. We don't know enough Scala to implement type classes yet---we'll learn all about how they work in Chapter [@sec:type-classes].
</div>

*High Score Table*

Starting with `directors`, find all films sorted by descending IMDB rating:

<div class="solution">
```tut:book:silent
directors.
  flatMap(director => director.films).
  sortWith((a, b) => a.imdbRating > b.imdbRating)
```
</div>

Starting with `directors` again, find the *average score* across all films:

<div class="solution">
We cache the list of films in a variable because we use it twice---once to calculate the sum of the ratings and once to fetch the number of films:

```tut:book:silent
val films = directors.flatMap(director => director.films)

films.foldLeft(0.0)((sum, film) => sum + film.imdbRating) / films.length
```
</div>

*Tonight's Listings*

Starting with `directors`, print the following for every film: `"Tonight only! FILM NAME by DIRECTOR!"`

<div class="solution">
Println is used for its side-effects so we don't need to accumulate a result---we use `println` as a simple iterator:

```tut:book:silent
directors.foreach { director =>
  director.films.foreach { film =>
    println(s"Tonight! ${film.name} by ${director.firstName} ${director.lastName}!")
  }
}
```
</div>

*From the Archives*

Finally, starting with `directors` again, find the *earliest film* by any director:

<div class="solution">
Here's the solution written using `sortWith`:

```tut:book:silent
directors.
  flatMap(director => director.films).
  sortWith((a, b) => a.yearOfRelease < b.yearOfRelease).
  headOption
```

We have to be careful in this solution to handle situations where there are no films. We can't use the `head` method, or even the `min` method we saw in the solution to *Vintage McTiernan*, because these methods throw exceptions if the sequence is empty:

```tut:book:fail
someBody.films.map(_.yearOfRelease).min
```
</div>

#### Do-It-Yourself

Now we know the essential methods of `Seq`, we can write our own versions of some other library methods.

*Minimum*

Write a method to find the smallest element of a `Seq[Int]`.

<div class="solution">
This is another fold. We have a `Seq[Int]`, the minimum operation is `(Int, Int) => Int`, and we want an `Int`. The challenge is to find the zero value.

What is the identity for `min` so that `min(x, identity) = x`. It is positive infinity, which in Scala we can write as `Int.MaxValue` (see, fixed width numbers do have benefits).

Thus the solution is:

```tut:book:silent
def smallest(seq: Seq[Int]): Int =
  seq.foldLeft(Int.MaxValue)(math.min)
```
</div>

*Unique*

Given `Seq(1, 1, 2, 4, 3, 4)` create the sequence containing each number only once. Order is not important, so `Seq(1, 2, 4, 3)` or `Seq(4, 3, 2, 1)` are equally valid answers. Hint: Use `contains` to check if a sequence contains a value.

<div class="solution">
Once again we follow the same pattern. The types are:

1. We have a `Seq[Int]`
2. We want a `Seq[Int]`
3. Constructing the operation we want to use requires a bit more thought. The hint is to use `contains`. We can keep a sequence of the unique elements we've seen so far, and use `contains` to test if the sequence contains the current element. If we have seen the element we don't add it, otherwise we do. In code

```tut:book:silent
def insert(seq: Seq[Int], elt: Int): Seq[Int] = {
 if(seq.contains(elt))
   seq
 else
   elt +: seq
}
```

With these three pieces we can solve the problem. Looking at the type table we see we want a `fold`. Once again we must find the identity element. In this case the empty sequence is what we want. Why so? Think about what the answer should be if we try to find the unique elements of the empty sequence.

Thus the solution is

```tut:book:silent
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
```

Note how I created the empty sequence. I could have written `Seq[Int]()` but in both cases I need to supply a type (`Int`) to help the type inference along.
</div>

*Reverse*

Write a function that reverses the elements of a sequence. Your output does not have to use the same concrete implementation as the input. Hint: use `foldLeft`.

<div class="solution">
In this exercise, and the ones that follow, using the types are particularly important. Start by writing down the type of `reverse`.

```tut:book:silent
def reverse[A, B](seq: Seq[A], f: A => B): Seq[B] = {
  ???
}
```

The hint says to use `foldLeft`, so let's go ahead and fill in the body as far as we can.

```scala
def reverse[A](seq: Seq[A]): Seq[A] = {
  seq.foldLeft(???){ ??? }
}
```

We need to work out the function to provide to `foldLeft` and the zero or identity element. For the function, the type of `foldLeft` requires it is `(Seq[A], A) => Seq[A]`. If we flip the types around the `+:` method on `Seq` has the right types.

For the zero element we know that it must have the same type as the return type of `reverse` (because the result of the fold is the result of `reverse`). Thus it's a `Seq[A]`. Which sequence? There are a few ways to answer this:

- The only `Seq[A]` we can create in this method, before we know what `A` is, is the empty sequence `Seq.empty[A]`.
- The identity element is one such that `x +: zero = Seq(x)`. Again this must be the empty sequence.

So we now we can fill in the answer.

```tut:book:silent
def reverse[A](seq: Seq[A]): Seq[A] = {
  seq.foldLeft(Seq.empty[A]){ (seq, elt) => elt +: seq }
}
```
</div>

*Map*

Write `map` in terms of `foldRight`.

<div class="solution">
Follow the same process as before: write out the type of the method we need to create, and fill in what we know. We start with `map` and `foldRight`.

```scala
def map[A, B](seq: Seq[A], f: A => B): Seq[B] = {
  seq.foldRight(???){ ??? }
}
```

As usual we need to fill in the zero element and the function. The zero element must have type `Seq[B]`, and the function has type `(A, Seq[B]) => Seq[B])`. The zero element is straightforward: `Seq.empty[B]` is the only sequence we can construct of type `Seq[B]`. For the function, we clearly have to convert that `A` to a `B` somehow. There is only one way to do that, which is with the function supplied to `map`. We then need to add that `B` to our `Seq[B]`, for which we can use the `+:` method. This gives us our final result.

```tut:book:silent
def map[A, B](seq: Seq[A], f: A => B): Seq[B] = {
  seq.foldRight(Seq.empty[B]){ (elt, seq) => f(elt) +: seq }
}
```
</div>

*Fold Left*

Write your own implementation of `foldLeft` that uses `foreach` and mutable state. Remember you can create a mutable variable using the `var` keyword, and assign a new value using `=`. For example

```tut:book
var mutable = 1

mutable = 2
```

<div class="solution">
Once again, write out the skeleton and then fill in the details using the types. We start with

```scala
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  seq.foreach { ??? }
}
```

Let's look at what we have need to fill in. `foreach` returns `Unit` but we need to return a `B`. `foreach` takes a function of type `A => Unit` but we only have a `(B, A) => B` available. The `A` can come from `foreach` and by now we know that the `B` is the intermediate result. We have the hint to use mutable state and we know that we need to keep a `B` around and return it, so let's fill that in.

```scala
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  var result: B = ???
  seq.foreach { (elt: A) => ??? }
  result
}
```

At this point we can just follow the types. `result` must be initially assigned to the value of `zero` as that is the only `B` we have. The body of the function we pass to `foreach` must call `f` with `result` and `elt`. This returns a `B` which we must store somewhere---the only place we have to store it is in `result`. So the final answer becomes

```tut:book:silent
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  var result = zero
  seq.foreach { elt => result = f(result, elt) }
  result
}
```
</div>

There are many other methods on sequences. Consult the [API documentation](http://www.scala-lang.org/api/current/scala/collection/Seq.html) for the `Seq` trait for more information.
