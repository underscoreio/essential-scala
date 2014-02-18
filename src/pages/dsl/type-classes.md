---
layout: page
---

# Type classes

*Type classes* are a programming idiom borrowed from the Haskell programming language. They provide a neat way to add functionality to existing classes without changing the classes themselves, and are a common use of implicit parameter lists. Type classes are useful for a couple of reasons:

 - We may not have access to the source for the original classes.
 - We may want to implement the same functionality in different ways in different parts of our application.

Broadly speaking, a type class in Scala involves three things:

 1. a trait, specifying some kind of desirable functionality;
 2. a set of adapters, implementing the functionality for various data types;
 3. an automatic way of selecting an appropriate adapter using implicits.

Let's motivate this with an example: converting data to HTML. This is a fundamental operation in any web application, and it would be great to be able to provide a `toHtml` method across the board in our application.

Let's start by naively implementing `toHtml` using a simple trait:

~~~ scala
scala> import scala.xml._

scala> trait HtmlWriteable {
     |   def toHtml: NodeSeq
     | }
defined trait HtmlWriteable

scala> class Person(var name: String, var email: String) extends HtmlWriteable {
     |   def toHtml = <span>{ name } &lt;{ email }&gt;</span>
     | }
defined class Person

scala> new Person("John", "john@example.com").toHtml
res2: scala.xml.Elem = <span>John &lt;john@example.com&gt;</span>
~~~

This solution has a number of drawbacks. First, we are restricted to having just one way of rendering a `Person`. If we want to list people on our company homepage, for example, it is unlikely we will want to list everybody's email addresses without any kind of obfuscation. Second, this pattern can only be applied to classes that we have written ourselves. If we want to render `java.util.Data` to HTML, for example, we will have to write some other form of library function.

We can overcome both of these problems by moving our HTML rendering function to an adapter class:

~~~ scala
scala> import java.util.Date
import java.util.Date

scala> trait HtmlWriter[T] {
     |   def write(in: T): NodeSeq
     | }
defined trait HtmlWriter

scala> object PersonWriter extends HtmlWriter[Person] {
     |   def write(in: Person) = <span>{ in.name } &lt;{ in.email }&gt;</span>
     | }
defined module PersonWriter

scala> object DateWriter extends HtmlWriter[Date] {
     |   def write(in: Date) = <span>{ in.toString }</span>
     | }
defined module DateWriter

scala> PersonWriter.write(new Person("John", "john@example.com"))
res3: scala.xml.Elem = <span>John &lt;john@example.com&gt;</span>
~~~

Now we can render any type of data in any way we want by simply choosing a different adapter for any given situation. However, there are still problems with the code. Whenever we want to render an object, we have to write out the name of a relevant `HtmlWriter`. This makes our code more verbose and difficult to maintain than the `toHtml` version we had above.

The type class pattern gives us a way of reducing the code overhead of the adapter approach. Let's introduce a simple example now. We know that we can convert anything to HTML as long as we have a compatible `HtmlWriter` in scope. Let's write this out in code:

~~~ scala
scala> object HtmlUtil {
     |   def htmlify[T](data: T)(implicit writer: HtmlWriter[T]): NodeSeq = {
     |     writer.write(data)
     |   }
     | }
defined module HtmlUtil
~~~

The `htmlify` method accepts two arguments: some `data` to convert to HTML and a `writer` to do the conversion. The `writer` is an implicit argument, so the compiler will automatically use any compatible `HtmlWriter` that is in implicit scope. Let's redefine our adapters so we can bring them into scope:

~~~ scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait HtmlWriters {
  implicit object PersonWriter extends HtmlWriter[Person] {
    def write(in: Person) = <span>{ in.name } &lt;{ in.email }&gt;</span>
  }
  implicit object DateWriter extends HtmlWriter[Date] {
    def write(in: Date) = <span>{ in.toString }</span>
  }
}

object HtmlWriters extends HtmlWriters

// Exiting paste mode, now interpreting.

defined trait HtmlWriters
defined object HtmlWriters
~~~

This is the idiomatic way of defining a set of adapters for use in a type class: we define everything in a trait, and then instantiate that trait as a singleton of the same name. The singleton gives us direct access to the definitions via an `import` statement, while the trait allows us to replace any adapter with anoter variant to create a custom set.

We can now use our adapters with `htmlify`:

~~~ scala
scala> import HtmlUtil._
import HtmlUtil._

scala> import HtmlWriters._
import HtmlWriters._

scala> htmlify(new Person("John", "john@example.com"))
res4: scala.xml.NodeSeq = <span>John &lt;john@example.com&gt;</span>
~~~

This final example has much lighter syntax requirements than its predecessor. We have now assembled the complete type class pattern: `HtmlUtil` specifies our HTML rendering functionality, `HtmlWriter` and `HtmlWriters` implement the functionality as a set of adapters, and the implicit argument to `htmlify` implicitly selects the correct adapter for any given argument.
