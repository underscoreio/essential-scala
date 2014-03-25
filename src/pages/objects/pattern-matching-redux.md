---
layout: page
title: "Pattern Matching Redux"
---

We have now seen all of the major aspects of classes, case classes, and traits, and collections. Armed with this information, we are in a good position to return to pattern matching and see some of its more powerful features.

As we discussed earlier, patterns are written in their own DSL that only superficially resembles regular Scala code. **Patterns serve as tests that *match* a specific set of Scala values**. The `match` expression compares a value to each pattern in turn, finds the first pattern that matches, and executes the corresponding block of Scala code.

**Some patterns bind values to variables** that can be used on the right hand side of the corresponding `=>` symbol, and **some patterns contain other patterns**, allowing us to build complex tests that simultaneously examine many parts of a value. Finally, **we can create our own custom patterns**, implemented in Scala code, to match any cross-section of values we see fit.

We have already seen case class patterns and certain types of sequence patterns. Each of the remaining types of pattern is described below together with an example of its use.

## Standard patterns

### Literal patterns

Literal patterns match a particular value. Any Scala literals work except function literals: primitive values, `Strings`, `nulls`, and `()`:

~~~ scala
scala> (1 + 1) match {
     |   case 1 => "It's one!"
     |   case 2 => "It's two!"
     |   case 3 => "It's three!"
     | }
res0: String = It's two!

scala> Person("Dave", "Gurnell") match {
     |   case Person("Noel", "Welsh") => "It's Noel!"
     |   case Person("Dave", "Gurnell") => "It's Dave!"
     | }
res1: String = It's Dave!

scala> println("Hi!") match {
     |   case () => "It's unit!"
     | }
Hi!
res6: String = It's unit!
~~~

### Constant patterns

Identifiers starting with an uppercase letter are *constants* that match a single predefined constant value:

~~~ scala
scala> val X = "Foo"
X: String = Foo

scala> val Y = "Bar"
Y: String = Bar

scala> val Z = "Baz"
Z: String = Baz

scala> "Bar" match {
     |   case X => "It's foo!"
     |   case Y => "It's bar!"
     |   case Z => "It's baz!"
     | }
res0: String = It's bar!
~~~

### Variable capture

Identifiers starting with lowercase letters bind values to variables. The variables can be used in the code to the right of the `=>`:

~~~ scala
scala> Person("Dave", "Gurnell") match {
     |   case Person(f, n) => f + " " + n
     | }
res2: String = "Dave Gurnell"
~~~

The `@` operator, written `x @ y`, allows us to capture a value in a variable `x` while also matching it against a pattern `y`. `x` must be a variable pattern and `y` can be any type of pattern. For example:

~~~ scala
scala> Person("Dave", "Gurnell") match {
     |   case p @ Person(_, s) => s"The person $p has the surname $s"
     | }
res2: String = "The person Person(Dave,Gurnell) is called Dave Gurnell"
~~~

### Wildcard patterns

The `_` symbol is a pattern that matches any value and simply ignores it. This is useful in two situations: when nested inside other patterns, and when used on its own to provide an "else" clause at the end of a match expression:

~~~ scala
scala> Person("Dave", "Gurnell") match {
     |   case Person("Noel", _) => "It's Noel!"
     |   case Person("Dave", _) => "It's Dave!"
     | }
res3: String = It's Dave!

scala> Person("Dave", "Gurnell") match {
     |   case Person(name, _) => s"It's $name!"
     | }
res4: String = It's Dave!

scala> Person("John", "Doe") match {
     |   case Person("Noel", _) => "It's Noel!"
     |   case Person("Dave", _) => "It's Dave!"
     |   case _ => "It's someone else!"
     | }
res5: String = It's someone else!
~~~

### Type patterns

A type pattern takes the form `x: Y` where `Y` is a type and `x` is a wildcard pattern or a variable pattern. The pattern matches any value of type `Y` and binds it to `x`:

~~~ scala
scala> val shape: Shape = Rectangle(1, 2)
shape: Shape = Rectangle(1.0,2.0)

scala> shape match {
     |   case c : Circle    => s"It's a circle: $c!"
     |   case r : Rectangle => s"It's a rectangle: $r!"
     |   case s : Square    => s"It's a square: $s!"
     | }
res3: String = It's a rectangle: Rectangle(1.0,2.0)!
~~~

### Tuple patterns

Tuples of any arity can be matched with parenthesised expressions as follows:

~~~ scala
scala> (1, 2) match {
     |   case (a, b) => a + b
     | }
res6: Int = 3
~~~

### Guard expressions

This isn't so much a pattern as a feature of the overall `match` syntax. We can add an extra condition to any `case` clause by suffixing the pattern with the keyword `if` and a regular Scala expression. For example:

~~~ scala
scala> 123 match {
     |   case a if a % 2 == 0 => "even"
     |   case _ => "odd"
     | }
res4: String = odd
~~~

To reiterate, the code between the `if` and `=>` keywords is a regular Scala expression, not a pattern.

## Extractors

Extractors are a much more general type of pattern. An extractor looks like a function call of zero or more arguments: `foo(a, b, c)`, where each argument is itself a pattern.

The clever thing about extractor patterns is that they are implemented in Scala code. Any object can be made into an extractor pattern simply by adding an method called `unapply` or `unapplySeq`. We'll dive into the guts of these methods in a minute. For now let's look at some of the predefined patterns from the Scala library:

### Case class extractors

The companion object of every `case class` is equipped with an extractor that creates a pattern of the same arity as the constructor. This makes it easy to capture fields in variables:

