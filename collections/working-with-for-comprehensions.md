---
layout: page
---

# Working with For Comprehensions

In the [previous section](for-comprehensions.html) we looked at the fundamentals of for comprehensions. In this section we're going to looking at some handy additional features in for comprehensions, the `Range` sequence type which is allows us to use for comprehensions like traditional for loops in Java, and finally we'll briefly look at the relationship between for comprehensions and monads.

## Filtering and `vals` in Comprehensions

It's quite common to only process selected elements. We can do this with comprehensions by adding an `if` clause after the generator expression. So to process only the positive elements of sequence we could write

{% highlight scala %}
scala> for(x <- Seq(-2, -1, 0, 1, 2) if x > 0) yield x
res0: Seq[Int] = List(1, 2)
{% endhighlight %}
