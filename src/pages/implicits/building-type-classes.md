---
layout: page
title: Building Type Classes
---

Type classes in Scala involve the interaction of a number of components. In this section we're going to build them up from the beginning, to gain an understanding of how all the pieces hang together.

Let's motivate this with an example -- converting data to HTML. This is a fundamental operation in any web application, and it would be great to be able to provide a `toHtml` method across the board in our application.

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

This solution has a number of drawbacks. First, we are restricted to having just one way of rendering a `Person`. If we want to list people on our company homepage, for example, it is unlikely we will want to list everybody's email addresses without obfuscation. Second, this pattern can only be applied to classes that we have written ourselves. If we want to render `java.util.Data` to HTML, for example, we will have to write some other form of library function.

We can overcome both of these problems by moving our HTML rendering to an adapter class:

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
     |   def write(in: Date) = s"<span>${in.toString}</span>"
     | }
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

This is the essence of the type class pattern. All the refinements we will see just make it easier to use.

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

Scala provides two equality predicates: by value (`==`) and by reference (`eq`). Nonetheless, we sometimes need additional predicates. For instac, we could appear people by just email address if we were validating new user accounts in some web application.

Implement a trait `Equal` of some type `A`, with a method `equals` that compares two values of type `A` and returns a `Boolean`.

<div class="solution">
~~~ scala
trait Equal[A] {
  def equal(v1: A, v2: A): Boolean
}
 scala
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
