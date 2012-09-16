---
layout: page
---

# For Comprehensions

We've discucssed the main collection transformation functions, `map`, `flatMap`, `fold`, and `foreach`, and seen that they provide a powerful way of working with collections. Then can become unwiedly to work with when dealing with many collections of many nested transformations. Scala has special syntax for working with collections (in fact any class that implements `map`, and `flatMap`) that makes complicated operations simpler to write. This syntax is known as a for comprehension.

Let's start with a simple example. Say we have the sequence `Seq(1, 2, 3)` and we wish to create a sequence with every element doubled. We know we can write

{% highlight scala %}
scala> Seq(1, 2, 3).map(_ * 2)
res0: Seq[Int] = List(2, 4, 6)
{% endhighlight %}

The equivalent program written with a for comprehension is

{% highlight scala %}
scala> for(x <- Seq(1, 2, 3)) yield x * 2
res1: Seq[Int] = List(2, 4, 6)
{% endhighlight %}

This example doesn't show the power of for comprehensions, so let's try a more complicated example. Say we want to double all the numbers in `Seq(Seq(1), Seq(2, 3), Seq(4, 5, 6))` and return a flattened sequence of numbers. To do this with `map` and `flatMap` we must nest calls:

{% highlight scala %}
scala> Seq(Seq(1), Seq(2, 3), Seq(4, 5, 6)).flatMap(_.map(_ * 2))
res2: Seq[Int] = List(2, 4, 6, 8, 10, 12)
{% endhighlight %}

This is getting complicated. The equivalent for comprehension is much more comprehensible.

{% highlight scala %}
scala> for {
  seq <- Seq(Seq(1), Seq(2, 3), Seq(4, 5, 6))
  elt <- seq
} yield elt * 2
res3: Seq[Int] = List(2, 4, 6, 8, 10, 12)
{% endhighlight %}

This gives us an idea of what the for comprehensions does. A for comprehsion

{% highlight scala %}
for {
  x <- a
  y <- b
  z <- c
} yield e
{% endhighlight %}

translates to

{% highlight scala %}
a.flatMap(x => b.flatMap(y => c.map(z => e)))
{% endhighlight %}

Recall that in Scala parentheses and braces are equivalent, except we must use semicolons to delimit expressions in the former. Thus

{% highlight scala %}
for {
  x <- a
  y <- b
  z <- c
} yield e
{% endhighlight %}

in the same as

{% highlight scala %}
for (
  x <- a;
  y <- b;
  z <- c;
) yield e
{% endhighlight %}

Conventional style is to use parentheses when there is only one expression, and to use braces otherwise.

Note if we omit the `yield` keyword, the final expression is executed for its side-effects, and any result is ignored. For example, to print the elements of a sequence

{% highlight scala %}
scala> for(x <- Seq(1, 2, 3)) println(x)
1
2
3
{% endhighlight %}

## Exercises

Complete the following using for comprehensions

1. Create a sequence with containing every element of `Seq(2, 4, 6)` halved.
1. Create a sequence containing every permutation of the elements of `Seq("a", "wet", "dog")`. The result should be a `Seq[String]`, not `Seq[Seq[String]]`. Hint: `"foo".permutations.toList` will give you all the permutations of `"foo"` as a list.
1. As above, but return a `Seq[Seq[String]]`.
1. Sum the elements of `Seq(1, 2, 3)`.
1. Write your own implementation of `foldLeft`.

## Additional Features of For Comprehensions

filter

val

## Ranges

## Monads
