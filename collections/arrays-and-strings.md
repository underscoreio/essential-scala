---
layout: page
---

# Arrays and Strings

Arrays and strings in Scala correspond to Java's arrays and strings.

{% highlight scala %}
scala> "this is not a string"
res0: java.lang.String = this is not a string
{% endhighlight %}

Yet all the familiar collection methods are available on them.

{% highlight scala %}
scala> "is it true?".map(elt => true)
res1: scala.collection.immutable.IndexedSeq[Boolean] = Vector(true, true, true, true, true, true, true, true, true, true, true)

scala> Array(1, 2, 3).map(_ * 2)
res2: Array[Int] = Array(2, 4, 6)
{% endhighlight %}

This conversion is done automatically using *implicit conversions*. We haven't covered implicits yet, and the full details are a bit involved. What we need to know here is that if we call a method on an object that does not support that method, but an implicit conversion exists to convert that object to one that does support the method, then the compiler will insert a call to the implicit conversion.

There are two


## Performance

You might be worried about the performance of implicit conversions. The Ops conversions are normally optimised away. The Wrapped conversions can give a small performance hit which may be an issue in particularly performance sensitive code.
