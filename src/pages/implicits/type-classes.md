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

Remember that one of the locations in implicit scope is *the companion objects of types involved in the type error*. This is particularly relevant to type class instances -- if the compiler is searching for a `HtmlWriter[Person]`, it will look in the companion objects of `HtmlWriter`, `Person`, and all of their superclasses.

We can use this behaviour to implement default type class instances for the main data types in our application. For example:

~~~ scala
case class Person(name: String, email: String)

object Person {
  implicit val htmlWriter = new HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"
  }
}
~~~

The compiler searches for implicits in local scope *first* before it looks at companion objects. We can therefore override companion object implicits with explicit imports:

~~~ scala
val person = Person("Dave", "dave@example.com")

HtmlUtil.htmlify(person) // uses Person.htmlWriter

import HtmlImplicits._

HtmlUtil.htmlify(person) // uses HtmlImplicits.PersonWriter

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

### TODO
