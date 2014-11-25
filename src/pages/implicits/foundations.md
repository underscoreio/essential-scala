---
layout: page
title: Type Class Foundations
---

Type classes in Scala involve the interaction of a number of components. To simplify the presentation we are going to start by looking at *using* type classes before we look at how to build them ourselves.

## Ordering

A simple example of a type class is the `Ordering` trait in Scala. Imagine we want to sort a `List` of `Int`s. There are many different ways to sort such a list. For example, we could sort from highest to lowest or lowest to highest. There is a method `sorted` on `List` that will sort a list, but to use it we must pass in an `Ordering` to give the particular ordering we want.

Let's define some `Ordering`s and see them in action.

~~~ scala
scala> import scala.math.Ordering

scala> val minOrdering = Ordering.fromLessThan[Int](_ < _)
minOrdering: scala.math.Ordering[Int] = scala.math.Ordering$$anon$9@787f32b7

scala> val maxOrdering = Ordering.fromLessThan[Int](_ > _)
maxOrdering: scala.math.Ordering[Int] = scala.math.Ordering$$anon$9@4bf324f9

scala> List(3, 4, 2).sorted(minOrdering)
res9: List[Int] = List(2, 3, 4)

scala> List(3, 4, 2).sorted(maxOrdering)
res10: List[Int] = List(4, 3, 2)
~~~

Here we define two orderings: `minOrdering`, which sorts from lowest to highest, and `maxOrdering`, which sorts from highest to lowest. When we call `sorted` we pass the `Ordering` we want to use. These implementations of a type class are called **type class instances**.

**This is the basic usage pattern for type classes.** Everything else we will see just provides extra convenience.


## Implicit Values

It can be inconvenient to continually pass the type class instance to a method when we want to repeatedly use the same instance. Scala provides a convenience, called **implicit values**, that allows us get the compiler to provide a type class instance for us. Here's an example of use:

~~~ scala
scala> implicit val ordering = Ordering.fromLessThan[Int](_ < _)

scala> List(2, 4, 3).sorted
res1: List[Int] = List(2, 3, 4)

scala> List(1, 7 ,5).sorted
res2: List[Int] = List(1, 5, 7)
~~~

Not how we don't supply an ordering to `sorted`. The compiler provides it for us. We have to tell the compiler which values it is allowed provide for us. We do this by annotating a value with `implicit`, as in the declaration `implicit val order = ...`. The second part is that the method must indicate that accepts implicit values. If you look at the [documentation for the `sorted` method on `List`](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List) you see that the single parameter is declared `implicit`. We'll talk more about implicit parameter lists in a bit. For now we just need to know that we can get the compiler to supply implicit values to parameters that are themselves marked implicit.

### Declaring Implicit Values

We can tag any `val`, `var`, `object` or zero-argument `def` with the `implicit` keyword, making it a potential candidate for an implicit parameter.

~~~ scala
implicit val exampleOne = ...
implicit var exampleTwo = ...
implicit def exampleThree = ...
~~~

### Implicit Value Ambiguity

What happens when multiple implicit values are in scope? Let's ask the console.

~~~ scala
scala> implicit val minOrdering = Ordering.fromLessThan[Int](_ < _)

scala> implicit val maxOrdering = Ordering.fromLessThan[Int](_ > _)

scala> List(3,4,5).sorted
 <console>:12: error: ambiguous implicit values:
 both value ordering of type => scala.math.Ordering[Int]
 and value minOrdering of type => scala.math.Ordering[Int]
 match expected type scala.math.Ordering[Int]
                List(3,4,5).sorted
                            ^
~~~

The rule is simple. The compiler will signal an error if there is any ambiguity in which implicit value should be used.


## Exercises

### More Orderings

Scala doesn't have a class to represent rational numbers, but we can easily implement one ourselves.

~~~ scala 
final case class Rational(numerator: Int, denominator: Int)
~~~

Implement an `Ordering` for `Rational` to order rationals from smallest to largest. The following test case should pass.

~~~ scala
assert(List(Rational(1, 2), Rational(3, 4), Rational(1, 3)).sorted ==
       List(Rational(1, 3), Rational(1, 2), Rational(3, 4)))
~~~

<div class="solution">
~~~ scala
implicit val ordering = Ordering.fromLessThan[Rational]((x, y) =>
  (x.numerator.toDouble / x.denominator.toDouble) < 
  (y.numerator.toDouble / y.denominator.toDouble)
)
~~~
</div>

# OLD MATERIAL HERE

