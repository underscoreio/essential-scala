## Arrays and Strings

Arrays and strings in Scala correspond to Java's arrays and strings.

```scala
"this is not a string"
// res: java.lang.String = this is not a string
```

Yet all the familiar collection methods are available on them.

```scala
"is it true?".map(elt => true)
// res: scala.collection.immutable.IndexedSeq[Boolean] = Vector(true, true, true, true, true, true,     ↩
                                                                          true, true, true, true, true)

Array(1, 2, 3).map(_ * 2)
// res: Array[Int] = Array(2, 4, 6)
```

This conversion is done automatically using implicit conversions. There are two conversions. The Wrapped conversions (`WrappedArray` and `WrappedString`) wrap the original array or string in an object supporting the `Seq` methods. Operations on such a wrapped object return another wrapped object.

```scala
val sequence = new scala.collection.immutable.WrappedString("foo")
// sequence: scala.collection.immutable.WrappedString = foo

sequence.reverse
// res: scala.collection.immutable.WrappedString = oof
```

The Ops conversions (`ArrayOps` and `StringOps`) add methods that return an object of the original type. Thus these objects are short-lived.

```scala
val sequence = new scala.collection.immutable.StringOps("foo")
// sequence: scala.collection.immutable.StringOps = foo

sequence.reverse
// res: String = oof
```

The choice of conversion is based on the required type. If we use a string, say, where a `Seq` is expected the string will be wrapped. If we just want to use a `Seq` method on a string then an Op conversion will be used.

```scala
val sequence: Seq[Char] = "foo"
// sequence: Seq[Char] = foo

sequence.getClass
// res: java.lang.Class[_ <: Seq[Char]] = class scala.collection.immutable.WrappedString
```

### Performance

You might be worried about the performance of implicit conversions. The Ops conversions are normally optimised away. The Wrapped conversions can give a small performance hit which may be an issue in particularly performance sensitive code.
