---
layout: page
title: Type Classes
---

We have seen how *type enrichment* allows us to augment individual existing types with extra functionality. Now we will see how we can add new functionality that cross-cuts a wide range of types using a new design pattern -- **type classes**.

## Type classes

*Type classes* are a programming idiom borrowed from the Haskell programming language. They provide a neat way to add functionality to existing classes without changing the classes themselves. Type classes are useful for a couple of reasons:

 - as with type enrichment, we may want to add functionality to classes that we don't have access to;

 - we may want to implement the same functionality in multiple different ways for a single type.

Broadly speaking, a type class in Scala involves three things:

 - a **type class** -- a trait specifying some kind of desirable functionality;

 - a set of **type class instances** -- objects that implement the type class functionality for various data types;

 - an automatic way of selecting an appropriate instance using a new feature of Scala, **implicit parameter lists**.

Let's motivate this with an example -- converting data to HTML. This is a fundamental operation in any web application, and it would be great to be able to provide a `toHtml` method across the board in our application.

To motivate this approach let's start by naively implementing `toHtml` using a simple trait:

~~~ scala
scala> trait HtmlWriteable {
     |   def toHtml: String
     | }
defined trait HtmlWriteable

scala> case class Person(name: String, email: String) extends HtmlWriteable {
     |   def toHtml = s"<span>$name &lt;$email&gt;</span>"
     | }
defined class Person

scala> Person("John", "john@example.com").toHtml
res0: String = <span>John &lt;john@example.com&gt;</span>
~~~

This solution has a number of drawbacks. First, we are restricted to having just one way of rendering a `Person`. If we want to list people on our company homepage, for example, it is unlikely we will want to list everybody's email addresses without obfuscation. Second, this pattern can only be applied to classes that we have written ourselves. If we want to render `java.util.Data` to HTML, for example, we will have to write some other form of library function.

We can overcome both of these problems by moving our HTML rendering to an adapter class:

~~~ scala
scala> trait HtmlWriter[T] {
     |   def write(in: T): String
     | }
defined trait HtmlWriter

scala> object PersonWriter extends HtmlWriter[Person] {
     |   def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
     | }
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
     |   def write(person: Person) =
     |     s"<span>${person.name} &lt;${person.email.replaceAll("@", " at ")}&gt;</span>"
     | }
defined module ObfuscatedPersonWriter

scala> ObfuscatedPersonWriter.write(Person("John", "john@example.com"))
res3: String = <span>John &lt;john at example.com&gt;</span>
~~~

Much safer -- it'll take a spam bot more than a few microseconds to decypher that!

The key thing with this design pattern is that we can render any type of data in any way we want simply by choosing the correct adapter for any given situation. However, we have to know what each writer is called. Ideally we'd like to use the same interface to convert any type to HTML. We can do this using a new Scala feature, **implicit parameter lists**.

## Implicit Parameter Lists

Here is an example of an implicit parameter list:

~~~ scala
scala> object HtmlUtil {
     |   def htmlify[T](data: T)(implicit writer: HtmlWriter[T]): String = {
     |     writer.write(data)
     |   }
     | }
defined module HtmlUtil
~~~

The `htmlify` method accepts two arguments: some `data` to convert to HTML and a `writer` to do the conversion. The `writer` is an implicit argument, so the compiler will automatically use any compatible `HtmlWriter` that we have defined using the `implicit` keyword.

The `implicit` keyword applies to the *whole parameter list*, not just an individual parameter. This makes the parameter list optional -- when we call `HtmlUtil.htmlify` we can either specify the list as normal:

~~~ scala
scala> HtmlUtil.htmlify(Person("John", "john@example.com"))(PersonWriter)
res2: String = <span>John &lt;john@example.com&gt;</span>
~~~

or we can omit the implicit parameter list. The compiler searches for **implicit values** of the correct type it can use to fill in the missing arguments. We have only seen implicit classes before so let's look at implicit values in a bit more detail.

## Implicit Values

We can tag any `val`, `var`, `object` or zero-argument `def` with the `implicit` keyword, making it a potential candidate for an implicit parameter:

~~~ scala
scala> implicit object PersonWriter extends HtmlWriter[Person] {
     |   def write(person: Person) =
     |     s"<span>${person.name} &lt;${person.email}&gt;</span>"
     | }
defined module PersonWriter
~~~

When the compiler expands an implicit argument list, it searches for candidate values for each argument by type. In our `htmlify` method the exact type will be decided by the type parameter `T` -- if `T` is `Person`, for example, the compiler searches for a value of type `HtmlWriter[Person]`.

The same resolution rules apply for implicit values as for implicit classes. If the compiler is unable to find suitable candidates for all parameters in the list, we get a compilation error.

Let's redefine our adapters for `HtmlWriter` so we can bring them all into scope. Note that outside the REPL implicit values are subject to the same packaging restrictions as implicit classes -- they have to be defined inside another class, object, or trait. We'll use the packaging convention we discussed in the previous section:

