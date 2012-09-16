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
