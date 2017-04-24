## Mutable Sequences

Most of the interfaces we've have covered so far do not have any side-effects---like the `copy` method on a case class, they return a new copy of the sequence. Sometimes, however, we need mutable collections. Fortunately, Scala provides two parallel collections hierarchies, one in the `scala.collection.mutable` package and one in the `scala.collection.immutable` package.

The default `Seq` is defined to be `scala.collection.immutable.Seq`. If we want a mutable sequence we can use `scala.collection.mutable.Seq`.

```scala
val mutable = scala.collection.mutable.Seq(1, 2, 3)
// mutable: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)
```

Note that the concrete implementation class is now an `ArrayBuffer` and not a `List`.

### Destructive update

In addition to all the methods of an immutable sequence, a mutable sequence can be updated using the `update` method. Note that `update` returns `Unit`, so no value is printed in the REPL after this call. When we print the original sequence we see it is changed:

```scala
mutable.update(0, 5)

mutable
// res: scala.collection.mutable.Seq[Int] = ArrayBuffer(5, 2, 3)
```

A more idiomatic way of calling `update` is to use *assignment operator syntax*, which is another special syntax built in to Scala, similar to infix operator syntax and function application syntax:

```scala
mutable(1) = 7

mutable
// res: scala.collection.mutable.Seq[Int] = ArrayBuffer(5, 7, 3)
```

### Immutable methods on mutable sequences

Methods defined on both mutable and immutable sequences will never perform destructive updates. For example, `:+` always returns a new copy of the sequence without updating the original:

```scala
val mutable = scala.collection.mutable.Seq[Int](1, 2, 3)
// mutable: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)

mutable :+ 4
// res: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3, 4)

mutable
// res: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)
```

<div class="callout callout-info">
#### Using Mutable Collections Safely

Scala programmers tend to favour immutable collections and only bring in mutable ones in specific circumastances. Using `import scala.collection.mutable._` at the top of a file tends to create a whole series of naming collisions that we have to work around.

To work around this, I suggest importing the `mutable` package iteself rather than its contents. We can then explicitly refer to any mutable collection using the package name as a prefix, leaving the unprefixed names referring to the immutable versions:

```scala
import scala.collection.mutable
import scala.collection.mutable

mutable.Seq(1, 2, 3)
// res: scala.collection.mutable.Seq[Int] = ArrayBuffer(1, 2, 3)

Seq(1, 2, 3)
// res: Seq[Int] = List(1, 2, 3)
```
</div>

### In summary

Scala's collections library includes mutable sequences in the `scala.collection.mutable` package. The main extra operation is `update`:

+------------+------------+-------------------+-------------+
| Method     | We have    | We provide        | We get      |
+============+============+===================+=============+
| `update`   | `Seq[A]`   | `Int`, `A`        | `Unit`      |
+------------+------------+-------------------+-------------+


### Exercises

#### Animals

Create a `Seq` containing the `String`s `"cat"`, `"dog"`, and `"penguin"`. Bind it to the name `animals`.

<div class="solution">
```scala
val animals = Seq("cat", "dog", "penguin")
// animals: Seq[String] = List(cat, dog, penguin)
```
</div>

Append the element `"tyrannosaurus"` to `animals` and prepend the element `"mouse"`.

<div class="solution">
```scala
"mouse" +: animals :+ "tyrannosaurus"
// res: Seq[String] = List(mouse, cat, dog, penguin, tyrannosaurus)
```
</div>

What happens if you prepend the `Int` `2` to `animals`? Why? Try it out... were you correct?

<div class="solution">
The returned sequence has type `Seq[Any]`.  It is perfectly valid to return a supertype (in this case `Seq[Any]`) from a non-destructive operation.

```scala
2 +: animals
// res: Seq[Any] = List(2, cat, dog, penguin)
```

You might expect a type error here, but Scala is capable of determining the least upper bound of `String` and `Int` and setting the type of the returned sequence accordingly.

In most real code appending an `Int` to a `Seq[String]` would be an error. In practice, the type annotations we place on methods and fields protect against this kind of type error, but be aware of this behaviour just in case.
</div>

Now create a mutable sequence containing `"cat"`, `"dog"`, and `"penguin"` and `update` an element to be an `Int`. What happens?

<div class="solution">
If we try to mutate a sequence we *do* get a type error:

```scala
val mutable = scala.collection.mutable.Seq("cat", "dog", "penguin")
// mutable: scala.collection.mutable.Seq[String] = ArrayBuffer(cat, dog, penguin)

mutable(0) = 2
// <console>:9: error: type mismatch;
//  found   : Int(2)
//  required: String
//               mutable(0) = 2
//                            ^
```
</div>
