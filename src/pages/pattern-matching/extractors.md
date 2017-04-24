## Custom Patterns

In the last section we took an in-depth look at all of the types of pattern that are embedded into the pattern matching language. However, in that list we didn't see some of the patterns that we've been using in the course so far---case class and sequence patterns were nowhere to be seen!

There is a final aspect of pattern matching that we haven't covered that truly makes it a universal tool---we can define our own custom *extractor* patterns using regular Scala code and use them along-side the built-in patterns in our `match` expressions.

### Extractors

An extractor pattern looks like a function call of zero or more arguments: `foo(a, b, c)`, where each argument is itself an arbitrary pattern.

Extractor patterns are defined by creating objects with a method called `unapply` or `unapplySeq`. We'll dive into the guts of these methods in a minute. For now let's look at some of the predefined extractor patterns from the Scala library.

#### Case class extractors

The companion object of every `case class` is equipped with an extractor that creates a pattern of the same arity as the constructor. This makes it easy to capture fields in variables:

```scala
Person("Dave", "Gurnell") match {
  case Person(f, l) => List(f, l)
}
// res: List[String] = List(Dave, Gurnell)
```

#### Regular expressions

Scala's regular expression objects are outfitted with a pattern that binds each of the captured groups:

```scala
import scala.util.matching.Regex

val r = new Regex("""(\d+)\.(\d+)\.(\d+)\.(\d+)""")
// r: scala.util.matching.Regex = (\d+)\.(\d+)\.(\d+)\.(\d+)

"192.168.0.1" match {
  case r(a, b, c, d) => List(a, b, c, d)
}
// res: List[String] = List(192, 168, 0, 1)
```

#### Lists and Sequences

Lists and sequences can be captured in several ways:

The `List` and `Seq` companion objects act as patterns that match fixed-length sequences.

```scala
List(1, 2, 3) match {
   case List(a, b, c) => a + b + c
}
// res: Int = 6
```

 - `Nil` matches the empty list:

```scala
Nil match {
  case List(a) => "length 1"
  case Nil => "length 0"
}
// res: String = length 0
```

There is also a singleton object `::` that matches the head and tail of a list.

```scala
List(1, 2, 3) match {
  case ::(head, tail) => s"head $head tail $tail"
  case Nil => "empty"
}
// res: String = head 1 tail List(2, 3)
```

This perhaps makes more sense when you realise that binary extractor patterns can also be written infix.

```scala
List(1, 2, 3) match {
  case head :: tail => s"head $head tail $tail"
  case Nil => "empty"
}
// res: String = head 1 tail List(2, 3)
```

Combined use of `::`, `Nil`, and `_` allow us to match the first elements of any length of list.

```scala
List(1, 2, 3) match {
  case Nil => "length 0"
  case a :: Nil => s"length 1 starting $a"
  case a :: b :: Nil => s"length 2 starting $a $b"
  case a :: b :: c :: _ => s"length 3+ starting $a $b $c"
}
// res: String = length 3+ starting 1 2 3
```

#### Creating custom fixed-length extractors

You can use any object as a fixed-length extractor pattern by giving it a method called `unapply` with a particular type signature:

```scala
def unapply(value: A): Boolean           // pattern with 0 parameters
def unapply(value: A): Option[B]                      // 1 parameter
def unapply(value: A): Option[(B1, B2)]               // 2 parameters
                                                      // etc...
```

Each pattern matches values of type `A` and captures arguments of type `B`, `B1`, and so on. Case class patterns and `::` are examples of fixed-length extractors.

For example, the extractor below matches email addresses and splits them into their user and domain parts:

```scala
object Email {
  def unapply(str: String): Option[(String, String)] = {
    val parts = str.split("@")
    if (parts.length == 2) Some((parts(0), parts(1))) else None
  }
}

"dave@underscore.io" match {
  case Email(user, domain) => List(user, domain)
}
// res: List[String] = List(dave, underscore.io)

"dave" match {
  case Email(user, domain) => List(user, domain)
  case _ => Nil
}
// res: List[String] = List()
```

