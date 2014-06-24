---
layout: page
title: Pattern Matching
---

Until now we have interacted with objects by calling methods or accessing fields. With case classes we can interact in another way, via **pattern matching**.

Pattern matching is like an extended `if` expression that allows us to evaluate an expression depending on the "shape" of the data. Recall the `Person` case class we've seen in previous examples:

~~~ scala
case class Person(firstName: String, lastName: String)
~~~

Now imagine we wanted to implement a `Stormtrooper` that is looking for members of the rebellion. We could use pattern matching like this:

~~~ scala
object Stormtrooper {
  def inspect(person: Person): String =
    person match {
      case Person("Luke", "Skywalker") => "Stop rebel scum!"
      case Person("Han", "Solo") => "Stop rebel scum!"
      case _ => "Move along"
    }
}
~~~

This shows the essence of pattern matching.

...

Pattern matching is introduced using the `match` keyword. It is followed by a sequence of `case` expressions. After each `case` keyword is a pattern, an `=>`, and an expression. Pattern matching is itself an expression and thus produces a value.

~~~
expr0 match {
  case pattern1 => expr1
  case pattern2 => expr1
  ...
}
~~~

Pattern matching proceeds by checking each pattern in turn, and evaluating the right-hand side expression of the first pattern that matches[^compilation].

[^compilation]: In reality patterns are compiled to a more efficient form than a sequence of tests, but the semantics are the same.

The syntax of pattern matching is very expressive. For case classes the pattern syntax matches the constructor syntax. So the pattern `User(id, address, createdAt)` matches a `User` and binds the names `id`, `address`, and `createdAt` to their respective values. Binding happens by position, not by name, so if we wrote `User(address, createdAt, id)` the name `address` would be bound to the value of the `id` and so on. If there is a value we don't want to bind to a name, we use the `_` symbol. As we only care about the `address` in the example above, we could just write `User(_, address, _)`.

Literals can be used as patterns, which match themselves. So a pattern like `User(_, "me@example.com", _)` would match the user with email address `me@example.com`.

Following a pattern with an `if` statement, known as a **guard**, allows testing of additional conditions. For example, to match `User`s from `example.com` we could use `User(_, email, _) if email.contains("example.com")`. Note there are no brackets around the condition in the guard, unlike the normal `if` expression.

Sometimes we want to bind a name to the entire value we've matched. For this we can use the syntax `name @ pattern` like so: `user @ User(_, address, _)`. This is useful because after matching the pattern `user` has a more specific type (`User`) than the one we started with (`Visitor`).

Finally, if we just want to test a particular type we can use syntax like `user : User`.

Scala's pattern matching is extensible, meaning we can define our own patterns. This is discussed is a later section.

## Exercise

We covered a lot of ground already, and it's time to cement our learning with a somewhat larger exercise than we've had before. We'll tackle it in small pieces.

### Sum Types

Imagine you are working on a system to record publications. A publication can be either a book or a periodical. All publications have an ISBN number (which we'll model as a `String`), and a title. Books have an author, while periodicals have an editor. Encode this greatly simplified model in Scala.

<div class="solution">
~~~ scala
trait Publication {
  def isbn: String
  def title: String
}

case class Book(
  val isbn: String,
  val title: String,
  val author: String
) extends Publication

case class Periodical(
  val isbn: String,
  val title: String,
  val editor: String
) extends Publication
~~~
</div>

### Pattern Matching

The publisher needs to ship out XML to book merchants. The XML looks like this:

~~~ xml
<book>
  <title>The Unbearable Lightness of Being</title>
  <author>Milan Kundera</author>
  <isbn>1234567890</isbn>
</book>

<periodical>
  <title>Modern Drunkard Magazine</title>
  <editor>Frank Kelly Rich</editor>
  <isbn>0987654321</isbn>
</periodical>
~~~

Write code to render a `Book` as XML. Note that Scala supports XML literals.

~~~ scala
scala> <foo></foo>
<foo></foo>
res11: scala.xml.Elem = <foo></foo>

scala> val name = "Jake"
val name = "Jake"
name: String = Jake

scala> <person>{name}</person>
<person>{name}</person>
res12: scala.xml.Elem = <person>Jake</person>
~~~

<div class="solution">
~~~ scala
object XmlExportService {
  def export(p: Publication) =
    p match {
      case Book(isbn, title, author) =>
        <book>
          <title>{title}</title>
          <author>{author}</author>
          <isbn>{isbn}</isbn>
        </book>

      case Periodical(isbn, title, editor) =>
        <periodical>
          <title>{title}</title>
          <editor>{editor}</editor>
          <isbn>{isbn}</isbn>
        </periodical>
    }
}
~~~
</div>

### More Pattern Matching

The publisher is sending back sales information as XML. Because they hate you they are using a different format to the one you generate for them. Parse the data into a `case class Sale(item: Publication, quantity: Int, unitPrice: Double)`. The data they send looks like

~~~ xml
<sale qty="2" totalPrice="10.0" type="periodical">
  <title>Modern Drunkard Magazine</title>
  <editor>Frank Kelly Rich</editor>
  <isbn>0987654321</isbn>
</sale>
~~~

from which you should get a data structure like

~~~ scala
Sale(
  Periodical("Modern Drunkard Magazine", "Frank Kelly Rich", "0987654321")
  2,
  5.0
)
~~~

Some tips for processing XML:

* import `scala.xml._` to get access to `NodeSeq`, the main type for XML
* you can pattern match on XML element names but not XML attributes;
* `xml \ "node"` will get you the child node called `node` of `xml`;
* `xml \ "@attr"` will get you the attribute called `attr` of `xml`; and
* `xml.text` will get you the text content of `xml`.

The `toInt` and `toDouble` methods on `String` convert a `String` to `Int` and `Double` respectively.

<div class="solution">
~~~ scala
case class Sale(item: Publication, quantity: Int, unitPrice: Double)

object XmlImportService {
  def parse(in: NodeSeq): Sale = {
    val quantity = (in \ "@qty").text.toInt
    val totalPrice = (in \ "@totalPrice").text.toDouble
    val publication =
      (in \ "@type").text  match {
        case "book" =>
          Book(
            (in \ "isbn").text,
            (in \ "title").text,
            (in \ "author").text
          )
        case "periodical" =>
          Periodical(
            (in \ "isbn").text,
            (in \ "title").text,
            (in \ "editor").text
          )
      }
    Sale(publication, quantity, totalPrice / quantity)
  }
}
~~~
</div>

## Sealed Traits
