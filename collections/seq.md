---
layout: page
---

# Introducing Sequences

A sequence is a collection of items with a defined and stable order. Sequences are one of the most common data structures. In this section we're going to look at the basics of sequences: creating them, key methods on sequences, and the distinction between mutable and immutable sequences.

Here's how you create a sequence in Scala:

{% highlight scala %}
scala> val sequence = Seq(1, 2, 3)
sequence: Seq[Int] = List(1, 2, 3)
{% endhighlight %}

This immediately shows off a key feature of Scala's collections, the **separation between interface and implementation**. In the above, the value has type `Seq[Int]` but is implemented by a `List`.


## Basic Operations on Sequences

Sequences implement [many methods](http://docs.scala-lang.org/overviews/collections/seqs.html). Let's look at some of the most common.

### Accessing an element

Accessing an element is done by calling the `apply` method with an index. Indices start from 0. As you'd expect, the element at the index is returned.

{% highlight scala %}
scala> sequence.apply(0)
res0: Int = 1
{% endhighlight %}

We can also use the shortcut syntax for `apply`.

{% highlight scala %}
scala> sequence(0)
res1: Int = 1
{% endhighlight %}

An exception is raised if we use an index that is out of bounds.

{% highlight scala %}
scala> sequence(3)
java.lang.IndexOutOfBoundsException: 3
...
{% endhighlight %}

### Length of a sequence

Finding the length of a sequence is straightforward.

{% highlight scala %}
scala> sequence.length
res3: Int = 3
{% endhighlight %}

### Adding elements to a sequence

There are many ways to add elements to a sequence. We can prepend an element with the `+:` method.

{% highlight scala %}
scala> sequence.+:(0)
res4: Seq[Int] = List(0, 1, 2, 3)
{% endhighlight %}

It is more idiomatic to call `+:` as an operator, where the trailing colon makes it right associative.

{% highlight scala %}
scala> 0 +: sequence
res5: Seq[Int] = List(0, 1, 2, 3)
{% endhighlight %}

To append an element we use the `:+` method. Again it is more idiomatic to call `:+` as an operator.

{% highlight scala %}
scala> sequence.:+(4)
res6: Seq[Int] = List(1, 2, 3, 4)

scala> sequence :+ 4
res7: Seq[Int] = List(1, 2, 3, 4)
{% endhighlight %}

Finally we can append and prepend entire sequences using the `++` and `++:` methods.

{% highlight scala %}
scala> Seq(-2, -1, 0) ++: sequence
res8: Seq[Int] = List(-2, -1, 0, 1, 2, 3)

scala> sequence.++:(Seq(-2, -1, 0))
res9: Seq[Int] = List(-2, -1, 0, 1, 2, 3)

scala> sequence ++ Seq(4, 5, 6)
res10: Seq[Int] = List(1, 2, 3, 4, 5, 6)
{% endhighlight %}


## Mutable and Immutable Sequences

The astute will have noticed that none of the operations above modified our sequence. In fact we haven't even show a method to modify a `Seq` -- this is because there is none! Throughout the collections framework, the default implementations are immutable. Immutability makes reasoning about programs easier, and plays well with concurrency.

A sequence can be functionality updated. That is, we can create a copy with a single element changed.

{% highlight scala %}
scala> sequence.updated(0, 5)
res11: Seq[Int] = List(5, 2, 3)
{% endhighlight %}

Sometimes, however, mutable collections are desirable. Scala provides two parallel collections hierarchies, `scala.collection.mutable` and `scala.collection.immutable`. The default `Seq` is defined to be `scala.collection.immutable.Seq`. If we want a mutable sequence we can use `scala.collection.mutable.Seq`.

{% highlight scala %}
scala> val mutable = scala.collection.mutable.Seq(1, 2, 3)
mutable: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)
{% endhighlight %}

Note that the concrete implementation class is now an `ArrayBuffer` not a `List`.

In addition to all the methods on an immutable sequence, a mutable sequence can be updated using the `update` method. Note that `update` returns `Unit`, so no value is printed in the REPL after this call. When we print the original sequence we see it is changed.

{% highlight scala %}
scala> mutable.update(0, 5)

scala> mutable
res14: scala.collection.mutable.Seq[Int] = ArrayBuffer(5, 2, 3)
{% endhighlight %}

More idiomatic is to use the operator form of `update`.

{% highlight scala %}
scala> mutable(0) = 7

scala> mutable
res16: scala.collection.mutable.Seq[Int] = ArrayBuffer(7, 2, 3)
{% endhighlight %}