~~~ scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait HtmlImplicits {
  implicit object PersonWriter extends HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"
  }

  implicit object DateWriter extends HtmlWriter[Date] {
    def write(in: Date) = s"<span>${in.toString}</span>"
  }
}

object HtmlImplicits extends HtmlImplicits

// Exiting paste mode, now interpreting.

defined trait HtmlWriters
defined object HtmlWriters
~~~

We can now use our adapters with `htmlify`:

~~~ scala
scala> import HtmlImplicits._
import HtmlImplicits._

scala> HtmlUtil.htmlify(Person("John", "john@example.com"))
res4: String = <span>John &lt;john@example.com&gt;</span>
~~~

This version of the code has much lighter syntax requirements than its predecessor. We have now assembled the complete type class pattern: `HtmlUtil` specifies our HTML rendering functionality, `HtmlWriter` and `HtmlWriters` implement the functionality as a set of adapters, and the implicit argument to `htmlify` implicitly selects the correct adapter for any given argument. However, we can take things one step further to really simplify things.

## Packaging Type Classes

We can package type classes in two ways: using the trait/singleton approach we introduced for implicit classes, or using the companion objects of the relevant types.

Remember that one of the locations in implicit scope is *the companion objects of types involved in the type error*. This is particularly relevant to type class instances -- if the compiler is searching for a `HtmlWriter[Person]`, it will look in the companion objects of `HtmlWriter`, `Person`, and all of their superclasses:

We can use this behaviour to implement type class instances in the companion objects of the main data types in our application, as opposed to packaging them all into one big trait. For example:

~~~ scala
case class Person(name: String, email: String)

object Person {
  implicit val htmlWriter = new HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"
  }
}
~~~

{% comment %}
The compiler searches for implicits in local scope *first* before it looks at companion objects. We can therefore override companion object implicits with explicit imports:

~~~ scala
val person = Person("Dave", "dave@example.com")

HtmlUtil.htmlify(person) // uses Person.htmlWriter

import HtmlImplicits._

HtmlUtil.htmlify(person) // uses HtmlImplicits.PersonWriter
~~~
{% endcomment %}

## Combining Type Classes and Type Enrichment

Type classes allow us to define adapter-style patterns that implement fixed behaviour for any type we specify. Type enrichment allows us to add functionality to existing classes without changing their definitions. We can combine the two techniques to add standard functionality to a range of classes.

To do this we keep the type class (`HtmlWriter`) and adapters (`PersonWriter`, `DateWriter` and so on) from our type class example, but replace our `HtmlUtils` singleton with an implicit generic class. For example:

~~~ scala
scala> implicit class HtmlOps[T](data: T) {
     |   def toHtml(implicit writer: HtmlWriter[T]) =
     |     writer.write(data)
     | }
defined class HtmlOps
~~~

This allows us to invoke our type-class pattern on any type for which we have an adapter *as if it were a built-in feature of the class*:

~~~ scala
scala> Person("John", "john@example.com").toHtml
res5: String = <span>John &lt;john@example.com&gt;</span>
~~~

This gives us many benefits. We can extend existing types to give them new functionality, use simple syntax to invoke the functionality, *and* choose our preferred implementation by controlling which implicits we have in scope.

## Take Home Points

Like type enrichment, **type classes** are a general programming pattern. Type classes allow us to define a piece of functionality for a wide range of classes without modifying the source code for those classes.

The type class pattern consists of three things: a *type class*, a set of *type class instances*, and a method of associating the type class instance for any particular type.

The Scala implementation of type classes uses **implicit values** to define type class instances and **implicit parameter lists** as the method of instance resolution.

We can package type class instances together in traits/singletons or separately in the **companion objects** of individual types.

We can combine type classes with type enrichment to add new behaviour to existing types across a whole application.

## Exercises

These exercises involve serializing Scala data to JSON, which is one of the classic use cases for type classes. The typical process for converting data to JSON in Scala involves two steps. First we convert our data types to an intermediate case class representation, then we serialize the intermediate representation to a string.

Here is a suitable case class representation of a subset of the JSON language. We have a `sealed trait JsValue` that defines a `stringify` method, and a set of subtypes for two of the main JSON data types -- objects and strings:

~~~ scala
sealed trait JsValue {
  def stringify: String
}

final case class JsObject(values: Map[String, JsValue]) extends JsValue {
  def stringify = values
    .map { case (name, value) => "\"" + name + "\":" + value.stringify }
    .mkString("{", ",", "}")
}

final case class JsString(value: String) extends JsValue {
  def stringify = "\"" + value.replaceAll("\\|\"", "\\\\$1") + "\""
}
~~~

We can construct JSON objects and serialize them as follows:

