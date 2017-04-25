## Maps and Sets

Up to now we've spent all of our time working with sequences. In this section we'll go through the two other most common collection types: `Maps` and `Sets`.

### Maps

A `Map` is very much like its counterpart in Java - it is a collection that maps *keys* to *values*. The keys must form a set and in most cases are unordered. Here is how to create a basic map:

```tut:book
val example = Map("a" -> 1, "b" -> 2, "c" -> 3)
```

The type of the resulting map is `Map[String,Int]`, meaning all the keys are type `String` and all the values are of type `Int`.

A quick aside on `->`. The constructor function for `Map` actually accepts an arbitrary number of `Tuple2` arguments. `->` is actually a function that generates a Tuple2.

```tut:book
"a" -> 1
```

Let's look at the most common operations on a map.

#### Accessing values using keys

The raison d'etre of a map is to convert keys to values. There are two main methods for doing this: `apply` and `get`.

```tut:book
example("a") // The same as example.apply("a")

example.get("a")
```

`apply` attempts to look up a key and throws an exception if it is not found. By contrast, `get` returns an `Option`, forcing you to handle the not found case in your code.

```tut:book:fail
example("d")
java.util.NoSuchElementException: key not found: d
```

```tut:book
example.get("d")
```

Finally, the `getOrElse` method accepts a default value to return if the key is not found.

```tut:book
example.getOrElse("d", -1)
```


#### Determining membership

The `contains` method determines whether a map contains a key.

```tut:book
example.contains("a")
```

#### Determining size

Finding the size of a map is just as easy as finding the size of a sequence.

```tut:book
example.size
```

#### Adding and removing elements

As with `Seq`, the default implementation of `Map` is immutable. We add and remove elements by creating new maps as opposed to mutating existing ones.

We can add new elements using the `+` method. Note that, as with Java's `HashMap`, keys are overwritten and order is non-deterministic.

```tut:book
example.+("c" -> 10, "d" -> 11, "e" -> 12)
```

We can remove keys using the `-` method:

```tut:book
example.-("b", "c")
```

If we are only specifying a single argument, we can write `+` and `-` as infix operators.

```tut:book
example + ("d" -> 4) - "c"
```

Note that we still have to write the pair `"d" -> 4` in parentheses because `+` and `->` have the same precedence.

