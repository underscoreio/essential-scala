## For Comprehensions

We've discussed the main collection transformation functions---`map`, `flatMap`, `foldLeft`, `foldRight`, and `foreach`---and seen that they provide a powerful way of working with collections. They can become unwieldy to work with when dealing with many collections or many nested transformations. Fortunately Scala has special syntax for working with collections (in fact any class that implements `map` and `flatMap`) that makes complicated operations simpler to write. This syntax is known as a _for comprehension_.

<div class="callout callout-info">
#### Not Your Father's For Loops {-}

_for comprehensions_ in Scala are very different to the C-style _for loops_ in Java. There is no direct equivalent of either language's syntax in the other.

</div>

Let's start with a simple example. Say we have the sequence `Seq(1, 2, 3)` and we wish to create a sequence with every element doubled. We know we can write

```scala mdoc
Seq(1, 2, 3).map(_ * 2)
```

The equivalent program written with a for comprehension is:

```scala mdoc
for {
  x <- Seq(1, 2, 3)
} yield x * 2
```

We call the expression containing the `<-` a _generator_, with a _pattern_ on the left hand side and a _generator expression_ on the right. A for comprehension iterates over the elements in the generator, binding each element to the pattern and calling the `yield` expression. It combines the yielded results into a sequence of the same type as the original generator.

In simple examples like this one we don't really see the power of for comprehensions---direct use of `map` and `flatMap` are often more compact in the simplest case. Let's try a more complicated example. Say we want to double all the numbers in `Seq(Seq(1), Seq(2, 3), Seq(4, 5, 6))` and return a flattened sequence of the results. To do this with `map` and `flatMap` we must nest calls:

```scala mdoc
val data = Seq(Seq(1), Seq(2, 3), Seq(4, 5, 6))

data.flatMap(_.map(_ * 2))
```

This is getting complicated. The equivalent for comprehension is much more ... comprehensible.

```scala mdoc
for {
  subseq  <- data
  element <- subseq
} yield element * 2
```

This gives us an idea of what the for comprehensions does. A general for comprehension:

```scala mdoc:invisible
val a: Seq[Int] = Seq.empty
val b: Seq[Int] = Seq.empty
val c: Seq[Int] = Seq.empty
val e: Int = 0
```

```scala mdoc:silent
for {
  x <- a
  y <- b
  z <- c
} yield e
```

translates to:

```scala mdoc:silent
a.flatMap(x => b.flatMap(y => c.map(z => e)))
```

The intuitive understanding of the code is to iterate through all of the sequences in the generators, mapping the `yield` expression over every element therein, and accumulating a result of the same type as sequence fed into the first generator.

Note that if we omit the `yield` keyword before the final expression, the overall type of the `for` comprehension becomes `Unit`. This version of the `for` comprehension is executed purely for its side-effects, and any result is ignored. Revisiting the doubling example from earlier, we can print the results instead of returning them:

```scala mdoc:silent
for {
  seq <- Seq(Seq(1), Seq(2, 3))
  elt <- seq
} println(elt * 2) // Note: no 'yield' keyword
// 2
// 4
// 6
```

The equivalent method calls use `flatMap` as usual and `foreach` in place of the final `map`:

```scala
a.flatMap(x => b.flatMap(y => c.foreach(z => e)))
```

We can use parentheses instead of braces to delimit the generators in a for loop. However, we must use semicolons to separate the generators if we do. Thus:

```scala mdoc:silent
for (
  x <- a;
  y <- b;
  z <- c
) yield e
```

is equivalent to:

```scala mdoc:silent
for {
  x <- a
  y <- b
  z <- c
} yield e
```

Some developers prefer to use parentheses when there is only one generator and braces otherwise:

```scala mdoc:silent
for(x <- Seq(1, 2, 3)) yield {
  x * 2
}
```

We can also use braces to wrap the yield expression and convert it to a _block_ as usual:

```scala
for {
  // ...
} yield {
  // ...
}
```

### Exercises

_(More) Heroes of the Silver Screen_

Repeat the following exercises from the previous section _without using `map` or `flatMap`_:

_Nolan Films_

List the names of the films directed by Christopher Nolan.

<div class="solution">
```scala mdoc:invisible
case class Film(name: String, imdbRating: Double)
case class Director(name: String, films: Seq[Film])
val nolan = Director("Christopher Nolan", Seq.empty)
val directors: Seq[Director] = Seq(nolan)
```

```scala mdoc:silent
for {
  film <- nolan.films
} yield film.name
```

</div>

_Cinephile_

List the names of all films by all directors.

<div class="solution">
```scala mdoc:silent
for {
  director <- directors
  film     <- director.films
} yield film.name
```
</div>

_High Score Table_

Find all films sorted by descending IMDB rating:

<div class="solution">
This one's a little trickier. We have to calculate the complete list of films first before sorting them with `sortWith`. Precedence rules require us to wrap the whole `for / yield` expression in parentheses to achieve this in one expression:

```scala mdoc:silent
(for {
  director <- directors
  film     <- director.films
} yield film).sortWith((a, b) => a.imdbRating > b.imdbRating)
```

Many developers prefer to use a temporary variable to make this code tidier:

```scala mdoc:silent
val films = for {
  director <- directors
  film     <- director.films
} yield film

films sortWith { (a, b) =>
  a.imdbRating > b.imdbRating
}
```

</div>

_Tonight's Listings_

Print the following for every film: `"Tonight only! FILM NAME by DIRECTOR!"`

<div class="solution">
We can drop the `yield` keyword from the `for` expression to achieve `foreach`-like semantics:

```scala mdoc:silent
for {
  director <- directors
  film     <- director.films
} println(s"Tonight! ${film.name} by ${director.name}!")
```

</div>
