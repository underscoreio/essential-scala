## Type Class Foundations

Type classes in Scala involve the interaction of a number of components. To understand how they work, in this section we're going to build them up from the beginning.

Let's start with an example---converting data to HTML. This is a fundamental operation in any web application, and it would be great to be able to provide a `toHtml` method across the board in our application.

The obvious implementation it to implement `toHtml` using a simple trait:

~~~ scala
scala> trait HtmlWriteable {
         def toHtml: String
       }
defined trait HtmlWriteable

scala> case class Person(name: String, email: String) extends HtmlWriteable {
         def toHtml = s"$name ($email)"
       }
defined class Person

scala> Person("John", "john@example.com").toHtml
res0: String = John (john@example.com)
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
         def write(person: Person) = s"${person.name} (${person.email})"
       }
defined module PersonWriter

scala> PersonWriter.write(Person("John", "john@example.com"))
res1: String = John (john@example.com)
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
           s"${person.name} (${person.email.replaceAll("@", " at ")})"
       }
defined module ObfuscatedPersonWriter

scala> ObfuscatedPersonWriter.write(Person("John", "john@example.com"))
res3: String = John (john at example.com)
~~~

Much safer---it'll take a spam bot more than a few microseconds to decypher that!

This is the essence of the type class pattern. All the refinements we will see in later sections just make it easier to use.

### Take Home Points

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
    s"${person.name} (${person.email})"
}

object ObfuscatedPersonWriter extends HtmlWriter[Person] {
  def write(person: Person) =
    s"${person.name} (${person.email.replaceAll("@", " at ")})"
}
~~~
- This allows us to implement the functionality for any type, and to provide different implementations for the same type.

### Exercises

#### Equality

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