~~~ scala
scala> Person("Dave", "Gurnell") match {
     |   case Person(f, l) => List(f, l)
     | }
res6: List[String] = List(Dave, Gurnell)
~~~

### Regular expressions

Scala's regular expression objects are outfitted with a pattern that binds each of the captured groups:

~~~ scala
scala> import scala.util.matching.Regex

scala> val r = new Regex("""(\d+)\.(\d+)\.(\d+)\.(\d+)""")
r: scala.util.matching.Regex = (\d+)\.(\d+)\.(\d+)\.(\d+)

scala> "192.168.0.1" match {
     |   case r(a, b, c, d) => List(a, b, c, d)
     | }
res7: List[String] = List(192, 168, 0, 1)
~~~

### Lists and Sequences

Lists and sequences can be captured in several ways:

 - The `List` and `Seq` companion objects act as patterns that match fixed-length sequences:

   ~~~ scala
   scala> List(1, 2, 3) match {
        |    case List(a, b, c) => a + b + c
        | }
   res8: Int = 6
   ~~~

 - `Nil` matches the empty list:

   scala> Nil match {
        |   case List(a) => "length 1"
        |   case Nil => "length 0"
        | }
   res11: String = length 0

 - There is also a singleton object `::` that matches the head and tail of a list:

   ~~~ scala
   scala> List(1, 2, 3) match {
     |   case ::(head, tail) => s"head $head tail $tail"
     |   case Nil => "empty"
     | }
   res12: String = head 1 tail List(2, 3)
   ~~~

   This perhaps makes more sense when you realise that binary extractor patterns can also be written infix:

   ~~~ scala
   scala> List(1, 2, 3) match {
     |   case head :: tail => s"head $head tail $tail"
     |   case Nil => "empty"
     | }
   res12: String = head 1 tail List(2, 3)
   ~~~

 - Combined use of `::`, `Nil`, and `_` allow us to match the first elements of any length of list:

   ~~~ scala
   scala> List(1, 2, 3) match {
     |   case Nil => "length 0"
     |   case a :: Nil => s"length 1 starting $a"
     |   case a :: b :: Nil => s"length 2 starting $a $b"
     |   case a :: b :: c :: _ => s"length 3+ starting $a $b $c"
     | }
   res0: String = length 3+ starting 1 2 3
   ~~~

### Creating custom fixed-length extractors

You can use any object as a fixed-length extractor pattern by giving it a method called `unapply` with a particular type signature:

~~~ scala
def unapply(value: A): Boolean // pattern with 0 parameters
def unapply(value: A): Option[B]            // 1 parameter
def unapply(value: A): Option[(B1, B2)]     // 2 parameters
                                            // etc...
~~~

Each pattern matches values of type `A` and captures arguments of type `B`, `B1`, and so on. Case class patterns and `::` are examples of fixed-length extractors.

For example, the extractor below matches email addresses and splits them into their user and domain parts:

~~~ scala
scala> object Email {
     |   def unapply(str: String) = {
     |     val parts = str.split("@")
     |     if (parts.length == 2) Some((parts(0), parts(1))) else None
     |   }
     | }

scala> "dave@underscore.io" match {
     |   case Email(user, domain) => List(user, domain)
     | }
res7: List[String] = List(dave, underscore.io)

scala> "dave" match {
     |   case Email(user, domain) => List(user, domain)
     |   case _ => Nil
     | }
res8: List[String] = List()
~~~

This simpler pattern matches any string and uppercases it:

~~~ scala
scala> object Uppercase {
     |   def unapply(str: String) =
     |     Some(str.toUpperCase)
     | }
defined module Uppercase

scala> Person("Dave", "Gurnell") match {
     |   case Person(f, Uppercase(l)) => s"$f $l"
     | }
res10: String = Dave GURNELL
~~~

### Creating custom variable-length extractors

We can also create extractors that match arbitrary numbers of arguments by defining an `unapplySeq` method of the following form:

~~~ scala
def unapplySeq(value: A): Option[Seq[B]]
~~~

Variable-length extractors match a value only if the pattern in the `case` clause is the same length as the `Seq` returned by `unapplySeq`. `Regex` and `List` are examples of variable-length extractors.

The extractor below splits a string into its component words:

~~~ scala
scala> object Words {
     |   def unapplySeq(str: String) = Some(str.split(" " ).toSeq)
     | }
defined module Words

scala> "the quick brown fox" match {
     |   case Words(a, b, c) => s"3 words: $a $b $c"
     |   case Words(a, b, c, d) => s"4 words: $a $b $c $d"
     | }
res0: String = 4 words: the quick brown fox
~~~

### Wildcard sequence patterns

There is one final type of pattern that can only be used with variable-length extractors. The *wildcard sequence* pattern, written `_*`, matches zero or more arguments from a variable-length pattern and discards their values. For example:

~~~ scala
scala> List(1, 2, 3, 4, 5) match {
     |   case List(a, b, _*) => a + b
     | }
res1: Int = 3

scala> "the quick brown fox" match {
     |   case Words(a, b, _*) => a + b
     | }
res2: String = "thequick"
~~~

## Exercises

### Titlecase extractor

Write an extractor that makes any string titlecase:

Tips: split(" "), toList, mkString(" ")

<div class="solution">
~~~ scala
object Titlecase {
  def unapply(str: String) = {
    str.split(" ").toList.map {
      case "" => ""
      case word => word.substring(0, 1).toUpperCase + word.substring(1)
    }.mkString(" ")
  }
}
~~~
</div>
