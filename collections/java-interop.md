---
layout: page
---

# Java Interoperation

The prefered way to convert between Scala and Java collections is use the `JavaConverters` implicit conversions. We use it by importing `scala.collection.JavaConverters._` and then methods `asJava` and `asScala` become available on many of the collections.

{% highlight scala %}
scala> import scala.collection.JavaConverters._
import scala.collection.JavaConverters._

scala> Seq(1, 2, 3).asJava
res18: java.util.List[Int] = [1, 2, 3]
{% endhighlight %}

Java does not distinguish mutable and immutable collections at the type level but the conversions do preserve this property by throwing `UnsupportOperationException` as appropriate.

{% highlight scala %}
scala> val java = Seq(1, 2, 3).asJava
java: java.util.List[Int] = [1, 2, 3]

scala> java.set(0, 5)
java.lang.UnsupportedOperationException
	at java.util.AbstractList.set(AbstractList.java:115)
    ...
{% endhighlight %}

The conversions go the other way as well.

{% highlight scala %}
scala> val list: java.util.List[Int] = new java.util.ArrayList[Int]()
list: java.util.List[Int] = []

scala> list.asScala
res4: scala.collection.mutable.Buffer[Int] = Buffer()
{% endhighlight %}

Note that the Scala equivalent is a mutable collection. If we mutate an element we see that the underlying Java collection is also changed. This holds for all conversions; they always share data and are not copied.

{% highlight scala %}
scala> list.asScala += 5
res8: scala.collection.mutable.Buffer[Int] = Buffer(5)

scala> list
res9: java.util.List[Int] = [5]
{% endhighlight %}

## JavaConversions

The is another object, `scala.collection.JavaConversions`, that performs conversions with needing the calls to `asJava` or `asScala`. Many people find this confusing in large systems and thus it is not recommended.