There are many other methods for manipulating immutable maps. For example, the `++` and `--` methods return the union and intersection of their arguments. See the [Scaladoc](http://www.scala-lang.org/api/current/scala/collection/Map.html) for `Map` for more information.

#### Mutable maps

The `scala.collection.mutable` package contains several mutable implementations of `Map`:

```tut:book
val example2 = scala.collection.mutable.Map("x" -> 10, "y" -> 11, "z" -> 12)
```

The in-place mutation equivalents of `+` and `-` are `+=` and `-=` respectively.

```tut:book
example2 += ("x" -> 20)

example2 -= ("y", "z")
```

Note that, like their immutable cousins, `+=` and `-=` both return a result of type `Map`. In this case, however, the return value is *the same object* that we called the method on. The return value is useful for chaining method calls together, but we can discard it if we see fit.

We can also use the `update` method, or its assignment-style syntactic-sugar, to update elements in the map:

```tut:book:silent
example2("w") = 30
```

```tut:book
example2
```

Note that, as with mutable sequences, `a(b) = c` is shorthand for `a.update(b, c)`. The `update` method does not return a value, but the map is mutated as a side-effect.

There are many other methods for manipulating mutable maps. See the [Scaladoc](http://www.scala-lang.org/api/current#scala.collection.mutable.Map) for `scala.collection.mutable.Map` for more information.

#### Sorted maps

The maps we have seen so far do not guarantee an ordering over their keys. For example, note that in this example, the order of keys in the resulting map is different from the order of addition operations.

```tut:book
Map("a" -> 1) + ("b" -> 2) + ("c" -> 3) + ("d" -> 4) + ("e" -> 5)
```

Scala also provides ordered immutable and mutable versions of a `ListMap` class that preserves the order in which keys are added:

```tut:book
scala.collection.immutable.ListMap("a" -> 1) + ("b" -> 2) + ("c" -> 3) + ("d" -> 4) + ("e" -> 5)
```

Scala's separation of interface and implementation means that the methods on ordered and unordered maps are almost identical, although their performance may vary. See [this useful page](http://docs.scala-lang.org/overviews/collections/performance-characteristics.html) for more information on the performance characteristics of the various types of collection.

#### map and flatMap

Maps, like sequences, extend the `Traversable` trait, which means they inherit the standard `map` and `flatMap` methods. In fact, a `Map[A,B]` is a `Traversable[Tuple2[A,B]]`, which means that `map` and `flatMap` operate on instances of `Tuple2`.

Here is an example of `map`:

```tut:book
example.map(pair => pair._1 -> pair._2 * 2)
```

Note that the resulting object is also a `Map` as you might expect. However, what happens when the function we supply doesn't return a pair? What does `map` return then? Is it a compile error? Let's try it.

```tut:book
example.map(pair => pair._1 + " = " + pair._2)
```

It turns out the code does work, but we get back an `Iterable` result (look at the type, not the value)---a far more general data type.

Scala's collections framework is built in a clever (and complicated) way that always ensures you get something sensible back out of one of the standard operations like `map` and `flatMap`. We won't go into the details here (it's practically a training course in its own right). Suffice to say that you can normally guess using common sense (and judicious use of the REPL) the type of collection you will get back from any operation.

Here is a more complicated example using `flatMap`:

```tut:book
example.flatMap {
         case (str, num) =>
           (1 to 3).map(x => (str + x) -> (num * x))
       }
```

and the same example written using `for` syntax:

```tut:book
for{
         (str, num) <- example
          x         <- 1 to 3
       } yield (str + x) -> (num * x)
```

Note that the result is a `Map` again. The argument to `flatMap` returns a sequence of pairs, so in the end we are able to make a new `Map` from them. If our function returns a sequence of non-pairs, we get back a more generic data type.

```tut:book
for{
         (str, num) <- example
          x         <- 1 to 3
       } yield (x + str) + "=" + (x * num)
```

#### In summary

Here is a type table of all the methods we have seen so far:

+------------+------------+-------------------------------------------+---------------+
| Method     | We have    | We provide                                | We get        |
+============+============+===========================================+===============+
| `Map(...)` |            | `Tuple2[A,B]`, ...                        | `Map[A,B]`    |
+------------+------------+-------------------------------------------+---------------+
| `apply`    | `Map[A,B]` | `A`                                       | `B`           |
+------------+------------+-------------------------------------------+---------------+
| `get`      | `Map[A,B]` | `A`                                       | `Option[B]`   |
+------------+------------+-------------------------------------------+---------------+
| `+`        | `Map[A,B]` | `Tuple2[A,B]`, ...                        | `Map[A,B]`    |
+------------+------------+-------------------------------------------+---------------+
| `-`        | `Map[A,B]` | `Tuple2[A,B]`, ...                        | `Map[A,B]`    |
+------------+------------+-------------------------------------------+---------------+
| `++`       | `Map[A,B]` | `Map[A,B]`                                | `Map[A,B]`    |
+------------+------------+-------------------------------------------+---------------+
| `--`       | `Map[A,B]` | `Map[A,B]`                                | `Map[A,B]`    |
+------------+------------+-------------------------------------------+---------------+
| `contains` | `Map[A,B]` | `A`                                       | `Boolean`     |
+------------+------------+-------------------------------------------+---------------+
| `size`     | `Map[A,B]` |                                           | `Int`         |
+------------+------------+-------------------------------------------+---------------+
| `map`      | `Map[A,B]` | `Tuple2[A,B] => Tuple2[C,D]`              | `Map[C,D]`    |
+------------+------------+-------------------------------------------+---------------+
| `map`      | `Map[A,B]` | `Tuple2[A,B] => E`                        | `Iterable[E]` |
+------------+------------+-------------------------------------------+---------------+
| `flatMap`  | `Map[A,B]` | `Tuple2[A,B] => Traversable[Tuple2[C,D]]` | `Map[C,D]`    |
+------------+------------+-------------------------------------------+---------------+
| `flatMap`  | `Map[A,B]` | `Tuple2[A,B] => Traversable[E]`           | `Iterable[E]` |
+------------+------------+-------------------------------------------+---------------+


Here are the extras for mutable Sets:

+------------+------------+-------------------+-------------+
| Method     | We have    | We provide        | We get      |
+============+============+===================+=============+
| `+=`       | `Map[A,B]` | `A`               | `Map[A,B]`  |
+------------+------------+-------------------+-------------+
| `-=`       | `Map[A,B]` | `A`               | `Map[A,B]`  |
+------------+------------+-------------------+-------------+
| `update`   | `Map[A,B]` | `A`, `B`          | `Unit`      |
+------------+------------+-------------------+-------------+


### Sets

Sets are unordered collections that contain no duplicate elements. You can think of them as sequences without an order, or maps with keys and no values. Here is a type table of the most important methods:

+------------+----------+-----------------------+-----------+
| Method     | We have  | We provide            | We get    |
+============+==========+=======================+===========+
| `+`        | `Set[A]` | `A`                   | `Set[A]`  |
+------------+----------+-----------------------+-----------+
| `-`        | `Set[A]` | `A`                   | `Set[A]`  |
+------------+----------+-----------------------+-----------+
| `++`       | `Set[A]` | `Set[A]`              | `Set[A]`  |
+------------+----------+-----------------------+-----------+
| `--`       | `Set[A]` | `Set[A]`              | `Set[A]`  |
+------------+----------+-----------------------+-----------+
| `contains` | `Set[A]` | `A`                   | `Boolean` |
+------------+----------+-----------------------+-----------+
| `apply`    | `Set[A]` | `A`                   | `Boolean` |
+------------+----------+-----------------------+-----------+
| `size`     | `Set[A]` |                       | `Int`     |
+------------+----------+-----------------------+-----------+
| `map`      | `Set[A]` | `A => B`              | `Set[B]`  |
+------------+----------+-----------------------+-----------+
| `flatMap`  | `Set[A]` | `A => Traversable[B]` | `Set[B]`  |
+------------+----------+-----------------------+-----------+



and the extras for mutable Sets:

+------------+----------+------------+-----------+
| Method     | We have  | We provide | We get    |
+============+==========+============+===========+
| `+=`       | `Set[A]` | `A`        | `Set[A]`  |
+------------+----------+------------+-----------+
| `-=`       | `Set[A]` | `A`        | `Set[A]`  |
+------------+----------+------------+-----------+


### Exercises

#### Favorites

Copy and paste the following code into an editor:

```tut:book:silent
val people = Set(
  "Alice",
  "Bob",
  "Charlie",
  "Derek",
  "Edith",
  "Fred")

val ages = Map(
  "Alice"   -> 20,
  "Bob"     -> 30,
  "Charlie" -> 50,
  "Derek"   -> 40,
  "Edith"   -> 10,
  "Fred"    -> 60)

val favoriteColors = Map(
  "Bob"     -> "green",
  "Derek"   -> "magenta",
  "Fred"    -> "yellow")

val favoriteLolcats = Map(
  "Alice"   -> "Long Cat",
  "Charlie" -> "Ceiling Cat",
  "Edith"   -> "Cloud Cat")
```

Use the code as test data for the following exercises:

Write a method `favoriteColor` that accepts a person's name as a parameter and returns their favorite colour.

<div class="solution">
The person may or may not be a key in the `favoriteColors` map so the function should return an `Option` result:

```tut:book:silent
def favoriteColor(person: String): Option[String] =
  favoriteColors.get(person)
```
</div>

Update `favoriteColor` to return a person's favorite color *or* beige as a default.

<div class="solution">
Now we have a default value we can return a `String` instead of an `Option[String]`:

```tut:book:silent
def favoriteColor(person: String): String =
  favoriteColors.get(person).getOrElse("beige")
```
</div>

Write a method `printColors` that prints everyone's favorite color!

<div class="solution">
We can write this one using `foreach` or a for comprehension:

```tut:book:silent
def printColors() = for {
  person <- people
} println(s"${person}'s favorite color is ${favoriteColor(person)}!")
```

or:

```tut:book:silent
def printColors() = people foreach { person =>
  println(s"${person}'s favorite color is ${favoriteColor(person)}!")
}
```
</div>

Write a method `lookup` that accepts a name and one of the maps and returns the relevant value from the map. Ensure that the return type of the method matches the value type of the map.

<div class="solution">
Here we write a generic method using a type parameter:

```tut:book:silent
def lookup[A](name: String, values: Map[String, A]) =
  values get name
```
</div>

Calculate the color of the oldest person:

<div class="solution">
First we find the oldest person, then we look up the answer:

```tut:book:silent
val oldest: Option[String] =
  people.foldLeft(Option.empty[String]) { (older, person) =>
    if(ages.getOrElse(person, 0) > older.flatMap(ages.get).getOrElse(0)) {
      Some(person)
    } else {
      older
    }
  }

val favorite: Option[String] =
  for {
    oldest <- oldest
    color  <- favoriteColors.get(oldest)
  } yield color
```
</div>

### Do-It-Yourself Part 2

Now we have some practice with maps and sets let's see if we can implement some useful library functions for ourselves.

#### Union of Sets

Write a method that takes two sets and returns a set containing the union of the elements. Use iteration, like `map` or `foldLeft`, not the built-in `union` method to do so!

<div class="solution">
As always, start by writing out the types and then follow the types to fill-in the details.

```tut:book:silent
def union[A](set1: Set[A], set2: Set[A]): Set[A] = {
  ???
}
```

We need to think of an algorithm for computing the union. We can start with one of the sets and add the elements from the other set to it. The result will be the union. What types does this result in? Our result has type `Set[A]` and we need to add every `A` from the two sets to our result, which is an operation with type `(Set[A], A) => Set[A]`. This means we need a fold. Since order is not important any fold will do.

```tut:book:silent
def union[A](set1: Set[A], set2: Set[A]): Set[A] = {
  set1.foldLeft(set2){ (set, elt) => (set + elt) }
}
```
</div>

#### Union of Maps

Now let's write union for maps. Assume we have two `Map[A, Int]` and add corresponding elements in the two maps. So the union of `Map('a' -> 1, 'b' -> 2)` and `Map('a' -> 2, 'b' -> 4)` should be `Map('a' -> 3, 'b' -> 6)`.

<div class="solution">
The solution follows the same pattern as the union for sets, but here we have to handle adding the values as well.

```tut:book:silent
def union[A](map1: Map[A, Int], map2: Map[A, Int]): Map[A, Int] = {
  map1.foldLeft(map2){ (map, elt) =>
    val (key, value1) = elt
    val value2 = map.get(key)
    val total = value1 + value2.getOrElse(0)
    map + (key -> total)
  }
}
```
</div>

#### Generic Union

There are many things that can be added, such as strings (string concatenation), sets (union), and of course numbers. It would be nice if we could generalise our `union` method on maps to handle anything for which a sensible `add` operation can be defined. How can we go about doing this?

<div class="solution">
With the tools we've seen far, we could add another function parameter like so:

```tut:book:silent
def union[A, B](map1: Map[A, B], map2: Map[A, B], add: (B, B) => B): Map[A, B] = {
  map1.foldLeft(map2){ (map, elt) =>
    val (k, v) = elt
    val newV = map.get(k).map(v2 => add(v, v2)).getOrElse(v)
    map + (k -> newV)
  }
}
```

Later we'll see a nicer way to do this using type classes.
</div>