~~~ scala
scala> JsObject(Map("foo" -> JsString("a"), "bar" -> JsString("b"), "baz" -> JsString("c")))
res2: JsObject = JsObject(Map(a -> JsArray(List(JsString(a), JsString(b), JsString(c)))))
scala> JsObject(Map("foo" -> JsString("a"), "bar" -> JsString("b"), "baz" -> JsString("c")))
res4: JsObject = JsObject(Map(foo -> JsString(a), bar -> JsString(b), baz -> JsString(c)))

scala> res4.stringify
res5: String = {"foo":"a","bar":"b","baz":"c"}
~~~

### Convert X to JSON

Let's create a type class for converting Scala data to JSON. Implement a `JsWriter` trait containing a single abstract method `write` that converts a value to a `JsValue`.

<div class="solution">
The *type class* is generic in a type `A`. The `write` method converts a value of type `A` to some kind of `JsValue`.

~~~ scala
trait JsWriter[A] {
  def write(value: A): JsValue
}
~~~
</div>

Now let's create the dispatch part of our type class. Write a `JsUtil` object containing a single method `toJson`. The method should accept a value of an arbitrary type `A` and convert it to JSON.

Tip: your method will have to accept an implicit `JsWriter` to do the actual conversion.

<div class="solution">
~~~ scala
object JsUtil {
  def toJson[A](value: A)(implicit writer: JsWriter[A]) =
    writer write value
}
~~~
</div>

Now, let's revisit our data types from the web site visitors example in the [Sealed traits](/objects/sealed-traits.html) section:

~~~ scala
import java.util.Date

sealed trait Visitor {
  def id: String
  def createdAt: Date
  def age: Long = new Date().getTime() - createdAt.getTime()
}

final case class Anonymous(
  val id: String,
  val createdAt: Date = new Date()
) extends Visitor

final case class User(
  val id: String,
  val email: String,
  val createdAt: Date = new Date()
) extends Visitor
~~~

Write `JsWriter` instances for `Anonymous` and `User`.

<div class="solution">
~~~ scala
implicit object AnonymousWriter extends JsWriter[Anonymous] {
  def write(value: Anonymous) = JsObject(Map(
    "id"           -> JsString(value.id),
    "createdAt"    -> JsString(value.createdAt.toString)
  ))
}

implicit object UserWriter extends JsWriter[User] {
  def write(value: User) = JsObject(Map(
    "id"           -> JsString(value.id),
    "email"        -> JsString(value.email),
    "createdAt"    -> JsString(value.createdAt.toString)
  ))
}
~~~
</div>

Given these two definitions we can implement a `JsWriter` for `Visitor` as follows. This uses a new type of pattern -- `a: B` -- which matches any value of type `B` and binds it to a variable `a`:

~~~ scala
implicit object VisitorWriter extends JsWriter[Visitor] {
  def write(value: Visitor) = value match {
    case anon: Anonymous => JsUtil.toJson(anon)
    case user: User      => JsUtil.toJson(user)
  }
}
~~~

Finally, verify that your code works by converting the following list of users to JSON:

~~~ scala
val visitors: Seq[Visitor] = Seq(Anonymous("001", new Date), User("003", "dave@xample.com", new Date))
~~~

<div class="solution">
~~~ scala
visitors.map(visitor => JsUtil.toJson(visitor))
~~~
</div>

### Prettier Conversion Syntax

Let's improve our JSON syntax by combining type classes and type enrichment. Convert `JsUtil` to an `implicit class` with a `toJson` method. Sample usage:

~~~ scala
Anonymous("001", new Date).toJson
~~~

<div class="solution">
~~~ scala
implicit class JsUtil[A](value: A) {
  def toJson(implicit writer: JsWriter[A]) =
    writer write value
}
~~~

In the previous exercise we only defined `JsWriters` for our main case classes. With this convenient syntax, it makes sense for us to have an complete set of `JsWriters` for all the serializable types in our codebase, including `Strings` and `Dates`:

~~~ scala
implicit object StringWriter extends JsWriter[String] {
  def write(value: String) = JsString(value)
}

implicit object DateWriter extends JsWriter[Date] {
  def write(value: Date) = JsString(value.toString)
}
~~~

With these definitions we can simplify our existing `JsWriters` for `Anonymous`, `User`, and `Visitor`:

~~~ scala
implicit object AnonymousWriter extends JsWriter[Anonymous] {
  def write(value: Anonymous) = JsObject(Map(
    "id"        -> value.id.toJson,
    "createdAt" -> value.createdAt.toJson
  ))
}

implicit object UserWriter extends JsWriter[User] {
  def write(value: User) = JsObject(Map(
    "id"        -> value.id.toJson,
    "email"     -> value.email.toJson,
    "createdAt" -> value.createdAt.toJson
  ))
}

implicit object VisitorWriter extends JsWriter[Visitor] {
  def write(value: Visitor) = value match {
    case anon: Anonymous => anon.toJson
    case user: User      => user.toJson
  }
}
~~~
</div>