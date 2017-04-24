## Java Interoperation

The prefered way to convert between Scala and Java collections is use the `JavaConverters` implicit conversions. We use it by importing `scala.collection.JavaConverters._` and then methods `asJava` and `asScala` become available on many of the collections.

```scala
import scala.collection.JavaConverters._

Seq(1, 2, 3).asJava
// res: java.util.List[Int] = [1, 2, 3]
```

Java does not distinguish mutable and immutable collections at the type level but the conversions do preserve this property by throwing `UnsupportOperationException` as appropriate.

```scala
val java = Seq(1, 2, 3).asJava
// java: java.util.List[Int] = [1, 2, 3]

java.set(0, 5)
// java.lang.UnsupportedOperationException
// 	at java.util.AbstractList.set(AbstractList.java:115)
//     ...
```

The conversions go the other way as well.

```scala
val list: java.util.List[Int] = new java.util.ArrayList[Int]()
// list: java.util.List[Int] = []

list.asScala
// res: scala.collection.mutable.Buffer[Int] = Buffer()
```

Note that the Scala equivalent is a mutable collection. If we mutate an element we see that the underlying Java collection is also changed. This holds for all conversions; they always share data and are not copied.

```scala
list.asScala += 5
// res: scala.collection.mutable.Buffer[Int] = Buffer(5)

list
// res: java.util.List[Int] = [5]
```

### JavaConversions

There is another set of conversions in `scala.collection.JavaConversions`, which perform conversions without needing the calls to `asJava` or `asScala`. Many people find this confusing in large systems and thus it is not recommended.
