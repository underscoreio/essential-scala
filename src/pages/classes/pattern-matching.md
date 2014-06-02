---
layout: page
title: Pattern Matching
---

We have seen how to use traits to model data that is of one type or another. Now we'll look at how to use this data. Presumably the distinction between types is important or we wouldn't have bothered making it in the first place. Let's imagine we want to add the ability to email `Visitor`s. We can only email `User`s, because we don't know the email address for `Anonymous` visitors. If we have a `Visitor` how can we tell what subtype we have, and thus if we can email them?

We're going to look at two solutions: an object-oriented solution based on dynamic dispatch, and a functional solution based on pattern matching.

The object-oriented solution is to add an abstract method to `Visitor` along with implementations in the subtypes. The code looks something like this (trimmed for simplicity).

~~~ scala
trait Visitor {
  def email(subject: String, body: String): Unit
}

case class Anonymous() extends Visitor {
  // Do nothing; you can't email an anonymous visitor
  def email(subject: String, body: String) =
    ()
}

case class User() extends Visitor {
  def email(subject: String, body: String) = {
    reallySendAnEmail(email, subject, body)
  }
}
~~~

Then if we have a `Visitor` we can simply call the `email` method and stuff happens, as if by magic.

I maintain **this is a bad design**. As we add more features to do with `Visitor`s we'll find the `Visitor` class getting bigger and bigger. It will be harder to maintain and test, and any system that uses `Visitor` will have to carry around all its dependencies.

A better design is to keep our case classes as [value objects](http://en.wikipedia.org/wiki/Value_object), meaning they shouldn't change (i.e. they are immutable), and they should only have methods that relate directly to the data they hold. The `age` method is fine, as it depends only on the `createdAt` date. The `email` method depends on a whole host of email processing code and thus should not appear in the `Visitor` trait. Instead methods to email `Visitor`s should be in a separate service. Let's call it `EmailService`.

~~~ scala
trait EmailService {
  def email(v: Visitor, subject: String, body: String): Unit = {
    // What do we put here?
  }
}
~~~

In the `email` method we now need to know if we have a `User` or an `Anonymous`. Scala has an awesome general purpose tool called **pattern matching** that we use in this situation. The code looks like this:

~~~ scala
trait EmailService {
  def email(v: Visitor, subject: String, body: String): Unit = {
    v match {
      case Anonymous(id, createdAt) =>
        ()
      case User(id, address, createdAt) =>
        reallySendAnEmail(address, subject, body)
    }
  }
}
~~~

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

The Scala compiler won't complain if we miss out a case in our pattern matching, but we will get an exception at runtime. For example:

~~~ scala
scala> def missingCase(v: Visitor) =
     |   v match {
     |     case User(_, _, _) => "Got a user"
     |   }
missingCase: (v: Visitor)String

scala> missingCase(Anonymous("a"))
missingCase(Anonymous("a"))
scala.MatchError: Anonymous(a,Fri Feb 14 19:47:42 GMT 2014) (of class Anonymous)
  at .missingCase(<console>:12)
    ...
~~~

The reason we don't get a compiler error is that other code could extend `Visitor`, creating new subtypes, so the Scala compiler can't be sure that only the two cases exist. This means that even a pattern match that appears complete could be rendered incomplete by additional subclasses. To avoid compilation errors in some cases but not others the compiler is silent in all cases.

However, when we define `Visitor` we can tell the Scala compiler that we are defining at the same time all the possible subtypes. In this case the compiler can correctly flag incomplete matches and the code above would fail to compile. We can do this by using a `sealed` trait.

We using a sealed trait we must define all the subtypes in the same file. For the `Visitor` example no other subtypes seem plausible -- either a visitor is anonymous or they are not. In this case we should use a sealed trait.

~~~ scala
sealed trait Visitor {
  def id: String // A unique id we assign to each user
  def createdAt: Date // The date this user first visited our site

  // How long has this visitor been around?
  def age: Long =
    new Date().getTime() - createdAt.getTime()

}

case class Anonymous(
  val id: String,
  val createdAt: Date = new Date()
) extends Visitor

case class User(
  val id: String,
  val email: String,
  val createdAt: Date = new Date()
) extends Visitor
~~~

When we do this, the compiler warns us if we don't have exhaustive pattern matching:

~~~ scala
scala> def missingCase(v: Visitor) =
       |  v match {
       |    case User(_, _, _) => "Got a user"
       |  }
<console>:21: warning: match may not be exhaustive.
It would fail on the following input: Anonymous(_, _)
               v match {
               ^
missingCase: (v: Visitor)String
~~~

Note that we can still extend subtypes of a sealed trait outside of the file where they are defined. For example, in other code we could extend `User` or `Anonymous`. If we want to prevent this possibility we should declare them as `sealed` (if we want to allow extensions within the file) or `final` if we want to disallow all extensions. For the analytics example it probably doesn't make sense to allow any extension, so the simplified code should look like this:

~~~ scala
sealed trait Visitor { ... }
final case class User(...) extends Visitor
final case class Anonymous(...) extends Visitor
~~~

Now imagine the problem of parsing HTTP headers. Most of the HTTP headers are defined in various RFCs, and we could define types for these as follows:

~~~ scala
sealed trait Header { ... }
final case class Accept(...) extends Header
final case class AcceptCharset(...) extends Header
...
~~~

It certainly doesn't make sense to subtype one of the defined headers so we can defined them as `final`. However custom headers are used all the time, so we need to provide some extension point. To achieve this we can define a `CustomHeader` subtype of `Header` that is neither `sealed` nor `final`. This allows the user to extend `CustomHeader` in their code, but we still retain the benefits of `sealed` and `final` types in ours[^http].

[^http]: There are so many HTTP headers that we're unlikely to perform pattern matching on them, but hopefully you can generalise this example to your own problems.

## Conclusion

We've covered a lot of material. Let's recap the main points.

We've been looking at data that can be one of a number of choices. We can model this by having each case `extend` a common trait. We usually use case classes as the leaf nodes of our hierarchy.

It is good practice to model our data as value objects, and avoid methods that depend on more than the data present in the objects. Instead we can use pattern matching to decompose data and take appropriate action for each case.

Finally, we saw we can used `sealed` and `final` to control how our data can be extended by external users.
