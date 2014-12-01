## Arrays and Strings

Arrays and strings in Scala correspond to Java's arrays and strings.

~~~ scala
scala> "this is not a string"
res0: java.lang.String = this is not a string
~~~

Yet all the familiar collection methods are available on them.

~~~ scala
scala> "is it true?".map(elt => true)
res1: scala.collection.immutable.IndexedSeq[Boolean] = Vector(true, true, true, true, true, true, true, true, true, true, true)

scala> Array(1, 2, 3).map(_ * 2)
res2: Array[Int] = Array(2, 4, 6)
~~~

This conversion is done automatically using *implicit conversions*. We haven't covered implicits yet, and the full details are a bit involved. What we need to know here is that if we call a method on an object that does not support that method, but an implicit conversion exists to convert that object to one that does support the method, then the compiler will insert a call to the implicit conversion.

There are two conversions. The Wrapped conversions (`WrappedArray` and `WrappedString`) wrap the original array or string in an object supporting the `Seq` methods. Operations on such a wrapped object return another wrapped object.

~~~ scala
scala> val sequence = new scala.collection.immutable.WrappedString("foo")
sequence: scala.collection.immutable.WrappedString = foo

scala> sequence.reverse
res4: scala.collection.immutable.WrappedString = oof
~~~

The Ops conversions (`ArrayOps` and `StringOps`) add methods that return an object of the original type. Thus these objects are short-lived.

~~~ scala
scala> val sequence = new scala.collection.immutable.StringOps("foo")
sequence: scala.collection.immutable.StringOps = foo

scala> sequence.reverse
res5: String = oof
~~~

The choice of conversion is based on the required type. If we use a string, say, where a `Seq` is expected the string will be wrapped. If we just want to use a `Seq` method on a string then an Op conversion will be used.

~~~ scala
scala> val sequence: Seq[Char] = "foo"
sequence: Seq[Char] = foo

scala> sequence.getClass
res10: java.lang.Class[_ <: Seq[Char]] = class scala.collection.immutable.WrappedString
~~~

### Performance

You might be worried about the performance of implicit conversions. The Ops conversions are normally optimised away. The Wrapped conversions can give a small performance hit which may be an issue in particularly performance sensitive code.
