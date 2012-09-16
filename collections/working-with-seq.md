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
| `map`  | `Seq[String]` | `String => List[String]` | `Seq[List[String]]` |
| `???`  | `Seq[A]` | `A => Seq[B]` | `Seq[B]` |
|===========================================|

To answer to our mystery method `???` is `flatMap`. If we simply replace `map` with `flatMap` we get the answer we want.

{% highlight scala %}
scala> Seq("a", "wet", "dog").flatMap(_.permutations.toList)
res15: Seq[String] = List(a, wet, wte, ewt, etw, twe, tew, dog, dgo, odg, ogd, gdo, god)
{% endhighlight %}

Now let's look at another kind of operation. Say we have a `Seq[Int]` and we want to add all the numbers. The operation we want to provide is `+`, which is a binary function. Neither `map` nor `flatMap` will do, as they both expect a unary function. There is a further wrinkle: what result do we expect if the sequence is empty? Zero is a natural choice. Finally, `+` is associative, so the order we apply it doesn't matter, but in general we must specify an order (from lowest to highest index, or the reverse). Let's make another type table to see what we're looking for.

| Method | We have  | We provide | We get   |
|--------+----------+------------+----------|
| `???`  | `Seq[Int]` | `0` and `(Int, Int) => Int` | `Int` |
|===========================================|

The method that fills the bill is `fold`, and its ordered variants `foldLeft` and `foldRight`. The types are:

| Method | We have  | We provide | We get   |
|--------+----------+------------+----------|
| `fold`  | `Seq[A]` | `A` and `(A, A) => A` | `A` |
| `foldLeft` | `Seq[A]` | `B` and `(B, A) => B` | `B` |
| `foldRight` | `Seq[B]` | `B` and `(A, B) => B` | `B` |
|===========================================|

Given the sequence `Seq(1, 2, 3)`, `0`, and `+` the fold methods calculate the following:

| Method                         | Operations     | Notes
|--------------|
| `Seq(1, 2, 3).fold(0)(_ + _)`  | `1 + 2 + 3`    | Order of evaluation is not specified, nor is the inclusion of 0.
| `Seq(1, 2, 3).foldLeft(0)(_ + _)`  | `(((0 + 1) + 2) + 3)`    | Evaluation is left ot right
| `Seq(1, 2, 3).foldRight(0)(_ + _)`  | `(1 + (2 + (3 + 0)))`    | Evaluation is right to left
|===========================================|

The fold methods are very flexible. In fact we can write any transformation on a sequence in terms of fold! This is very deep result, and it goes beyond sequences. For any *algebraic datatype* there is a systematic process to define a fold that is a universal transformation for that datatype. We're not going to go deeper in this here, but be aware of it in your future study of functional programming.

There is one more traversal method that is commonly used: `foreach`. Unlike `map`, `flatMap` and the `fold`s, `foreach` is executed purely for its sideffects. The type table is:

| Method | We have  | We provide | We get   |
|--------+----------+------------+----------|
| `foreach` | `Seq[A]` | `A => Unit` | `Unit` |
|==========|

An example of when you'd use `foreach` is if you were printing the elements of a sequence.

### Algebra of transformations

We've seen the four major traversal functions, `map`, `flatMap`, `fold`, and `foreach`. It can be difficult to know which to use, but it turns out there is a simple way to decide: look at the types! The type table below gives the types for all the operations we've seen so far. To use it, start with the data you have (always a `Seq[A]` in the table below) and then look at the functions you have available and the result you want to obtain. The final column will tell you which method to use.

| We have  | We provide    | We want   | Method
|--------+----------+------------+------------|
| `Seq[A]` | `A => Unit`   | `Unit`   | `foreach` |
| `Seq[A]` | `A => B`      | `Seq[B]` | `map` |
| `Seq[A]` | `A => Seq[B]` | `Seq[B]` | `flatMap` |
| `Seq[A]` | `A1 >: A` and `(A1, A1) => A1` | `A1` | `fold` |
| `Seq[A]` | `B` and `(B, A) => B` | `B` | `foldLeft` |
| `Seq[A]` | `B` and `(A, B) => B` | `B` | `foldRight` |
|============|

## Exercises

1. Print every element of the sequence `Seq(1, 2, 3)`.
2. Multiply together all the elements of `Seq(1, 2, 3)`.
3. Write a function to find the smallest element of a `Seq[Int]`.
4. Given `Seq(1, 1, 2, 4, 3, 4)` create the sequence containing each number only once. Order is not important, so `Seq(1, 2, 4, 3)` or `Seq(4, 3, 2, 1)` are equally valid answers. Hint: Use `contains` to check if a sequence contains a value.
5. Write a function that reverses the elements of a sequence. Your output does not have to use the same concrete implementation as the input. Hint: use `foldLeft`.
6. Write `map` in terms of `foldRight`.
7. Write your own implementation of `foldRight` that uses `foreach` and mutable state.


## Other Useful Functions

There are many other useful methods defined on `Seq`. We've seen `contains` in the exercises above (you did do the exercises, right?) Similar functions are `filter` and `find`. Filter returns a sequence containing all the element that pass a test. For example, to get just the positive elements of sequence:

{% highlight scala %}
scala> Seq(-1, 1, 2, -2).filter(elt => elt > 0)
res16: Seq[Int] = List(1, 2)
{% endhighlight %}

Find finds the first element that matches a predicate. Since no element may match, find returns an `Option`.

{% highlight scala %}
scala> Seq(-1, 1, 2, -2).find(elt => elt > 0)
res17: Option[Int] = Some(1)

scala> Seq(-1, -2).find(elt => elt > 0)
res18: Option[Int] = None
{% endhighlight %}

There are many more methods on sequences. Consult the documentation for more.
