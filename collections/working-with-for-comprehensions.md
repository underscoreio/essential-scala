---
layout: page
---

# Working with For Comprehensions

In the [previous section](for-comprehensions.html) we looked at the fundamentals of for comprehensions. In this section we're going to looking at some handy additional features in for comprehensions, the `Range` sequence type which is allows us to use for comprehensions like traditional for loops in Java, and finally we'll briefly look at the relationship between for comprehensions and monads.


## Filtering

It's quite common to only process selected elements. We can do this with comprehensions by adding an `if` clause after the generator expression. So to process only the positive elements of sequence we could write

{% highlight scala %}
scala> for(x <- Seq(-2, -1, 0, 1, 2) if x > 0) yield x
res0: Seq[Int] = List(1, 2)
{% endhighlight %}

The code is converted to a `withFilter` call, or if that doesn't exist to `filter`.


## Parallel Iteration

Another common problem is to iterate over two or more collections in parallel. For example, say we have the sequences `Seq(1, 2, 3)` and `Seq(4, 5, 6)` and we want to add together elements with the same index yielding `Seq(5, 7 , 9)`. If we write

{% highlight scala %}
scala> for {
  x <- Seq(1, 2, 3)
  y <- Seq(4, 5, 6)
} yield x + y
res1: Seq[Int] = List(5, 6, 7, 6, 7, 8, 7, 8, 9)
{% endhighlight %}

we see that iterations are nested. We traverse the first element from the first sequence and then all the elements of the second sequence, then the second element from the first sequence and so on.

The solution is to `zip` together the two sequences, giving a sequence containing pairs of corresponding elements

{% highlight scala %}
scala> Seq(1, 2, 3).zip(Seq(4, 5, 6))
res2: Seq[(Int, Int)] = List((1,4), (2,5), (3,6))
{% endhighlight %}

With this we can easily compute the result we wanted

{% highlight scala %}
scala> for(x <- Seq(1, 2, 3).zip(Seq(4, 5, 6))) yield { val (a, b) = x; a + b }
res3: Seq[Int] = List(5, 7, 9)
{% endhighlight %}

Sometimes you want to iterate over the values in sequence and their indices. For this case the `zipWithIndex` method is provided.

{% highlight scala %}
scala> for(x <- Seq(1, 2, 3).zipWithIndex) yield x
res4: Seq[(Int, Int)] = List((1,0), (2,1), (3,2))
{% endhighlight %}

Finally note that `zip` and `zipWithIndex` are available on all collection classes, including `Map` and `Set`.


## Pattern Matching

The pattern on the left hand side of a generator is not named accidentally. We can include any pattern there and only process results matching the pattern. This provides another way of filtering results. So instead of

{% highlight scala %}
scala> for(x <- Seq(1, 2, 3).zip(Seq(4, 5, 6))) yield { val (a, b) = x; a + b }
res3: Seq[Int] = List(5, 7, 9)
{% endhighlight %}

we can write

{% highlight scala %}
scala> for((a, b) <- Seq(1, 2, 3).zip(Seq(4, 5, 6))) yield a + b
res6: Seq[Int] = List(5, 7, 9)
{% endhighlight %}


## Intermediate Results

It is often useful to create an intermediate result within a sequence of generators. We can do this by inserting a `val` expression like so

{% highlight scala %}
scala> for {
  x <- Seq(1, 2, 3)
  val square = x * x
  y <- Seq(4, 5, 6)
} yield square * y
res8: Seq[Int] = List(4, 5, 6, 16, 20, 24, 36, 45, 54)
{% endhighlight %}


## Ranges

So far we've seen lots of ways to iterate over sequences but not much in the way of iterating over numbers. In Java and other languages it is common to write code like

{% highlight java %}
for(i = 0; i < array.length; i++) {
  doSomething(array[i])
}
{% endhighlight %}

We've seen that for comprehensions provide a succinct way of implementing these programs. But what about classics like this?

{% highlight java %}
for(i = 99; i > 0; i--) {
  System.out.println(i + "bottles of beer on the wall!")
  // Full text omitted for the sake of brevity
}
{% endhighlight %}

Scala provides the `Range` class for these occasions. A `Range` represents a sequence of integers from some starting value to less than the end value with a non-zero step. We can construct a `Range` using the `until` method on `Int`.

{% highlight scala %}
scala> 1 until 10
res9: scala.collection.immutable.Range = Range(1, 2, 3, 4, 5, 6, 7, 8, 9)
{% endhighlight %}

By default the step size is 1, so trying to go from high to low gives us an empty `Range`.

{% highlight scala %}
scala> 10 until 1
res10: scala.collection.immutable.Range = Range()
{% endhighlight %}

We can rectify this by specifying a different step, using the `by` method on `Range`.

{% highlight scala %}
scala> 10 until 1 by -1
res11: scala.collection.immutable.Range = Range(10, 9, 8, 7, 6, 5, 4, 3, 2)
{% endhighlight %}

Now we can write the Scala equivalent of our Java program.

{% highlight scala %}
scala> for(i <- 99 until 0 by -1) println(i + " bottles of beer on the wall!")
99 bottles of beer on the wall!
98 bottles of beer on the wall!
97 bottles of beer on the wall!
// etc ...
{% endhighlight %}

This gives us a hint of the power of ranges. Since they are sequences we can combine them with other sequences in interesting ways. For example, to create a range with a gap in the middle we can concatenate two ranges:

{% highlight scala %}
scala> (1 until 10) ++ (20 until 30)
res12: scala.collection.immutable.IndexedSeq[Int] = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29)
{% endhighlight %}

Note that the result is a `Vector` not a `Range` but this doesn't matter. As they are both sequences we can use both them in a for comprehension without any code change!

## Exercises

Solve the following exercises, taken from [Project Euler](http://projecteuler.net), using for comprehensions.

1. If we list all the natural numbers below 10 that are multiples of 3 or 5, we get 3, 5, 6 and 9. The sum of these multiples is 23. Find the sum of all the multiples of 3 or 5 below 1000.

1. Each new term in the Fibonacci sequence is generated by adding the previous two terms. By starting with 1 and 2, the first 10 terms will be:
   1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...
By considering the terms in the Fibonacci sequence whose values do not exceed four million, find the sum of the even-valued terms.


## Monads

We've seen that by implementing a few methods (`map`, `flatMap`, and optionally `filter` and `foreach`) we can use any class with a for comprehension, and that this is an incredibly useful abstraction. This abstraction is called a monad and has extremely wide application. For example, suppose we have a number of computations that could fail. Let's model this by having them return an `Option`, with `None` indicating failure. (If you were doing this for real you'd want some information on why it failed. Look at `Either` or `Try`, the later being Scala 2.10 and above.) We want to sequence our computations but stop as soon as one fails.

We could write

{% highlight scala %}
computationOne() match {
  case Some(x) =>
    computationTwo(x) match {
      case Some(y) => ...
      case None => None
    }
  case None => None
}
{% endhighlight %}

Or we could write

{% highlight scala %}
computationOne() flatMap {
  x => computationTwo(x) flatMap {
    y => ...
  }
}
{% endhighlight %}

Both are rather cumbersome. With a for comprehension we can simply write

{% highlight scala %}
for {
  x <- computationOne()
  y <- computationTwo(x)
  ...
} yield ...
{% endhighlight %}

Here we've done something completely different to our previous examples using collections. Yet the same basic abstraction applies! The more you look the more examples of monads you'll find, and for comprehensions give us a powerful and generic way to work with them.
