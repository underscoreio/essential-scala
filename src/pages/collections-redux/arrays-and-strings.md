## Arrays and Strings

Arrays and strings in Scala correspond to Java's arrays and strings.

```scala mdoc
"this is a string"
```

Yet all the familiar collection methods are available on them.

```scala mdoc
"is it true?".map(elt => true)

Array(1, 2, 3).map(_ * 2)
```

This conversion is done automatically using implicit conversions. There are two conversions. The Wrapped conversions (`WrappedArray` and `WrappedString`) wrap the original array or string in an object supporting the `Seq` methods. Operations on such a wrapped object return another wrapped object.

```scala mdoc
val sequence = new scala.collection.immutable.WrappedString("foo")

sequence.reverse
```

The Ops conversions (`ArrayOps` and `StringOps`) add methods that return an object of the original type. Thus these objects are short-lived.

```scala mdoc
val sequence = new scala.collection.immutable.StringOps("foo")

sequence.reverse
```

The choice of conversion is based on the required type. If we use a string, say, where a `Seq` is expected the string will be wrapped. If we just want to use a `Seq` method on a string then an Op conversion will be used.

```scala mdoc
val sequence: Seq[Char] = "foo"

sequence.getClass
```

### Performance

You might be worried about the performance of implicit conversions. The Ops conversions are normally optimised away. The Wrapped conversions can give a small performance hit which may be an issue in particularly performance sensitive code.
