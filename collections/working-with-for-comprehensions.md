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

## Intermediate Results

val

## Ranges

## Monads