To understand how they work, in this section we're going to build them up from the beginning.

Let's start with an example -- converting data to HTML. Thits is a fundamental operation in any web application, and it would be great to be able to provide a `toHtml` method across the board in our application.

The obvious implementation it to implement `toHtml` using a simple trait:

~~~ scala
scala> trait HtmlWriteable {
         def toHtml: String
       }
defined trait HtmlWriteable

scala> case class Person(name: String, email: String) extends HtmlWriteable {
         def toHtml = s"<span>$name &lt;$email&gt;</span>"
       }
defined class Person

scala> Person("John", "john@example.com").toHtml
res0: String = <span>John &lt;john@example.com&gt;</span>
~~~

This solution has a number of drawbacks. First, we are restricted to having just one way of rendering a `Person`. If we want to list people on our company homepage, for example, it is unlikely we will want to list everybody's email addresses without obfuscation. For logged in users, however, we probably want the convenience of direct email links. Second, this pattern can only be applied to classes that we have written ourselves. If we want to render a `java.util.Date` to HTML, for example, we will have to write some other form of library function.

Polymorphism has failed us, so perhaps we should try pattern matching instead? We could write some like

~~~ scala
object HtmlWriter {
  def write(in: Any): String =
    in match {
      case Person(name, email) => ...
      case Date => ...
      case _ => throw new Exception(s"Can't render ${in} to HTML")
    }
}
~~~

This implementation has its own issues. We have lost type safety because there is not useful supertype that covers just the elements we want to render. We can't have more than one implemnetation of rendering for a given type. We also have to modify this code whenever we want to render a new type.

We can overcome all of these problems by moving our HTML rendering to an adapter class:

~~~ scala
scala> trait HtmlWriter[T] {
         def write(in: T): String
       }
defined trait HtmlWriter

scala> object PersonWriter extends HtmlWriter[Person] {
         def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
       }
defined module PersonWriter

scala> PersonWriter.write(Person("John", "john@example.com"))
res1: String = <span>John &lt;john@example.com&gt;</span>
~~~

This is better. We can now define `HtmlWriter` functionality for other types, including types we have not written ourselves:

~~~ scala
scala> import java.util.Date
import java.util.Date

scala> object DateWriter extends HtmlWriter[Date] {
         def write(in: Date) = s"<span>${in.toString}</span>"
       }
defined module DateWriter

scala> DateWriter.write(new Date)
res2: String = <span>Sat Apr 05 16:01:58 BST 2014</span>
~~~

We can also write another `HtmlWriter` for writing `People` on our homepage:

~~~ scala
scala> object ObfuscatedPersonWriter extends HtmlWriter[Person] {
         def write(person: Person) =
           s"<span>${person.name} &lt;${person.email.replaceAll("@", " at ")}&gt;</span>"
       }
defined module ObfuscatedPersonWriter

scala> ObfuscatedPersonWriter.write(Person("John", "john@example.com"))
res3: String = <span>John &lt;john at example.com&gt;</span>
~~~

Much safer -- it'll take a spam bot more than a few microseconds to decypher that!

This is the essence of the type class pattern. All the refinements we will see in later sections just make it easier to use.

## Take Home Points

We have seen the basic pattern for implementing type classes, though we'll shortly see a number of features in Scala that make them easier to use.

- We declare some interface for the functionality we want

  ~~~ scala
  trait HtmlWriter[A] {
    def toHtml(in: A): String
  }
  ~~~
- We write adaptors for each concrete class we want to use and for each different situation we want to use it in

  ~~~ scala
  object PersonWriter extends HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"
  }

  object ObfuscatedPersonWriter extends HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email.replaceAll("@", " at ")}&gt;</span>"
  }
  ~~~
- This allows us to implement the functionality for any type, and to provide different implementations for the same type.

## Exercises

### Equality

Scala provides two equality predicates: by value (`==`) and by reference (`eq`). Nonetheless, we sometimes need additional predicates. For instance, we could compare people by just email address if we were validating new user accounts in some web application.

Implement a trait `Equal` of some type `A`, with a method `equal` that compares two values of type `A` and returns a `Boolean`.

<div class="solution">
~~~ scala
trait Equal[A] {
  def equal(v1: A, v2: A): Boolean
}
~~~
</div>

Our `Person` class is

~~~ scala
case class Person(name: String, email: String)
~~~

Implement instances of `Equal` that compare for equality by email address only, and by name and email.

<div class="solution">
~~~ scala
object EmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email
}

object NameEmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email && v1.name == v2.name
}
~~~
</div>
