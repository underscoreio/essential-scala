---
layout: page
title: Introducing For Comprehensions
---

We've discussed the main collection transformation functions -- `map`, `flatMap`, `foldLeft`, `foldRight`, and `foreach` -- and seen that they provide a powerful way of working with collections. Then can become unwiedly to work with when dealing with many collections of many nested transformations. Fortunately Scala has special syntax for working with collections (in fact any class that implements `map` and `flatMap`) that makes complicated operations simpler to write. This syntax is known as a *for comprehension*.

<div class="alert alert-warning">
**Important note:** *for comprehensions* in Scala are very different to the C-style *for loops* in Java. There is no direct equivalent of either language's syntax in the other.
</div>

Let's start with a simple example. Say we have the sequence `Seq(1, 2, 3)` and we wish to create a sequence with every element doubled. We know we can write

~~~ scala
scala> Seq(1, 2, 3).map(_ * 2)
res0: Seq[Int] = List(2, 4, 6)
~~~

The equivalent program written with a for comprehension is:

~~~ scala
scala> for {
     |   x <- Seq(1, 2, 3)
     | } yield x * 2
res1: Seq[Int] = List(2, 4, 6)
~~~

We call the expression containing the `<-` a *generator*, with a *pattern* on the left hand side and a *generator expression* on the right. A for comprehension iterates over the elements in the generator, binding each element to the pattern and calling the `yield` expression. It combines the yielded results into a sequence of the same type as the original generator.

In simple examples like this one we don't really see the power of for comprehensions -- direct use of `map` and `flatMap` are often more compact in the simplest case. Let's try a more complicated example instead. Say we want to double all the numbers in `Seq(Seq(1), Seq(2, 3), Seq(4, 5, 6))` and return a flattened sequence of the results. To do this with `map` and `flatMap` we must nest calls:

~~~ scala
scala> val data = Seq(Seq(1), Seq(2, 3), Seq(4, 5, 6))
data: Seq[Seq[Int]] = List(List(1), List(2, 3), List(4, 5, 6))

scala> data.flatMap(_.map(_ * 2))
res3: Seq[Int] = List(2, 4, 6, 8, 10, 12)
~~~

This is getting complicated. The equivalent for comprehension is much more... comprehensible:

~~~ scala
scala> for {
     |   subseq  <- data
     |   element <- subseq
     | } yield element * 2
res3: Seq[Int] = List(2, 4, 6, 8, 10, 12)
~~~

This gives us an idea of what the for comprehensions does. A general for comprehension:

~~~ scala
for {
  x <- a
  y <- b
  z <- c
} yield e
~~~

translates to:

~~~ scala
a.flatMap(x => b.flatMap(y => c.map(z => e)))
~~~

The intuitive understanding of the code is to iterate through all of the sequences in the generators, mapping the `yield` expression over every element therein, and accumulating a result of the same type as sequence fed into the first generator.

Note that if we omit the `yield` keyword the final expression, the overall type of the `for` comprehension becomes `Unit`. This version of the `for` comprehension is executed purely for its side-effects, and any result is ignored. Revisiting doubling example from earlier, we can print the results instead of returning them:

~~~ scala
scala> for {
     |  seq <- Seq(Seq(1), Seq(2, 3))
     |  elt <- seq
     |} println(elt * 2) // Note: no 'yield' keyword
2
4
6
~~~

The equivalent method calls use `flatMap` as usual and `foreach` in place of the final `map`:

~~~ scala
a.flatMap(x => b.flatMap(y => c.foreach(z => e)))
~~~

<div class="alert alert-info">
**Syntax tip:** We can use parentheses instead of braces to delimit the generators in a for loop. However, we must use semicolons to separate the generators if we do. Thus:

~~~ scala
for (
  x <- a;
  y <- b;
  z <- c;
) yield e
~~~

is equivalent to:

~~~ scala
for {
  x <- a
  y <- b
  z <- c
} yield e
~~~

