## Java Interoperation

The prefered way to convert between Scala and Java collections is use the `JavaConverters` implicit conversions. We use it by importing `scala.collection.JavaConverters._` and then methods `asJava` and `asScala` become available on many of the collections.

```tut:book:silent
import scala.collection.JavaConverters._
```

```tut:book
Seq(1, 2, 3).asJava
```

Java does not distinguish mutable and immutable collections at the type level but the conversions do preserve this property by throwing `UnsupportOperationException` as appropriate.

```tut:book
val javaCollection = Seq(1, 2, 3).asJava
```

```tut:book:fail:silent
javaCollection.set(0, 5)
// java.lang.UnsupportedOperationException
// 	at java.util.AbstractList.set(AbstractList.java:115)
//     ...
```

The conversions go the other way as well.

```tut:book
val list: java.util.List[Int] = new java.util.ArrayList[Int]()

list.asScala
```

Note that the Scala equivalent is a mutable collection. If we mutate an element we see that the underlying Java collection is also changed. This holds for all conversions; they always share data and are not copied.

```tut:book
list.asScala += 5

list
```

### JavaConversions

There is another set of conversions in `scala.collection.JavaConversions`, which perform conversions without needing the calls to `asJava` or `asScala`. Many people find this confusing in large systems and thus it is not recommended.