This simpler pattern matches any string and uppercases it:

```scala
object Uppercase {
  def unapply(str: String): Option[String] =
    Some(str.toUpperCase)
}

Person("Dave", "Gurnell") match {
  case Person(f, Uppercase(l)) => s"$f $l"
}
// res: String = Dave GURNELL
```

#### Creating custom variable-length extractors

We can also create extractors that match arbitrary numbers of arguments by defining an `unapplySeq` method of the following form:

```scala
def unapplySeq(value: A): Option[Seq[B]]
```

Variable-length extractors match a value only if the pattern in the `case` clause is the same length as the `Seq` returned by `unapplySeq`. `Regex` and `List` are examples of variable-length extractors.

The extractor below splits a string into its component words:

```scala
object Words {
  def unapplySeq(str: String) = Some(str.split(" ").toSeq)
}

"the quick brown fox" match {
  case Words(a, b, c)    => s"3 words: $a $b $c"
  case Words(a, b, c, d) => s"4 words: $a $b $c $d"
}
// res: String = 4 words: the quick brown fox
```

#### Wildcard sequence patterns

There is one final type of pattern that can only be used with variable-length extractors. The *wildcard sequence* pattern, written `_*`, matches zero or more arguments from a variable-length pattern and discards their values. For example:

```scala
List(1, 2, 3, 4, 5) match {
  case List(a, b, _*) => a + b
}
// res: Int = 3

"the quick brown fox" match {
  case Words(a, b, _*) => a + b
}
// res: String = "thequick"
```

We can combine wildcard patterns with the `@` operator to capture the remaining elements in the sequence.

```scala
"the quick brown fox" match {
  case Words(a, b, rest @ _*) => rest
}
// res: Seq[String] = WrappedArray("brown", "fox")
```

### Exercises

#### Positive Matches

Custom extractors allow us to abstract away complicated conditionals. In this example we will build a very simple extractor, which we probably wouldn't use in real code, but which is representative of this idea.

Create an extractor `Positive` that matches any positive integer. Some test cases:

```scala
assert(
  "No" ==
    (0 match {
       case Positive(_) => "Yes"
       case _ => "No"
     })
)

assert(
  "Yes" ==
    (42 match {
       case Positive(_) => "Yes"
       case _ => "No"
     })
)
```

<div class="solution">
To implement this extractor we define an `unapply` method on an object `Postiive`.
```scala
object Positive {
  def unapply(in: Int): Option[Int] =
    if(in > 0)
      Some(in)
    else
      None
}
```
</div>

#### Titlecase extractor

Extractors can also transform their input. In this exercise we'll write an extractor that converts any string to titlecase by uppercasing the first letter of every word. A test case:

```scala
assert(
  "Sir Lord Doctor David Gurnell" ==
    ("sir lord doctor david gurnell" match {
       case Titlecase(str) => str
     })
)
```

Tips:

 - Java `Strings` have the methods `split(String)`, `toUpperCase` and `substring(Int, Int)`.

 - The method `split(String)` returns a Java `Array[String]`. You can convert this to a `List[String]` using `array.toList` so you can `map` over it and manipulate each word.

 - A `List[String]` can be converted back to a `String` with the code `list.mkString(" ")`.

This extractor isn't particularly useful, and in general defining your own extractors is not common in Scala. However it can be a useful tool in certain circumstances.

<div class="solution">
The model solution splits the string into a list of words and maps over the list, manipulating each word before re-combining the words into a string.

```scala
object Titlecase {
  def unapply(str: String) = {
    str.split(" ").toList.map {
      case "" => ""
      case word => word.substring(0, 1).toUpperCase + word.substring(1)
    }.mkString(" ")
  }
}
```
</div>
