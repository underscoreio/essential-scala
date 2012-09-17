---
layout: page
---

# Maps and Sets

Up to now we've spent all of our time working with sequences. In this section we'll go through the two other most common collection types: `Maps` and `Sets`.

## Maps

A `Map` is very much like its counterpart in Java - it is a collection that maps *keys* to *values*. The keys must form a set and in most cases are unordered. Here is how to create a basic map:

{% highlight scala %}
scala> val example = Map("a" -> 1, "b" -> 2, "c" -> 3)
res0: scala.collection.immutable.Map[java.lang.String,Int] =
        Map(a -> 1, b -> 2, c -> 3)
{% endhighlight %}

The type of the resulting map is `Map[String,Int]`, meaning all the keys are type `String` and all the values are of type `Int`.

A quick aside on `->`. The constructor function for Map actually accepts an arbitrary number of `Tuple2` arguments. `->` is actually a function that generates a Tuple2:

{% highlight scala %}
scala> "a" -> 1
res1: (java.lang.String, Int) = (a,1)
{% endhighlight %}

Let's look at the most common operations on a Map.

### Accessing values using keys

The raison d'etre of a map is to convert keys to values. There are two main methods for doing this: `apply` and `get`:

{% highlight scala %}
scala> example.apply("a")
res2: Int = 1

scala> example.get("a")
res3: Option[Int] = Some(1)
{% endhighlight %}

`apply` attempts to look up a key and throws an exception if it is not found. By contrast, `get` returns an `Option`, forcing you to handle the not found case in your code:

{% highlight scala %}
scala> example.apply("d")
java.util.NoSuchElementException: key not found: d

scala> example.get("d")
res5: Option[Int] = None
{% endhighlight %}

Finally, the `getOrElse` method accepts a default value to return if the key is not found:

{% highlight scala %}
scala> example.getOrElse("d", -1)
res6: Int = -1
{% endhighlight %}


### Determining membership

The `contains` method determines whether a map contains a key:

{% highlight scala %}
scala> example.contains("a")
res7: Boolean = true
{% endhighlight %}

### Determining size

Finding the size of a map is just as easy as finding the length of a sequence:

{% highlight scala %}
scala> example.size
res8: Int = 3
{% endhighlight %}

### Adding and removing elements

As with `Seq`, the default implementation of `Map` is immutable. We add and remove elements by creating new maps as opposed to mutating existing ones.

We can add new elements using the `+` method. Note that, as with Java's `HashMap`, keys are overwritten and order is non-deterministic:

{% highlight scala %}
scala> example.+("c" -> 10, "d" -> 11, "e" -> 12)
res9: scala.collection.immutable.Map[java.lang.String,Int] =
        Map(e -> 12, a -> 1, b -> 2, c -> 10, d -> 11)
{% endhighlight %}

We can remove keys using the `-` method:

{% highlight scala %}
scala> example.-("b", "c")
res10: scala.collection.immutable.Map[java.lang.String,Int] =
        Map(a -> 1)
{% endhighlight %}

If we are only specifying a single argument, we can write `+` and `-` as infix operators.

{% highlight scala %}
scala> example + ("d" -> 4) - "c"
res11: scala.collection.immutable.Map[java.lang.String,Int] =
         Map(a -> 1, b -> 2, d -> 4)
{% endhighlight %}

Note that we still have to write the pair `"d" -> 4` in parentheses because `+` and `->` have the same precedence.

