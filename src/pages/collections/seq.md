## Sequences {#seq}

A *sequence* is a collection of items with a defined and stable order. Sequences are one of the most common data structures. In this section we're going to look at the basics of sequences: creating them, key methods on sequences, and the distinction between mutable and immutable sequences.

Here's how you create a sequence in Scala:

```tut:book
val sequence = Seq(1, 2, 3)
```

This immediately shows off a key feature of Scala's collections, the *separation between interface and implementation*. In the above, the value has type `Seq[Int]` but is implemented by a `List`.

### Basic operations

Sequences implement [many methods](http://docs.scala-lang.org/overviews/collections/seqs.html). Let's look at some of the most common.

#### Accessing elements

We can access the elements of a sequence using its `apply` method, which accepts an `Int` index as a parameter. Indices start from `0`.

```tut:book
sequence.apply(0)

sequence(0) // sugared syntax
```

An exception is raised if we use an index that is out of bounds:

```tut:book:fail:silent
sequence(3)
// java.lang.IndexOutOfBoundsException: 3
//        at ...
```

We can also access the head and tail of the sequence:

```tut:book
sequence.head

sequence.tail

sequence.tail.head
```

Again, trying to access an element that doesn't exist throws an exception:

```tut:book:fail:silent
Seq().head
// java.util.NoSuchElementException: head of empty list
//   at scala.collection.immutable.Nil$.head(List.scala:337)
//   ...

Seq().tail
// java.lang.UnsupportedOperationException: tail of empty list
//   at scala.collection.immutable.Nil$.tail(List.scala:339)
//   ...
```

If we want to safely get the `head` without risking an exception, we can use `headOption`:

```tut:book
sequence.headOption

Seq().headOption
```

The `Option` class here is Scala's built-in equivalent of our `Maybe` class from earlier. It has two subtypes---`Some` and `None`---representing the presence and absence of a value respectively.

### Sequence length

Finding the length of a sequence is straightforward:

```tut:book
sequence.length
```

### Searching for elements

There are a few ways of searching for elements. The `contains` method tells us whether a sequence contains an element (using `==` for comparison):

```tut:book
sequence.contains(2)
```

The `find` method is like a generalised version of `contains` - we provide a test function and the sequence returns the first item for which the test returns `true`:

```tut:book
sequence.find(_ == 3)

sequence.find(_ > 4)
```

The `filter` method is a variant of `find` that returns *all* the matching elements in the sequence:

```tut:book
sequence.filter(_ > 1)
```

### Sorting elements

We can use the `sortWith` method to sort a list using a binary function. The function takes two list items as parameters and returns `true` if they are in the correct order and `false` if they are the wrong way around. For example, to sort a list of `Ints` in descending order:

```tut:book
sequence.sortWith(_ > _)
```

### Appending/prepending elements

There are many ways to add elements to a sequence. We can append an element with the `:+` method:

```tut:book
sequence.:+(4)
```

It is more idiomatic to call `:+` as an infix operator:

```tut:book
sequence :+ 4
```

We can similarly *prepend* an element using the `+:` method:

```tut:book
sequence.+:(0)
```

Again, it is more idiomatic to call `+:` as an infix operator. Here *the trailing colon makes it right associative*, so we write the operator-style expression the other way around:

```tut:book
0 +: sequence
```

This is another of Scala's general syntax rules---any method ending with a `:` character becomes *right associative* when written as an infix operator. This rule is designed to replicate Haskell-style operators for things like list prepend (`::`) and list concatenation (`:::`). We'll look at this in more detail in a moment.

Finally we can concatenate entire sequences using the `++` method.

```tut:book
sequence ++ Seq(4, 5, 6)
```

<!--
### Updating elements

The `updated` method replaces the *nth* item in a sequence with a new value:

```tut:book
sequence.updated(0, 5)
```
-->

### Lists

The default implementation of `Seq` is a `List`, which is a classic [linked list](http://en.wikipedia.org/wiki/Linked_list) data structure similar to the one we developed in an earlier exercise. Some Scala libraries work specifically with `Lists` rather than using more generic types like `Seq`. For this reason we should familiarize ourselves with a couple of list-specific methods.

We can write an empty list using the singleton object `Nil`:

```tut:book
Nil
```

Longer lists can be created by prepending elements in classic linked-list style using the `::` method, which is equivalent to `+:`:

```tut:book
val list = 1 :: 2 :: 3 :: Nil

4 :: 5 :: list
```

We can also use the `List.apply` method for a more conventional constructor notation:

```tut:book
List(1, 2, 3)
```

Finally, the `:::` method is a right-associative `List`-specific version of `++`:

```tut:book
List(1, 2, 3) ::: List(4, 5, 6)
```

`::` and `:::` are specific to lists whereas `+:`, `:+` and `++` work on any type of sequence.

Lists have well known performance characteristics---constant-time prepend and head/tail operations and linear-time append and search operations. Other immutable sequences are available in Scala with different [performance characteristics](http://www.scala-lang.org/docu/files/collections-api/collections_40.html) to match all situations. It is up to us as developers to decide whether we want to tie our code to a specific sequence type like `List` or refer to our sequences as `Seqs` to simplify swapping implementations.


### Importing Collections and Other Libraries

The `Seq` and `List` types are so ubiquitous in Scala that they are made automatically available at all times. Other collections like `Stack` and `Queue` have to be brought into scope manually.

The main collections package is called `scala.collection.immutable`. We can import specific collections from this package as follows:

```tut:book:silent
import scala.collection.immutable.Vector
```

```tut:book
Vector(1, 2, 3)
```

We can also use *wildcard imports* to import everything in a package:

```tut:book:silent
import scala.collection.immutable._

```tut:book
Queue(1, 2, 3)
```

We can also use `import` to bring methods and fields into scope from a singleton:

```tut:book:silent
import scala.collection.immutable.Vector.apply
```

```tut:book
apply(1, 2, 3)
```

We can write import statements anywhere in our code---imported identifiers are lexically scoped to the block where we use them:

```tut:book:silent
// `empty` is unbound here

def someMethod = {
  import scala.collection.immutable.Vector.empty

  // `empty` is bound to `Vector.empty` here
  empty[Int]
}

// `empty` is unbound again here
```

<div class="callout callout-info">
#### Import Statements {-}

Import statements in Scala are very flexible. The main points are nicely described in the [Scala Wikibook](http://en.wikibooks.org/wiki/Scala/Import).
</div>

### Take Home Points

`Seq` is Scala's general sequence datatype. It has a number of general subtypes such as `List`, `Stack`, `Vector`, `Queue`, and `Array`, and specific subtypes such as `String`.

*The default sequences in Scala are immutable.* We also have access to mutable sequences, which are covered separately in the [Collections Redux](/collections-redux/index.html) chapter.

We have covered a variety of methods that operate on sequences. Here is a type table of everything we have seen so far:

+-------------+------------+---------------------+-------------+
| Method      | We have    | We provide          | We get      |
+=============+============+=====================+=============+
| `Seq(...)`  |            | `[A]`, ...          | `Seq[A]`    |
+-------------+------------+---------------------+-------------+
| `apply`     | `Seq[A]`   | `Int`               | `A`         |
+-------------+------------+---------------------+-------------+
| `head`      | `Seq[A]`   |                     | `A`         |
+-------------+------------+---------------------+-------------+
| `tail`      | `Seq[A]`   |                     | `Seq[A]`    |
+-------------+------------+---------------------+-------------+
| `length`    | `Seq[A]`   |                     | `Int`       |
+-------------+------------+---------------------+-------------+
| `contains`  | `Seq[A]`   | `A`                 | `Boolean`   |
+-------------+------------+---------------------+-------------+
| `find`      | `Seq[A]`   | `A => Boolean`      | `Option[A]` |
+-------------+------------+---------------------+-------------+
| `filter`    | `Seq[A]`   | `A => Boolean`      | `Seq[A]`    |
+-------------+------------+---------------------+-------------+
| `sortWith`  | `Seq[A]`   | `(A, A) => Boolean` | `Seq[A]`    |
+-------------+------------+---------------------+-------------+
| `:+`, `+:`  | `Seq[A]`   | `A`                 | `Seq[A]`    |
+-------------+------------+---------------------+-------------+
| `++`        | `Seq[A]`   | `Seq[A]`            | `Seq[A]`    |
+-------------+------------+---------------------+-------------+
| `::`        | `List[A]`  | `A`                 | `List[A]`   |
+-------------+------------+---------------------+-------------+
| `:::`       | `List[A]`  | `List[A]`           | `List[A]`   |
+-------------+------------+---------------------+-------------+


<!--
| `updated`   | `Seq[A]`   | `Int` `A`           | `Seq[A]`    |
-->

We can always use `Seq` and `List` in our code. Other collections can be brought into scope using the `import` statement as we have seen.

### Exercises

#### Documentation

Discovering Scala's collection classes is all about knowing how to read the API documentation. Look up the `Seq` and `List` types now and answer the following:

 - There is a synonym of `length` defined on `Seq`---what is it called?

 - There are two methods for retrieving the first item in a `List` --
   what are they called and how do they differ?

 - What method can be used to display the elements of the sequence as a string?

 - What method of `Option` can be used to determine whether the option contains a value?

**Tip:** There is a link to the Scala API documentation at [http://scala-lang.org](http://scala-lang.org).

<div class="solution">
The synonym for `length` is `size`.

The methods for retrieving the first element in a list are:
 - `head`      ---returns `A`, throwing an exception if the list is empty
 - `headOption`---returns `Option[A]`, returning `None` if the list is empty

The `mkString` method allows us to quickly display a `Seq` as a `String`:

```tut:book:silent
Seq(1, 2, 3).mkString(",")               // returns "1,2,3"
Seq(1, 2, 3).mkString("[ ", ", ", " ]") // returns "[ 1, 2, 3 ]"
```

`Options` contain two methods, `isDefined` and `isEmpty`, that we can use as a quick test:

```tut:book:silent
Some(123).isDefined // returns true
Some(123).isEmpty   // returns false
None.isDefined      // returns false
None.isEmpty        // returns true
```
</div>

#### Animals

Create a `Seq` containing the `String`s `"cat"`, `"dog"`, and `"penguin"`. Bind it to the name `animals`.

<div class="solution">
```tut:book
val animals = Seq("cat", "dog", "penguin")
```
</div>

Append the element `"tyrannosaurus"` to `animals` and prepend the element `"mouse"`.

<div class="solution">
```tut:book
"mouse" +: animals :+ "tyrannosaurus"
```
</div>

What happens if you prepend the `Int` `2` to `animals`? Why? Try it out... were you correct?

<div class="solution">
The returned sequence has type `Seq[Any]`.  It is perfectly valid to return a supertype (in this case `Seq[Any]`) from a non-destructive operation.

```scala
2 +: animals
```

You might expect a type error here, but Scala is capable of determining the least upper bound of `String` and `Int` and setting the type of the returned sequence accordingly.

In most real code appending an `Int` to a `Seq[String]` would be an error. In practice, the type annotations we place on methods and fields protect against this kind of type error, but be aware of this behaviour just in case.
</div>

#### Intranet Movie Database

Let's revisit our films and directors example from the [Classes](/classes) chapter.

The code below is a partial rewrite of the previous sample code in which `Films` are stored as a field of `Director` instead of the other way around. Copy and paste this into a new Scala worksheet and continue with the exercises below:

```tut:book:silent
case class Film(
  name: String,
  yearOfRelease: Int,
  imdbRating: Double)

case class Director(
  firstName: String,
  lastName: String,
  yearOfBirth: Int,
  films: Seq[Film])

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

val eastwood = new Director("Clint", "Eastwood", 1930,
  Seq(highPlainsDrifter, outlawJoseyWales, unforgiven, granTorino, invictus))

val mcTiernan = new Director("John", "McTiernan", 1951,
  Seq(predator, dieHard, huntForRedOctober, thomasCrownAffair))

val nolan = new Director("Christopher", "Nolan", 1970,
  Seq(memento, darkKnight, inception))

val someGuy = new Director("Just", "Some Guy", 1990,
  Seq())

val directors = Seq(eastwood, mcTiernan, nolan, someGuy)

// TODO: Write your code here!
```

Using this sample code, write implementations of the following methods:

 - Accept a parameter `numberOfFilms` of type `Int`---find all directors
   who have directed more than `numberOfFilms`:

   <div class="solution">
    We use `filter` because we are expecting more than one result:

```tut:book:silent
def directorsWithBackCatalogOfSize(numberOfFilms: Int): Seq[Director] =
 directors.filter(_.films.length > numberOfFilms)
```
   </div>

 - Accept a parameter `year` of type `Int`---find a director who was born
   before that year:

   <div class="solution">
   We use `find` because we are expecting at most one result. This solution
   will return the first director found who matches the criteria of the search:

```tut:book:silent
def directorBornBefore(year: Int): Option[Director] =
 directors.find(_.yearOfBirth < year)
```

   The `Option` type is discussed in more detail later this chapter.
   </div>

 - Accept two parameters, `year` and `numberOfFilms`, and return a list of directors
   who were born before `year` who have also directed more than than `numberOfFilms`:

   <div class="solution">
   This solution performs each part of the query separately and uses
   `filter` and `contains` to calculate the intersection of the results:

```tut:book:silent
def directorBornBeforeWithBackCatalogOfSize(year: Int, numberOfFilms: Int): Seq[Director] = {
 val byAge   = directors.filter(_.yearOfBirth < year)
 val byFilms = directors.filter(_.films.length > numberOfFilms)
 byAge.filter(byFilms.contains)
}
```
   </div>

 - Accept a parameter `ascending` of type `Boolean` that defaults to `true`. Sort the directors by age
   in the specified order:

   <div class="solution">
   Here is one solution. Note that sorting by ascending age is the same as sorting by descending year of birth:

```tut:book:silent
def directorsSortedByAge(ascending: Boolean = true) =
  if(ascending) {
    directors.sortWith((a, b) => a.yearOfBirth < b.yearOfBirth)
  } else {
    directors.sortWith((a, b) => a.yearOfBirth > b.yearOfBirth)
  }
```

   Because Scala is a functional language, we can also factor our code as follows:

```tut:book:silent
def directorsSortedByAge(ascending: Boolean = true) = {
  val comparator: (Director, Director) => Boolean =
    if(ascending) {
      (a, b) => a.yearOfBirth < b.yearOfBirth
    } else {
      (a, b) => a.yearOfBirth > b.yearOfBirth
    }

  directors.sortWith(comparator)
}
```

   Here is a final refactoring that is slightly less efficient because it rechecks
   the value of `ascending` multiple times.

```tut:book:silent
def directorsSortedByAge(ascending: Boolean = true) =
  directors.sortWith { (a, b) =>
    if(ascending) {
      a.yearOfBirth < b.yearOfBirth
    } else {
      a.yearOfBirth > b.yearOfBirth
    }
  }
```

   Note the use of braces instead of parentheses on the call to `sortWith` in the
   last example. We can use this syntax on any method call of one argument to give
   it a control-structure-like look and feel.
   </div>
