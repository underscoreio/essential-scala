---
layout: page
title: Type Classes
---

We have now seen all the fundamental pieces of type classes. They are quite involved, so let's recap the steps needed to construct and use a type class.

<div class="callout callout-info">
#### Type Class Pattern

To implement a type class we must implement three things:

1. a trait, defining the interface we will support;
2. some implicit instances of the trait; and
3. an object with one or more methods using implicit parameters to select the appropriate instance.

The trait is *the* type class and always has a generic type parameter

~~~ scala
trait TypeClass[A] {
  declarationOrExpression ...
}
~~~

Instances implement the type class for a particular type.

~~~ scala
object AnInstance {
  implicit object theInstance extends TypeClass[TheClass] {
    ...
  }
}
~~~

The convenience interface provides methods using implicit parameters to require instances. Methods have a generic type that controls type class instance selection.

~~~ scala
object Interface {
  def method[A](implicit instance: TypeClass[A]) = ...
  ...
}
~~~
</div>

Now we have type classes down let's look in more detail at the implicit resolution rules and type class instance packaging.

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