There are many other methods for manipulating immutable maps. For example, the `++` and `--` methods return the union and intersection of their arguments. See the [Scaladoc](file://localhost/usr/local/Cellar/scala/2.9.1/doc/scala-devel-docs/api/scala/collection/Map.html) for `Map` for more information.

### Mutable maps

The `scala.collection.mutable` package contains several mutable implementations of `Map`:

{% highlight scala %}
scala> val example2 = scala.collection.mutable.Map("x" -> 10, "y" -> 11, "z" -> 12)
example2: scala.collection.mutable.Map[java.lang.String,Int] =
            Map(x -> 10, z -> 12, y -> 11)
{% endhighlight %}

The in-place mutation equivalents of `+` and `-` are `+=` and `-=` respectively:

{% highlight scala %}
scala> example2 += ("x" -> 20)
res12: example2.type = Map(x -> 20, z -> 12, y -> 11)

scala> example2 -= ("y", "z")
res13: example2.type = Map(x -> 20)
{% endhighlight %}

Note that, like their immutable cousins, `+=` and `-=` both return a result of type `Map`. In this case, however, the return value is *the same object* that we called the method on. The return value is useful for chaining method calls together, but we can discard it if we see fit.

We can also use the `update` method, or its assignment-style syntactic-sugar, to update elements in the map:

{% highlight scala %}
scala> example2("w") = 30

scala> example2
res14: scala.collection.mutable.Map[java.lang.String,Int] = Map(x -> 20, w -> 30)
{% endhighlight %}

Note that, as with mutable sequences, `a(b) = c` is shorthand for `a.update(b, c)`. The `update` method does not return a value, but the map is mutated as a side-effect.

There are many other methods for manipulating mutable maps. See the [Scaladoc](file://localhost/usr/local/Cellar/scala/2.9.1/doc/scala-devel-docs/api/scala/collection/mutable/Map.html) for `scala.collection.mutable.Map` for more information.

### Sorted maps

The maps we have seen so far do not guarantee an ordering over their keys. For example, note that in this example, the order of keys in the resulting map is different from the order of addition operations:

{% highlight scala %}
scala> Map("a" -> 1) + ("b" -> 2) + ("c" -> 3) +
         ("d" -> 4) + ("e" -> 5)
res15: scala.collection.immutable.Map[java.lang.String,Int] =
         Map(e -> 5, a -> 1, b -> 2, c -> 3, d -> 4)
{% endhighlight %}

Scala also provides ordered immutable and mutable versions of a `ListMap` class that preserves the order in which keys are added:

{% highlight scala %}
scala> scala.collection.immutable.ListMap("a" -> 1) + ("b" -> 2) + ("c" -> 3) +
         ("d" -> 4) + ("e" -> 5)
res16: scala.collection.immutable.ListMap[java.lang.String,Int] =
         Map(a -> 1, b -> 2, c -> 3, d -> 4, e -> 5)
{% endhighlight %}

Scala's separation of interface and implementation means that the methods on ordered and unordered maps are almost identical, although their performance may vary. See [this useful page](http://docs.scala-lang.org/overviews/collections/performance-characteristics.html) for more information on the performance characteristics of the various types of collection.

### In summary

Here is a type table of all the methods we have seen so far:

|------------+------------+--------------------+-------------|
| Method     | We have    | We provide         | We get      |
|------------+------------+--------------------+-------------|
| `Map(...)` |            | `Tuple2[A,B]`, ... | `Map[A,B]`  |
| `apply`    | `Map[A,B]` | `A`                | `B`         |
| `get`      | `Map[A,B]` | `A`                | `Option[B]` |
| `+`        | `Map[A,B]` | `Tuple2[A,B]`, ... | `Map[A,B]`  |
| `-`        | `Map[A,B]` | `Tuple2[A,B]`, ... | `Map[A,B]`  |
| `++`       | `Map[A,B]` | `Map[A,B]`         | `Map[A,B]`  |
| `--`       | `Map[A,B]` | `Map[A,B]`         | `Map[A,B]`  |
| `contains` | `Map[A,B]` | `A`                | `Boolean`   |
| `size`     | `Map[A,B]` |                    | `Int`       |
|============================================================|

and the extras for mutable Sets:

|------------+------------+-------------------+-------------|
| Method     | We have    | We provide        | We get      |
|------------+------------+-------------------+-------------|
| `+=`       | `Map[A,B]` | `A`               | `Map[A,B]`  |
| `-=`       | `Map[A,B]` | `A`               | `Map[A,B]`  |
| `update`   | `Map[A,B]` | `A`, `B`          | `Unit`      |
|===========================================================|

## Sets

Sets are unordered collections that contain no duplicate elements. You can think of them as sequences without an order, or maps with keys and no values. Here is a type table of the most important methods:

|------------+----------+------------+-----------|
| Method     | We have  | We provide | We get    |
|------------+----------+------------+-----------|
| `+`        | `Set[A]` | `A`        | `Set[A]`  |
| `-`        | `Set[A]` | `A`        | `Set[A]`  |
| `++`       | `Set[A]` | `Set[A]`   | `Set[A]`  |
| `--`       | `Set[A]` | `Set[A]`   | `Set[A]`  |
| `contains` | `Set[A]` | `A`        | `Boolean` |
| `apply`    | `Set[A]` | `A`        | `Boolean` |
| `size`     | `Set[A]` |            | `Int`     |
|================================================|

and the extras for mutable Sets:

|------------+----------+------------+-----------|
| Method     | We have  | We provide | We get    |
|------------+----------+------------+-----------|
| `+=`       | `Set[A]` | `A`        | `Set[A]`  |
| `-=`       | `Set[A]` | `A`        | `Set[A]`  |
|================================================|