Some developers prefer to use parentheses when there is only one generator and braces otherwise:

~~~ scala
for(x <- Seq(1, 2, 3)) yield {
  x * 2
}
~~~

We can also use braces to wrap the yield expression and convert it to a *block* as usual:

~~~ scala
for {
  // ...
} yield {
  // ...
}
~~~
</div>

## Exercises

Complete the following using for comprehensions

### Iteration

Create a sequence with containing every element of `Seq(2, 4, 6)` halved.

<div class="solution">
We did this exact exercise in a previous section. The solution method is to first recognise it is a `map`, which you can do by following the types. Then we must encode that as a for comprehension. The solution is

~~~ scala
for(x <- Seq(2, 4, 6)) yield x / 2
~~~
</div>

### Permutations

Create a sequence containing every permutation of the elements of `Seq("a", "wet", "dog")`. The result should be a `Seq[String]`, not `Seq[Seq[String]]`. Hint: `"foo".permutations.toSeq` will give you all the permutations of `"foo"` as a `Seq`.

<div class="solution">
You can follow the types to work out this is a `flatMap`, or you can recall this was an example used earlier.

~~~ scala
Seq("a", "wet", "dog") flatMap { x => x.permutations.toSeq }
~~~

Now we need to encode it as a for comprehension. The obvious solution is

~~~ scala
for(x <- Seq("a", "wet", "dog")) yield x.permutations.toSeq
~~~

but this doesn't work because it expands to a `map` not a `flatMap`. We need to add an extra level to the for-comprehension to get a `flatMap`.

~~~ scala
for {
  x <- Seq("a", "wet", "dog")
  p <- x.permutations.toSeq
} yield p
~~~
</div>

### Permutations, Again

As above, but return a `Seq[Seq[String]]`.

<div class="solution">
If you did the previous exercise you probably stumbled over the solution to this exercise. The difference is to use `map` instead of `flatMap`, which you can determine by looking at the result type of each. Thus the solution is

~~~ scala
for {
  x <- Seq("a", "wet", "dog")
} yield x.permutations.toSeq
~~~
</div>

### Summing

Sum the elements of `Seq(1, 2, 3)`.

<div class="solution">
You should recognise this as a fold. If not, the solution is, as always, to examine the types. `+` is a binary operation, and the only collection methods that accept binary operations are the folds.

The question is how to encode this using a for comprehension. For comprehensions only give us `map`, `flatMap`, and `foreach`. You might remember an earlier exercise where we wrote `foldLeft` using `foreach` and mutable state. The solution follows the same pattern.

~~~
var sum = 0
for(x <- Seq(1, 2, 3)) sum = sum + x
sum
~~~
</div>

### Folding

Write your own implementation of `foldLeft`.

<div class="solution">
The exercise follows on from the last one, where we implemented a fold. Now we just need generalise the pattern. Start, as usual by writing the skeleton with the types.

~~~
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  ???
}
~~~

Now need to fill in the body. As usual we follow the types. Let's look at what we have need to fill in. We know we need to use the for comprehension equivalent of `foreach`. `foreach` returns `Unit` but we need to return a `B`. `foreach` takes a function of type `A => Unit` but we only have a `(B, A) => B` available. The `A` can come from the for comprehension and by now we know that the `B` is the intermediate result. We have seen we need to use mutable state and we know that we need to keep a `B` around and return it, so let's fill that in.

~~~ scala
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  var result: B = ???
  for(x <- seq) { ??? }
  result
}
~~~

At this point we can just follow the types. `result` must be initially assigned to the value of `zero` as that is the only `B` we have. The body of the function we pass to `foreach` must call `f` with `result` and `elt`. This returns a `B` which we must store somewhere -- the only place we have to store it is in `result`. So the final answer becomes

~~~ scala
def foldLeft[A, B](seq: Seq[A], zero: B, f: (B, A) => B): B = {
  var result = zero
  for(x <- seq) { result = f(result, x) }
  result
}
~~~
</div>
