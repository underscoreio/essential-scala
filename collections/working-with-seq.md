---
layout: page
---

# Working with Sequences

In the [previous section](seq.html) with looked at the basic operations on sequences. Now we're going to look practical aspects of working with sequences: how to process every element of a sequence at once, and the performance characteristics of different sequence implementations.


## Bulk Processing of Elements

When working with sequences we often want to deal with the collection as a whole, rather than accessing and manipulating individual elements. For example, we might want to double every element of a sequence. In Java we'd write a loop to do this. In Scala we can do this more simply, using the `map` method. Map takes a function and applies this function to every element, creating a sequence containing the results. So to double every element we can write

{% highlight scala %}
scala> val sequence = Seq(1, 2, 3)
sequence: Seq[Int] = List(1, 2, 3)

scala> sequence.map(elt => elt * 2)
res0: Seq[Int] = List(2, 4, 6)
{% endhighlight %}

If we use the underscore function definition shorthand, we can write this even more compactly:

{% highlight scala %}
scala> sequence.map(_ * 2)
res1: Seq[Int] = List(2, 4, 6)
{% endhighlight %}

Given a sequence with type `Seq[A]`, the function we pass to `map` must have type `A => B` and we get a `Seq[B]` as a result. This isn't right for every situation. For example, suppose we have a sequence of strings, and we want to generate a sequence of all the permutations of those strings. We can call the `permutations` method on a string to get all permutations of it.

{% highlight scala %}
scala> "dog".permutations
res12: Iterator[String] = non-empty iterator
{% endhighlight %}

This returns an `Iterable`. We're going to look at Iterables in more detail later. For now all we need to know is tha we can call the `toList` method to convert an `Iterable` to a `List`.

{% highlight scala %}
scala> "dog".permutations.toList
res13: List[String] = List(dog, dgo, odg, ogd, gdo, god)
{% endhighlight %}

Thus we could write

{% highlight scala %}
scala> Seq("a", "wet", "dog").map(_.permutations.toList)
res14: Seq[List[String]] = List(List(a), List(wet, wte, ewt, etw, twe, tew), List(dog, dgo, odg, ogd, gdo, god))
{% endhighlight %}

but we end up with a sequence of sequences. Let's look at the types in more detail to see what's gone wrong

| Method | We have  | We provide | We get   |
|--------+----------+------------+----------|
| `map`  | `Seq[A]` | `A => B`   | `Seq[B]` |
|        | `Seq[String]` | `String => List[String]` | `Seq[List[String]]` |
| `???`  | `Seq[A]` | `A => Seq[B]` | `Seq[B]` |
|===========================================|

To answer to our mystery method `???` is `flatMap`. If we simply replace `map` with `flatMap` we get the answer we want.

{% highlight scala %}
scala> Seq("a", "wet", "dog").flatMap(_.permutations.toList)
res15: Seq[String] = List(a, wet, wte, ewt, etw, twe, tew, dog, dgo, odg, ogd, gdo, god)
{% endhighlight %}

### Finding and filtering

Another common task is to find elements matching a predicate.
