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

The `Option` class here is Scala's built-in equivalent of our `Maybe` class from earlier. It has two subtypes -- `Some` and `None` -- representing the presence and absence of a value respectively.

The `filter` method is a variant of `find` that returns *all* the matching elements in the sequence:

~~~ scala
scala> sequence.filter(_ > 1)
res7: Seq[Int] = List(2, 3)
~~~

### Sorting elements

We can use the `sortWith` method to sort a list using a binary function. The function takes two list items as parameters and returns `true` if they are in the correct order and `false` if they are the wrong way around:

~~~ scala
scala> sequence.sortWith(_ < _)
res 8: Seq[Int] = List(3, 2, 1)
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

Another of Scala's general syntax rules -- any method ending with a `:` character becomes **right associative** when written as an infix operator. This rule is designed to replicate Haskell-style operators for things like list prepend (`::`) and list concatenation (`:::`). We'll look at this in more detail in a moment.

{% comment %}
### Updating elements

The `updated` method replaces the *nth* item in a sequence with a new value:

~~~ scala
scala> sequence.updated(0, 5)
res11: Seq[Int] = List(5, 2, 3)
~~~
{% endcomment %}

## Lists

The default implementation of `Seq` is a `List`, which is a classic [linked list](http://en.wikipedia.org/wiki/Linked_list) data structure similar to the one we developed in an earlier exercise. Some Scala libraries work specifically with `Lists` rather than using more generic types like `Seq`. For this reason we should familiarize ourselves with a couple of list-specific methods.

We can write an empty list using the singleton object `Nil`:

~~~ scala
scala> Nil
res0: scala.collection.immutable.Nil.type = List()
~~~

Longer lists can be created by prepending elements in classic linked-list style using the `::` method, which is equivalent to `+:`:

~~~ scala
scala> 1 :: 2 :: 3 :: Nil
res1: List[Int] = List(1, 2, 3)

scala> 4 :: 5 :: res1
res1: List[Int] = List(4, 5, 1, 2, 3)
~~~

We can also use the `List.apply` method for a more conventional constructor notation:

~~~ scala
scala> List(1, 2, 3)
res2: List[Int] = List(1, 2, 3)
~~~

Finally, the `:::` method is a right-associative `List`-specific version of `++`:

~~~ scala
scala> List(1, 2, 3) ::: List(4, 5, 6)
res3: List[Int] = List(1, 2, 3, 4, 5, 6)
~~~

`::` and `:::` are specific to lists whereas `+:`, `:+` and `++` work on any type of sequence.

Lists have well known performance characteristics -- constant-time prepend and head/tail operations and linear-time append and search operations. Other immutable sequences are available in Scala with different [performance characteristics](http://www.scala-lang.org/docu/files/collections-api/collections_40.html) to match all situations. It is up to us as developers to decide whether we want to tie our code to a specific sequence type like `List` or refer to our sequences as `Seqs` to simplify swapping implementations.


## Importing Collections and Other Libraries

The `Seq` and `List` types so ubiquitous in Scala that they are made automatically available at all times. Other collections like `Vector` and `Queue` have to be brought into scope manually.

The main collections package is called `scala.collection.immutable`. We can import specific collections from this package as follows:

~~~ scala
scala> import scala.collection.immutable.Vector
import scala.collection.immutable.Vector

scala> Vector(1, 2, 3)
res1: scala.collection.immutable.Vector[Int] = Vector(1, 2, 3)
~~~

We can also use **wildcard imports** to import everything in a package:

~~~ scala
scala> import scala.collection.immutable._
import scala.collection.immutable._

scala> Queue(1, 2, 3)
res2: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3)
~~~

We can also use `import` to bring methods and fields into scope from a singleton:

~~~ scala
scala> import scala.collection.immutable.Vector._
import scala.collection.immutable.Vector.empty

scala> apply(1, 2, 3)
res3: scala.collection.immutable.Vector[Int] = Vector(1, 2, 3)
~~~

We can write import statements anywhere in our code -- impoted identifiers are scoped lexically to the block where we use them:

~~~ scala
// `empty` is unbound here

def someMethod = {
  import scala.collection.immutable.Vector.empty

  // `empty` is bound to `Vector.empty` here
  empty[Int]
}

// `empty` is unbound again here
~~~

<div class="alert alert-info">
**Java tip:** Import statements are significantly more flexible in Scala than Java. The main differences are described nicely in the [Scala Wikibook](http://en.wikibooks.org/wiki/Scala/Import).
</div>

## Take Home Points

`Seq` is Scala's general sequence datatype. It has a number of general subtypes such as `List`, `Stack`, `Vector`, `Queue`, and `Array`, and specific subtypes such as `String`.

**The default sequences in Scala are immutable.** We also have access to mutable sequences, which are covered separately in the [Collections Redux](/collections-redux/index.html) chapter.

We have covered a variety of methods that operate on sequences. Here is a type table of everything we have seen so far:

|-------------+------------+---------------------+-------------|
| Method      | We have    | We provide          | We get      |
|-------------+------------+---------------------+-------------|
| `Seq(...)`  |            | `[A]`, ...          | `Seq[A]`    |
| `apply`     | `Seq[A]`   | `Int`               | `A`         |
| `length`    | `Seq[A]`   |                     | `Int`       |
| `contains`  | `Seq[A]`   | `A`                 | `Boolean`   |
| `find`      | `Seq[A]`   | `A => Boolean`      | `Option[A]` |
| `filter`    | `Seq[A]`   | `A`                 | `Seq[A]`    |
| `sortWith`  | `Seq[A]`   | `(A, A) => Boolean` | `Seq[A]`    |
| `:+`, `+:`  | `Seq[A]`   | `A`                 | `Seq[A]`    |
| `++`        | `Seq[A]`   | `Seq[A]`            | `Seq[A]`    |
| `::`        | `List[A]`  | `A`                 | `List[A]`   |
| `:::`       | `List[A]`  | `List[A]`           | `List[A]`   |
|==============================================================|
{: .table .table-bordered .table-responsive }

{% comment %}
| `updated`   | `Seq[A]`   | `Int` `A`           | `Seq[A]`    |
{% endcomment %}

We can always use `Seq` and `List` in our code. Other collections can be brought into scope using the `import` statement. This has a number of features that aren't present in Java -- it can be used to import methods from objects, and be written anywhere in our code.

## Exercises

### Documentation

Discovering Scala's collection classes is all about knowing how to read the API documentation. Look up the `Seq` and `List` types now and answer the follo

 - There is a synonym of `length` defined on `Seq` -- what is it called?

 - There are two methods for retrieving the first item in a `List` --
   what are they called and how do they differ?

 - What method can be used used to display the elements of the sequence as a string?

 - What method of `Option` can be used to determine whether the option contains a value?

**Tip:** There is a link to the Scala API documentation in the left-hand menu of the course notes.

<div class="solution">
The synonym for `length` is `size`.

The methods for retrieving the first element in a list are:
 - `head`       -- returns `A`, throwing an exception if the list is empty
 - `headOption` -- returns `Option[A]`, returning `None` if the list is empty

The `mkString` method allows us to quickly display a `Seq` as a `String`:

~~~ scala
Seq(1, 2, 3).mkString(",")               // returns "1,2,3"
Seq(1, 2, 3).mkString("[ ", ", ", " ]"") // returns "[ 1, 2, 3 ]"
~~~

`Options` contain two methods, `isDefined` and `isEmpty`, that we can use as a quick test:

~~~ scala
Some(123).isDefined // returns true
Some(123).isEMpty   // returns false
None.isDefined      // returns false
None.isEMpty        // returns true
~~~
</div>

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

### Intranet Movie Database

Let's revisit our films and directors example from the [Classes](/classes) chapter.

The code below is a partial rewrite of the previous sample code in which `Films` are stored as a field of `Director` instead of the other way around. Copy and paste this into a new Scala worksheet and continue with the exercises below:

~~~ scala
class Film(
  name: String,
  yearOfRelease: Int,
  imdbRating: Double)

object Film {
  def newer(film1: Film, film2: Film): Film =
    if (film1.yearOfRelease < film2.yearOfRelease) film1 else film2

  def highestRating(film1: Film, film2: Film): Double = {
    val rating1 = film1.imdbRating
    val rating2 = film2.imdbRating
    if (rating1 > rating2) rating1 else rating2
  }

  def oldestDirectorAtTheTime(film1: Film, film2: Film): Director =
    if (film1.directorsAge > film2.directorsAge) film1.director else film2.director
}

case class Director(
  firstName: String,
  lastName: String,
  yearOfBirth: Int,
  films: Seq[Film]) {

  def ageAtRelease(film: Film) =
    film.yearOfRelease - director.yearOfBirth

  def name: String =
    s"$firstName $lastName"
}

object Director {
  def older(director1: Director, director2: Director): Director =
    if (director1.yearOfBirth < director2.yearOfBirth) director1 else director2
}

val memento           = new Film("Memento", 2000, 8.5)
val darkKnight        = new Film("Dark Knight", 2008, 9.0)
val inception         = new Film("Inception", 2010, 8.8)

val highPlainsDrifter = new Film("High Plains Drifter", 1973, 7.7)
val outlawJoseyWales  = new Film("The Outlaw Josey Wales", 1976, 7.9)
val unforgiven        = new Film("Unforgiven", 1992, 8.3)
val granTorino        = new Film("Gran Torino", 2008, 8.2)
val invictus          = new Film("Invictus", 2009, 7.4)

val predator          = new Film("Predator", 1987, 7.9)
val dieHard           = new Film("Die Hard", 1988, 8.3)
val huntForRedOctober = new Film("The Hunt for Red October", 1990, 7.6)
val thomasCrownAffair = new Film("The Thomas Crown Affair", 1999, 6.8)

val clintEastwood = new Director("Clint", "Eastwood", 1930,
  Seq(highPlainsDrifter, outlawJoseyWales, unforgiven, granTorino, invictus))

val johnMcTiernan = new Director("John", "McTiernan", 1951,
  Seq(predator, dieHard, huntForRedOctober, thomasCrownAffair))

val christopherNolan = new Director("Christopher", "Nolan", 1970,
  Seq(memento, darkKnight, inception))

val someGuy = new Director("Just”, "Some Guy”, 1990,
  Seq())

val directors = Seq(clintEastwood, johnMcTiernan, christopherNolan, someGuy)

// TODO: Write your code here!
~~~

Write code in `FilmBuff` to do the following:

 - Find a director who born before 1950.

   <div class="solution">
   ~~~ scala
   directors.find(_.yearOfBirth < 1950)
   // returns Some(clintEastwood)
   ~~~
   </div>

 - Fetch a list of all directors who have directed more than 3 films
   (according to the database).

   <div class="solution">
   ~~~ scala
   directors.filter(_.films.length > 3)
   // returns Seq(clintEastwood, johnMcTiernan)
   ~~~
   </div>

 - Fetch a list of all directors who have directed a film when they
   were over 45 years old (hint: use `Director.ageAtRelease`).

   <div class="solution">
   ~~~ scala
   directors.filter(d => d.films.find(f => d.ageAtRelease(f) > 45).isDefined)
   // returns Seq(clintEastwood)
   ~~~
   </div>
